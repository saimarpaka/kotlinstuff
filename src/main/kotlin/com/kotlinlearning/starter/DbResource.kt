package com.kotlinlearning.starter

import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient

class DbResource : IDbResource{

    override val sqlClient : SqlClient

    init {
        val connectionOptions =
            PgConnectOptions()
                .setPort(5433)
                .setHost("192.168.1.252")
                .setDatabase("postgres")
                .setUser("postgres")
        val poolOptions = PoolOptions().setMaxSize(200)
        val client = PgPool.client(Vertx.vertx(),connectionOptions, poolOptions)
        sqlClient = client
    }
}

interface IDbResource {
    val sqlClient : SqlClient
}
