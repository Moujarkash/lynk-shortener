package com.mod.database.entities

import com.mod.database.tables.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)
    var email by Users.email
    var password by Users.password
    var isEmailVerified by Users.isEmailVerified
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
}