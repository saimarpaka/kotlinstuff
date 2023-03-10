package com.kotlinlearning.starter

import com.google.gson.Gson
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Launcher
import io.vertx.core.Promise
import io.vertx.core.VertxOptions
import io.vertx.core.http.impl.HttpClientConnection.log
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*


class MainVerticle : AbstractVerticle() {

  private val eventRepository : IEventRepository
  init {
    val dbResource = DbResource()
    eventRepository = EventRepository(dbResource)
  }
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val deploymentOptions = DeploymentOptions().setInstances(8)
      val vertx = io.vertx.core.Vertx.vertx()
      vertx.deployVerticle(MainVerticle::class.java, deploymentOptions)
    }
  }
  @OptIn(DelicateCoroutinesApi::class)
  private fun coroutineHandler(route: Route, handler: suspend (RoutingContext) -> Unit) {
    route.handler { ctx ->
      GlobalScope.launch(ctx.vertx().dispatcher()) {
        try {
          handler(ctx)
        } catch (e: Exception) {
          log.error("Coroutine handler failed: $e")
          ctx.fail(e)
        }
      }
    }
  }

  private suspend fun getDummyValue() : Int {
    println("getDummyValue()")
    delay(2000)
    return 10
  }

  override fun start(startPromise: Promise<Void>) {
    val gson = Gson()
    val router = Router.router(vertx)
    coroutineHandler(router.route("/")) {
      val added = coroutineScope {
        val firstDummy = getDummyValue()
        val secondDummy = getDummyValue()
        firstDummy + secondDummy
      }
      println(added)
      val rows = eventRepository.getAll()
      it.response().end(gson.toJson(rows))
    }
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
