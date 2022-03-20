package me.hechfx.konnor.config

import kotlinx.serialization.Serializable
import me.hechfx.konnor.config.database.DatabaseConfig

@Serializable
data class DiscordConfig (
    val konnorConfig: KonnorConfig,
    val databaseConfig: DatabaseConfig
)