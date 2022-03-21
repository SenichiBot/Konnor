package me.hechfx.konnor.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfig (
    val konnorConfig: KonnorConfig,
    val databaseConfig: DatabaseConfig,
    val valorantConfig: ValorantConfig
) {
    @Serializable
    data class KonnorConfig(
        val token: String,
        val applicationId: Long
    )

    @Serializable
    data class DatabaseConfig(
        val host: String,
        val port: String,
        val database: String,
        val user: String,
        val password: String
    )

    @Serializable
    data class ValorantConfig(
        val apiKey: String
    )
}