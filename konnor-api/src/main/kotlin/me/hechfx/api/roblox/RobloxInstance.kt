package me.hechfx.api.roblox

import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import me.hechfx.api.roblox.response.SearchRobloxUserResponse
import me.hechfx.api.util.Utils.http
import me.hechfx.api.util.Utils.json

class RobloxInstance {
    companion object {
        private const val usersEndpoint = "https://users.roblox.com/v1/users/"
    }

    suspend fun fetchUser(userId: Long): SearchRobloxUserResponse.RobloxUser {
        val retrievedContentAsString: String = http.get("$usersEndpoint/$userId")
        return json.decodeFromString(retrievedContentAsString)
    }

    suspend fun searchUser(query: String, limit: Int = 10): SearchRobloxUserResponse {
        val retrievedContentAsString: String = http.get("$usersEndpoint/search") {
            parameter("keyword", query)
            parameter("limit", limit)
        }

        return json.decodeFromString(retrievedContentAsString)
    }
}