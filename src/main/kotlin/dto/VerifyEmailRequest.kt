package com.mod.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val code: String
)