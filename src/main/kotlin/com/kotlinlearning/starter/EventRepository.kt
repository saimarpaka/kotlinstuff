package com.kotlinlearning.starter

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import kotlinx.coroutines.coroutineScope

class EventRepository(dbResource: DbResource) : IEventRepository {
    private val sqlClient : SqlClient
    init {
        sqlClient = dbResource.sqlClient
    }
    override suspend fun getAll() : List<Event> =
        coroutineScope {
            val rowSet = sqlClient
                .query("Select * FROM events")
                .execute().await()

            val rows = rowSet.map { rs ->
                    Event(rs.getLong("id"), rs.getString("value"))
                }
            println(Thread.currentThread().name)
            rows
        }

}

interface IEventRepository {
    suspend fun getAll() : List<Event>
}
