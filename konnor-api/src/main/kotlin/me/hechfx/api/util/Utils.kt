package me.hechfx.api.util

import io.ktor.client.*
import kotlinx.serialization.json.Json

object Utils {
    val http = HttpClient()
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
}