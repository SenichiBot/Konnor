package me.hechfx.konnor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.database.table.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseService {
    fun connect(databaseConfig: DiscordConfig.DatabaseConfig) {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://${databaseConfig.host}:${databaseConfig.port}/${databaseConfig.database}?useTimezone=true&serverTimezone=UTC"
            driverClassName = "org.postgresql.Driver"
            username = databaseConfig.user
            password = databaseConfig.password
            maximumPoolSize = 8
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }
}