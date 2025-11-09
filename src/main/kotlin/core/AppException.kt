package com.mod.core

import io.ktor.http.HttpStatusCode

sealed class AppException(
    message: String,
    val statusCode: HttpStatusCode
) : Exception(message)

class EmailAlreadyExistsException(email: String) : AppException(
    message = "Email '$email' is already registered",
    statusCode = HttpStatusCode.Conflict
)

class EmailNotExistsException : AppException(
    message = "Email not found",
    statusCode = HttpStatusCode.NotFound
)

class EmailAlreadyVerifiedException : AppException(
    message = "Email already verified",
    statusCode = HttpStatusCode.BadRequest
)

class NoVerificationCodeFoundException : AppException(
    message = "No verification code has been sent to this email",
    statusCode = HttpStatusCode.BadRequest
)

class VerificationCodeExpiredException : AppException(
    message = "Verification code has been expired",
    statusCode = HttpStatusCode.BadRequest
)

class InvalidVerificationCodeException : AppException(
    message = "Invalid verification code",
    statusCode = HttpStatusCode.BadRequest
)

class InvalidCredentialsException : AppException(
    message = "Invalid email or password",
    statusCode = HttpStatusCode.Unauthorized
)

class EmailIsNotVerifiedException : AppException(
    message = "Email is not verified",
    statusCode = HttpStatusCode.Forbidden
)

class ResourceNotFoundException(resource: String, id: String) : AppException(
    message = "$resource with id '$id' not found",
    statusCode = HttpStatusCode.NotFound
)

class ValidationException(message: String) : AppException(
    message = message,
    statusCode = HttpStatusCode.BadRequest
)

class UnauthorizedException(message: String = "Unauthorized access") : AppException(
    message = message,
    statusCode = HttpStatusCode.Unauthorized
)