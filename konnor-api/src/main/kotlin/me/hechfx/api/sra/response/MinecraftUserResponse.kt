package me.hechfx.api.sra.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftUserResponse(
    val username: String,
    val uuid: String,
    @SerialName("name_history")
    val nameHistory: List<NameHistoryResponse>
) {
    @Serializable
    data class NameHistoryResponse(
        val name: String,
        @SerialName("changedToAt")
        val changedAt: String
    )
}