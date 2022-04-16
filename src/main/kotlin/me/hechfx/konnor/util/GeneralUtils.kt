package me.hechfx.konnor.util

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.ConfigFactory
import dev.kord.common.Locale
import dev.kord.common.entity.DiscordGuild
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.application.ApplicationCommand
import dev.kord.rest.service.RestClient
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Logger
import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.GuildComponentContext

object GeneralUtils {
    val JSON = Json { ignoreUnknownKeys = true }
    val logger: Logger = Logger.getLogger("main")

    suspend fun GuildApplicationCommandContext.getGuild(rest: RestClient) = rest.guild.getGuild(this.guildId)

    fun String.arguments(vararg arguments: String): String {
        var result = this
        for ((i, e) in arguments.withIndex()) {
            result = result.replace("{${i}}", e)
        }
        return result
    }

    val ApplicationCommandContext.locale: JsonNode
    get() {
        val userLocale = this.discordInteraction.locale.value ?: Locale("en", "US")

        return Konnor.getTranslation(
            "${userLocale.language}-${userLocale.country?.lowercase()}"
        )
    }

    val GuildApplicationCommandContext.locale: JsonNode
    get() {
        val guildLocale = this.discordInteraction.guildLocale.value ?: Locale("en", "US")

        return Konnor.getTranslation(
            "${guildLocale.language}-${guildLocale.country?.lowercase()}"
        )
    }

    val ComponentContext.locale: JsonNode
    get() {
        val userLocale = this.discordInteraction.locale.value ?: Locale("en", "US")

        return Konnor.getTranslation(
            "${userLocale.language}-${userLocale.country?.lowercase()}"
        )
    }

    val GuildComponentContext.locale: JsonNode
    get() {
        val guildLocale = this.discordInteraction.guildLocale.value ?: Locale("en", "US")

        return Konnor.getTranslation(
            "${guildLocale.language}-${guildLocale.country?.lowercase()}"
        )
    }

    fun SlashCommandDeclarationBuilder.getSlashCommandLocalizedDescription(localeId: String, cmdCategory: CommandCategory): String = Konnor.getTranslation(localeId)["command"][cmdCategory.category]["description"].asText()

    fun SlashCommandDeclarationBuilder.getSubCommandLocalizedDescription(localeId: String, cmdCategory: CommandCategory, cmdName: String): String = Konnor.getTranslation(localeId)["command"][cmdCategory.category][cmdName]["description"].asText()

    enum class CommandCategory(val category: String) {
        DEV("development"),
        ECONOMY("economy"),
        MINECRAFT("minecraft"),
        MODERATION("moderation"),
        ROBLOX("roblox"),
        SENICHI("senichi"),
        SOCIAL("social"),
        UTIL("util")
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    inline fun <reified T> Hocon.decodeFromFile(file: File): T = decodeFromConfig(ConfigFactory.parseFile(file).resolve())

    class Counter(
        var users: MutableList<Snowflake>,
        var name: String
    ) {
        private val mutex = Mutex()

        suspend fun get() = mutex.withLock {
            users
        }

        suspend fun add(userId: Snowflake) = mutex.withLock {
            users.add(userId)
        }
    }
}