package com.reas.tracker2.database.tables

import org.jetbrains.exposed.v1.core.Table

object ApiKeyTable : Table("api_keys") {
    val user = text("user")
    val key = varchar("key", 32)
    val lastSeenId = long("last_seen_id")
}