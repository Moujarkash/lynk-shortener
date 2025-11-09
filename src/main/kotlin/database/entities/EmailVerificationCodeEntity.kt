package com.mod.database.entities

import com.mod.database.tables.EmailVerificationCodes
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class EmailVerificationCodeEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<EmailVerificationCodeEntity>(EmailVerificationCodes)
    var user by UserEntity referencedOn EmailVerificationCodes.userId
    var code by EmailVerificationCodes.code
    var expiresAt by EmailVerificationCodes.expiresAt
    var isUsed by EmailVerificationCodes.isUsed
    var createdAt by EmailVerificationCodes.createdAt
}