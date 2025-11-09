package com.mod.plugins

import com.mod.core.AppException
import com.mod.dto.MessageResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

fun Application.configureRouting() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            call.respond(
                status = cause.statusCode,
                message = MessageResponse(
                    message = cause.message ?: "An error occurred"
                )
            )
        }

        exception<SerializationException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = MessageResponse(
                    message = cause.message ?: "Invalid request format"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)

            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = MessageResponse(
                    message = "An unexpected error occurred"
                )
            )
        }
    }
}
