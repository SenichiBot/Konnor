package me.hechfx.konnor.config.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val port: String,
    val database: String,
    val user: String,
    val password: String
)
