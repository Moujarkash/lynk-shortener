package com.mod

import com.mod.plugins.configureFrameworks
import com.mod.plugins.configureHTTP
import com.mod.plugins.configureMonitoring
import com.mod.plugins.configureRouting
import com.mod.plugins.configureSecurity
import com.mod.plugins.configureSerialization
import io.github.cdimascio.dotenv.dotenv
import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    dotenv.entries().forEach {
        System.setProperty(it.key, it.value) // Load each .env entry into system properties
    }

    EngineMain.main(args)
}


fun Application.module() {
    configureFrameworks(log, environment.config)
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()

    routing {
        install(RateLimiting) {
            rateLimiter {
                type = TokenBucket::class
                capacity = 100
                rate = 10.seconds
            }
        }

        /**
         * Get root.
         *
         * @response 201
         */
        route("/") {
            get {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        swaggerUI(path = "swagger", swaggerFile = "openapi/open-api.json")
        openAPI(path = "openapi", swaggerFile = "openapi/open-api.json")
    }
}
