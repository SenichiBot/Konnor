package me.hechfx.api.roblox.structure

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRobloxUserResponse(
    @SerialName("previousPageCursor")
    val previousPage: String,
    @SerialName("nextPageCursor")
    val nextPage: String,
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
}