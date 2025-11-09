package com.mod.database

import com.mod.database.tables.EmailVerificationCodes
import com.mod.database.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.sql.DriverManager
import kotlin.getValue

object DatabaseFactory: KoinComponent {
    private val config by inject<ApplicationConfig>()
    private val appMicrometerRegistry by inject<PrometheusMeterRegistry>()

    private val dbHost = config.property("database.host").getString()
    private val dbPort = config.property("database.port").getString()
    private val maxPoolSize = config.property("database.maxPoolSize").getString()
    private val dbUser = config.property("database.username").getString()
    private val dbPassword = config.property("database.password").getString()
    private val dbName = config.property("database.name").getString()

    private val url = "jdbc:postgresql://${dbHost}:${dbPort}/$dbName?reWriteBatchedInserts=true"

    fun init() {
        setupDatabase()

        val datasource = HikariDataSource(HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = url
            username = dbUser
            password = dbPassword
            maximumPoolSize = maxPoolSize.toInt()
            metricRegistry = appMicrometerRegistry
            addDataSourceProperty("reWriteBatchedInserts", true)
            validate()
        })

        Database.connect(datasource)

        transaction {
            SchemaUtils.create(Users, EmailVerificationCodes)
        }
    }

    private fun setupDatabase() {
        val postgresConnection = DriverManager.getConnection(
            "jdbc:postgresql://${dbHost}:${dbPort}/postgres",
            dbUser,
            dbPassword
        )

        val resultSet = postgresConnection.createStatement()
            .executeQuery("SELECT 1 FROM pg_database WHERE datname = '${dbName}'")
        val dbExists = resultSet.next()

        if (!dbExists) {
            postgresConnection.createStatement().execute("CREATE DATABASE $dbName")
        }

        postgresConnection.close()

        val appConnection = DriverManager.getConnection(
            "jdbc:postgresql://${dbHost}:${dbPort}/$dbName",
            dbUser,
            dbPassword
        )

        appConnection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS citext")
         appConnection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

        appConnection.close()
    }
}