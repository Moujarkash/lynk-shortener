package com.mod.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgetPasswordRequest (
    val email: String
)