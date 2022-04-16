package me.hechfx.konnor.util.moderation

import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.rest.json.response.BanResponse
import dev.kord.rest.service.RestClient
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.hechfx.konnor.util.moderation.ModerationUtils.banMember
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.components.GuildComponentContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object ModerationUtils {
    suspend fun GuildApplicationCommandContext.retrieveBanInformation(rest: RestClient, member: Snowflake): BanResponse {
        return rest.guild.getGuildBan(this.guildId, member)
    }

    suspend fun GuildApplicationCommandContext.unbanMember(rest: RestClient, member: Snowflake) {
        return rest.guild.deleteGuildBan(this.guildId, member)
    }

    suspend fun GuildApplicationCommandContext.banMember(rest: RestClient, member: Snowflake, reason: String, deleteMessages: Boolean? = false) {
        val author = this@banMember.sender

        return rest.guild.addGuildBan(this.guildId, member) {
            this.reason = "Banned by ${author.name}#${author.discriminator} » $reason"
            if (deleteMessages == true) {
                deleteMessagesDays = 7
            }
        }
    }

    suspend fun GuildComponentContext.banMember(rest: RestClient, member: Snowflake, reason: String, deleteMessages: Boolean? = false) {
        val author = this@banMember.sender

        return rest.guild.addGuildBan(this.guildId, member) {
            this.reason = "Banned by ${author.name}#${author.discriminator} » $reason"
            if (deleteMessages == true) {
                deleteMessagesDays = 7
            }
        }
    }

    suspend fun GuildApplicationCommandContext.timeoutMember(rest: RestClient, member: Snowflake, reason: String, duration: Instant): DiscordGuildMember {
        val author = this@timeoutMember.sender

        return rest.guild.modifyGuildMember(this.guildId, member) {
            communicationDisabledUntil = duration
            this.reason = "Punished by ${author.name}#${author.discriminator} » $reason"
        }
    }

    suspend fun GuildComponentContext.timeoutMember(rest: RestClient, member: Snowflake, reason: String, duration: Instant): DiscordGuildMember {
        val author = this@timeoutMember.sender

        return rest.guild.modifyGuildMember(this.guildId, member) {
            communicationDisabledUntil = duration
            this.reason = "Punished by ${author.name}#${author.discriminator} » $reason"
        }
    }
}