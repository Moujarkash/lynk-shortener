package com.mod.plugins

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import com.mod.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.logging.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks(logger: Logger, config: ApplicationConfig) {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single { logger }
            single { config }
            single { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
            single {
                MailgunClient.config(environment.config.property("mailgun.apiKey").getString())
                    .createApi(MailgunMessagesApi::class.java)
            }
        }, servicesModule)
    }
}

val servicesModule = module {
    singleOf(::AuthService)
}
