package com.mod.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.logging.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks(logger: Logger, config: ApplicationConfig) {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single { logger }
            single { config }
        })
    }
}
