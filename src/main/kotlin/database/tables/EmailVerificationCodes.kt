package com.mod.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object EmailVerificationCodes : UUIDTable("email_verification_codes") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val code = varchar("code", 255)
    val expiresAt = timestamp("expires_at")
    val isUsed = bool("is_used").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index(false, userId, code)
    }
}