package com.cybersecurity.aianalyzer.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object AiCacheTable : Table("ai_response_cache") {
    val id = integer("id").autoIncrement()
    val requestPrompt = text("request_prompt")
    val modelName = varchar("model_name", 128)
    val responseJson = text("response_json")

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:file:./build/ai_cache_db;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(AiCacheTable)
        }
    }
}
