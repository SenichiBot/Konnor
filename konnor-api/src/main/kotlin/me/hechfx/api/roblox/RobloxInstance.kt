package me.hechfx.api.roblox

import io.ktor.client.*
import io.ktor.client.request.*
import me.hechfx.api.roblox.structure.SearchRobloxUserResponse
import me.hechfx.api.roblox.structure.entity.RobloxUser

class RobloxInstance {
    companion object {
        private const val usersEndpoint = "https://users.roblox.com/v1/users/"
        private val http = HttpClient()
    }

    suspend fun fetchUser(userId: Long): RobloxUser {
        return http.get("$usersEndpoint/$userId")
    }

    suspend fun searchUser(query: String, limit: Int = 10): SearchRobloxUserResponse {
        return http.get("$usersEndpoint/search") {
            parameter("keyword", query)
            parameter("limit", limit)
        }
    }
}