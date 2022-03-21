package me.hechfx.api.sra

import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import me.hechfx.api.sra.response.MinecraftServerResponse
import me.hechfx.api.sra.response.MinecraftUserResponse
import me.hechfx.api.util.Utils.http
import me.hechfx.api.util.Utils.json

class SRAInstance {
    companion object {
        private const val baseUrl = "https://some-random-api.ml"
        private const val other = "https://eu.mc-api.net/v3/server/ping"
    }

    suspend fun retrieveMCUser(username: String): MinecraftUserResponse? {
        return try {
            val contentAsString: String = http.get("$baseUrl/mc") {
                parameter("username", username)
            }

            json.decodeFromString(contentAsString)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun retrieveMCServer(ip: String): MinecraftServerResponse? {
        return try {
            val contentAsString: String = http.get("$other/$ip")

            json.decodeFromString(contentAsString)
        } catch (e: Exception) {
            null
        }
    }
}