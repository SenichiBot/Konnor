package me.hechfx.konnor.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val port: String,
    val database: String,
    val user: String,
    val password: String
)
