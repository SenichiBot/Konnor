package me.hechfx.konnor.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfig (
    val konnorConfig: KonnorConfig,
    val databaseConfig: DatabaseConfig
)