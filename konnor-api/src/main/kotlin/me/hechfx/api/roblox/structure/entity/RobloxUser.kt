package me.hechfx.api.roblox.structure.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RobloxUser(
    @SerialName("id")
    val userId: Long,
    val name: String,
    val displayName: String,
    val description: String,
    @SerialName("created")
    val createdAt: String,
    val isBanned: Boolean,
    @SerialName("externalAppDisplayName")
    val externalDisplayName: String
)
