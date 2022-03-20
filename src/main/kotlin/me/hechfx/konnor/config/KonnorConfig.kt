package me.hechfx.konnor.config

import kotlinx.serialization.Serializable

@Serializable
data class KonnorConfig(
    val token: String,
    val applicationId: Long
)