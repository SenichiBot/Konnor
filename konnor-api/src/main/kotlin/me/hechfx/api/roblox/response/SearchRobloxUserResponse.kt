package me.hechfx.api.roblox.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRobloxUserResponse(
    @SerialName("previousPageCursor")
    val previousPage: String? = null,
    @SerialName("nextPageCursor")
    val nextPage: String? = null,
    val data: List<UserResponse>
) {
    @Serializable
    data class UserResponse(
        @SerialName("id")
        val userId: Long,
        val name: String,
        val displayName: String,
        val previousUsernames: List<String>
    )

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
        val externalDisplayName: String? = null
    )
}