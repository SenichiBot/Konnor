package me.hechfx.api.sra.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftServerResponse(
    val online: Boolean,
    val status: Boolean,
    val favicon: String,
    @SerialName("took")
    val ping: Double,
    val version: MinecraftServerVersionResponse,
    val players: MinecraftServerPlayersResponse,
    val description: String
) {
    @Serializable
    data class MinecraftServerVersionResponse(
        val name: String,
        val protocol: Int
    )

    @Serializable
    data class MinecraftServerPlayersResponse(
        val max: Long,
        val online: Long
    )
}