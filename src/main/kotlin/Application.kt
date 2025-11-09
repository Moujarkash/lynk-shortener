package com.mod

import com.mod.controllers.authRoutes
import com.mod.database.DatabaseFactory
import com.mod.plugins.*
import com.mod.services.AuthService
import io.github.cdimascio.dotenv.dotenv
import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.getValue
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    dotenv.entries().forEach {
        System.setProperty(it.key, it.value) // Load each .env entry into system properties
    }

    try {
        EngineMain.main(args)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Application.module() {
    configureFrameworks(log, environment.config)
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()

    DatabaseFactory.init()

    val authService by inject<AuthService>()

    routing {
        install(RateLimiting) {
            rateLimiter {
                type = TokenBucket::class
                capacity = 100
                rate = 10.seconds
            }
        }

        route("/api/v1") {
            authRoutes(authService)
        }
    }
}
