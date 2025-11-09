package com.mod.controllers

import com.mod.core.Validator
import com.mod.dto.*
import com.mod.services.AuthService
import io.ktor.http.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/signup") {
            try {
                val body = call.receive<CreateUserRequest>()

                if (!Validator.isValidEmail(body.email)) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is not valid"))
                    return@post
                }

                val (isPasswordValid, errorMessage) = Validator.validatePassword(body.password)
                if (!isPasswordValid) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(errorMessage!!))
                    return@post
                }

                authService.signup(body.email, body.password)
                call.respond(HttpStatusCode.OK, MessageResponse("Verification code has been sent to your email"))
            } catch (_: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Email and Password are required"))
            }

        }

        post("/verify") {
            try {
                val body = call.receive<VerifyEmailRequest>()

                if (!Validator.isValidEmail(body.email)) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is not valid"))
                    return@post
                }

                if (body.code.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Code is not valid"))
                    return@post
                }

                val token = authService.verifyEmail(body.email, body.code)
                call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
            } catch (_: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Email and Code are required"))
            }
        }

        post("/login") {
            try {
                val body = call.receive<LoginRequest>()

                if (!Validator.isValidEmail(body.email)) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is not valid"))
                    return@post
                }

                if (body.password.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Password is required"))
                    return@post
                }

                val token = authService.login(body.email, body.password)
                call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
            } catch (_: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Email and Password are required"))
            }
        }

        post("/forget-password") {
            try {
                val body = call.receive<ForgetPasswordRequest>()

                if (!Validator.isValidEmail(body.email)) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is not valid"))
                    return@post
                }

                authService.forgetPassword(body.email)
                call.respond(HttpStatusCode.OK, MessageResponse("Verification code has been sent to your email"))
            } catch (_: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is required"))
            }
        }

        post("/reset-password") {
            try {
                val body = call.receive<ResetPasswordRequest>()

                if (!Validator.isValidEmail(body.email)) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Email is not valid"))
                    return@post
                }

                if (body.code.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse("Code is not valid"))
                    return@post
                }

                val (isPasswordValid, errorMessage) = Validator.validatePassword(body.password)
                if (!isPasswordValid) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(errorMessage!!))
                    return@post
                }

                val token = authService.resetPassword(body.email, body.code, body.password)
                call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
            } catch (_: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Email, Code and Password are required"))
            }
        }
    }
}