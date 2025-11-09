package com.mod.database.tables

import com.mod.database.citext
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users: UUIDTable("users") {
    val email = citext("email").uniqueIndex()
    val password = varchar("password", 255)
    val isEmailVerified = bool("is_email_verified").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}