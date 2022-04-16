package me.hechfx.konnor.command.moderation

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.json.request.DMCreateRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.hechfx.konnor.command.moderation.ModerationCommand.createConfirmationMessage
import me.hechfx.konnor.command.moderation.ModerationCommand.logger
import me.hechfx.konnor.command.moderation.button.ConfirmBanButtonExecutor
import me.hechfx.konnor.command.moderation.button.ConfirmTimeoutButtonExecutor
import me.hechfx.konnor.command.moderation.button.DenyPunishmentButtonExecutor
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.util.GeneralUtils
import me.hechfx.konnor.util.GeneralUtils.arguments
import me.hechfx.konnor.util.GeneralUtils.getSlashCommandLocalizedDescription
import me.hechfx.konnor.util.GeneralUtils.getSubCommandLocalizedDescription
import me.hechfx.konnor.util.GeneralUtils.locale
import me.hechfx.konnor.util.moderation.ModerationUtils
import me.hechfx.konnor.util.moderation.ModerationUtils.banMember
import me.hechfx.konnor.util.moderation.ModerationUtils.timeoutMember
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.discordinteraktions.common.components.interactiveButton
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object ModerationCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("moderation", "Moderation Commands") {
        descriptionLocalizations = mapOf(
            Locale.PORTUGUESE_BRAZIL to getSlashCommandLocalizedDescription("pt-br", GeneralUtils.CommandCategory.MODERATION),
            Locale.ENGLISH_UNITED_STATES to getSlashCommandLocalizedDescription("en-us", GeneralUtils.CommandCategory.MODERATION)
        )
        subcommand("ban", "Bans a member") {
            descriptionLocalizations = mapOf(
                Locale.ENGLISH_UNITED_STATES to getSubCommandLocalizedDescription("en-us", GeneralUtils.CommandCategory.MODERATION, "ban"),
                Locale.PORTUGUESE_BRAZIL to getSubCommandLocalizedDescription("en-us", GeneralUtils.CommandCategory.MODERATION, "ban")
            )

            executor = ModerationBanCommandExecutor
        }

        subcommand("timeout", "Timeout a member") {
            descriptionLocalizations = mapOf(
                Locale.ENGLISH_UNITED_STATES to getSubCommandLocalizedDescription("en-us", GeneralUtils.CommandCategory.MODERATION, "timeout"),
                Locale.PORTUGUESE_BRAZIL to getSubCommandLocalizedDescription("en-us", GeneralUtils.CommandCategory.MODERATION, "timeout")
            )
            executor = ModerationTimeoutCommandExecutor
        }
    }

    val logger = KotlinLogging.logger {}

    enum class Punishment(val type: String) {
        BAN("BAN"),
        TIMEOUT("TIMEOUT"),
        UNBAN("UNBAN"),
        KICK("KICK")
    }

    fun getPunishmentString(type: Punishment): String {
        return when (type) {
            Punishment.BAN -> "ban"
            Punishment.KICK -> "kick"
            Punishment.TIMEOUT -> "timeout"
            Punishment.UNBAN -> "unban"
        }
    }

    suspend fun GuildApplicationCommandContext.createConfirmationMessage(
        member: net.perfectdreams.discordinteraktions.common.entities.User,
        reason: String,
        punishmentType: Punishment,
        deleteMessages: Boolean? = false, // Only for ban punishment type
        duration: Instant? = null, // Only for timeout punishment type
    ) {

        val context = this

        val punishmentString = when (punishmentType) {
            Punishment.BAN -> context.locale["command"]["moderation"]["banType"].asText()
            Punishment.KICK -> context.locale["command"]["moderation"]["kickType"].asText()
            Punishment.TIMEOUT -> context.locale["command"]["moderation"]["timeoutType"].asText()
            Punishment.UNBAN -> context.locale["command"]["moderation"]["unbanType"].asText()
        }

        this.sendEphemeralMessage {
            embed {
                title = context.locale["command"]["moderation"]["incomingPunishment"].asText()
                description = context.locale["command"]["moderation"]["punishmentConfirmation"].asText()
                    .arguments(punishmentString, member.id.value.toString())
                color = DEFAULT_COLOR

                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                }
            }

            when (punishmentType) {
                Punishment.BAN -> {
                    actionRow {
                        interactiveButton(ButtonStyle.Success, ConfirmBanButtonExecutor, "${this@createConfirmationMessage.sender.id.value}\\${member.id.value}\\${reason}\\${deleteMessages}") {
                            label = context.locale["command"]["moderation"]["button"]["yes"].asText()
                        }

                        interactiveButton(ButtonStyle.Danger, DenyPunishmentButtonExecutor, "${this@createConfirmationMessage.sender.id.value}") {
                            label = context.locale["command"]["moderation"]["button"]["no"].asText()
                        }
                    }
                }

                Punishment.TIMEOUT -> {
                    actionRow {
                        interactiveButton(ButtonStyle.Success, ConfirmTimeoutButtonExecutor, "${this@createConfirmationMessage.sender.id.value}\\${member.id.value}\\${reason}\\${duration?.toEpochMilliseconds()}") {
                            label = context.locale["command"]["moderation"]["button"]["yes"].asText()
                        }

                        interactiveButton(ButtonStyle.Danger, DenyPunishmentButtonExecutor, "${this@createConfirmationMessage.sender.id.value}") {
                            label = context.locale["command"]["moderation"]["button"]["no"].asText()
                        }
                    }
                }
            }
        }
    }
}

class ModerationBanCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ModerationBanCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val member = user("member", "a guild member")
                .register()
            val reason = string("reason", "reason to ban the member")
                .register()
            val force = optionalBoolean("force_ban", "forces the ban without confirmation")
                .register()
            val deleteMessages = optionalBoolean("delete_messages", "delete the message when the member is banned")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context is GuildApplicationCommandContext) {
            val self = konnor.client.getGuild(context.guildId)?.getMember(konnor.client.selfId)

            if (!context.member.permissions.contains(Permission.BanMembers)) {
                context.sendEphemeralMessage {
                    content = context.locale["commands"]["moderation"]["youDontHavePermission"].asText()
                }
                return
            }

            if (self?.getPermissions()?.contains(Permission.BanMembers) == false) {
                context.sendEphemeralMessage {
                    content = context.locale["commands"]["moderation"]["iDontHavePermission"].asText()
                        .arguments("Ban Members")
                }
                return
            }

            if (args[options.force] == true) {
                try {
                    context.banMember(
                        konnor.client.rest,
                        args[options.member].id,
                        args[options.reason],
                        args[options.deleteMessages]
                    )

                    try {
                        val dmRequest = DMCreateRequest(
                            args[options.member].id
                        )

                        val dmChannel = konnor.client.rest.user.createDM(dmRequest)

                        konnor.client.rest.channel.createMessage(dmChannel.id) {
                            embed {
                                title = context.locale["command"]["moderation"]["ban"]["dmEmbedTitle"].asText()
                                description = context.locale["command"]["moderation"]["ban"]["dmEmbedDescription"].asText()
                                color = DEFAULT_COLOR

                                thumbnail {
                                    url = "https://cdn.discordapp.com/emojis/956131775338414130.png"
                                }

                                field {
                                    name = context.locale["command"]["moderation"]["ban"]["dmEmbedServer"].asText()
                                    value = "${konnor.client.getGuild(context.guildId)?.name} (`${context.guildId}`)"
                                    inline = true
                                }

                                field {
                                    name = context.locale["command"]["moderation"]["ban"]["dmEmbedReason"].asText()
                                    value = args[options.reason]
                                    inline = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.info { "Something has gone wrong... ${e.message}" }
                        e.printStackTrace()
                    }

                    context.sendEphemeralMessage {
                        content = context.locale["command"]["moderation"]["successfullyPunishment"].asText()
                            .replace("{0}", args[options.member].name)
                            .replace("{1}", args[options.member].discriminator)
                            .replace("{2}", "${args[options.member].id.value}")
                    }
                } catch (e: Exception) {
                    context.sendEphemeralMessage {
                        content = context.locale["command"]["somethingHasGoneWrong"].asText()
                            .replace("{0}", "${e.message}")
                    }
                }
            } else {
                context.createConfirmationMessage(
                    args[options.member],
                    args[options.reason],
                    ModerationCommand.Punishment.BAN,
                    args[options.deleteMessages]
                )
            }
        }
    }
}

class ModerationTimeoutCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {

    companion object : SlashCommandExecutorDeclaration(ModerationTimeoutCommandExecutor::class) {

        object CommandOptions : ApplicationCommandOptions() {
            val member = user("member", "member that will be punished")
                .register()
            val selectedTime = string("time", "time that the member will be timeout (e.g: 1 hour, 10 minutes or etc)")
                .register()
            val reason = string("reason", "the reason that member was timeout")
                .register()
            val force = optionalBoolean("force_timeout", "forces the timeout without confirmation")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val splitDuration = args[options.selectedTime].split(" ")
        val parsedDuration = when (splitDuration[1]) {
            "dias", "dia", "day", "days" -> Clock.System.now() + splitDuration[0].toInt().days
            "horas", "hours", "hora", "hour", "h" -> Clock.System.now() + splitDuration[0].toInt().hours
            "minutos", "minuto", "minutes", "minute", "m" -> Clock.System.now() + splitDuration[0].toInt().minutes
            "segundos", "segundo", "seconds", "second", "s" -> Clock.System.now() + splitDuration[0].toInt().seconds
            else -> null
        }

        if (context is GuildApplicationCommandContext) {
            val self = konnor.client.getGuild(context.guildId)?.getMember(konnor.client.selfId)

            if (!context.member.permissions.contains(Permission.ModerateMembers)) {
                context.sendEphemeralMessage {
                    content = context.locale["commands"]["moderation"]["youDontHavePermission"].asText()
                }
                return
            }

            if (self?.getPermissions()?.contains(Permission.ModerateMembers) == false) {
                context.sendEphemeralMessage {
                    content = context.locale["commands"]["moderation"]["iDontHavePermission"].asText()
                        .arguments("Moderate Members")
                }
                return
            }

            if (parsedDuration == null) {
                context.sendEphemeralMessage {
                    content = context.locale["commands"]["moderation"]["timeout"]["durationError"].asText()
                }
                return
            }

            if (args[options.force] == true) {
                try {
                    context.timeoutMember(
                        konnor.client.rest,
                        args[options.member].id,
                        args[options.reason],
                        parsedDuration,
                    )

                    try {
                        val dmRequest = DMCreateRequest(
                            args[options.member].id
                        )

                        val dmChannel = konnor.client.rest.user.createDM(dmRequest)

                        konnor.client.rest.channel.createMessage(dmChannel.id) {
                            embed {
                                title = context.locale["commands"]["moderation"]["timeout"]["dmEmbedTitle"].asText()
                                description = context.locale["commands"]["moderation"]["timeout"]["dmEmbedDescription"].asText()
                                color = DEFAULT_COLOR

                                thumbnail {
                                    url = "https://cdn.discordapp.com/emojis/956131775338414130.png"
                                }

                                field {
                                    name = context.locale["commands"]["moderation"]["timeout"]["dmEmbedServer"].asText()
                                    value = "${konnor.client.getGuild(context.guildId)?.name} (`${context.guildId}`)"
                                    inline = true
                                }

                                field {
                                    name = context.locale["commands"]["moderation"]["timeout"]["dmEmbedReason"].asText()
                                    value = args[ModerationBanCommandExecutor.options.reason]
                                    inline = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.info { "Something has gone wrong... ${e.message}" }
                        e.printStackTrace()
                    }

                    context.sendEphemeralMessage {
                        content = context.locale["command"]["moderation"]["successfullyPunishment"].asText()
                            .arguments(args[options.member].name, args[options.member].discriminator, "${args[options.member].id.value}")
                    }
                } catch (e: Exception) {
                    context.sendEphemeralMessage {
                        content = context.locale["commands"]["somethingHasGoneWrong"].asText()
                            .arguments("${e.message}")
                    }
                }
            } else {
                context.createConfirmationMessage(
                    args[options.member],
                    args[options.reason],
                    ModerationCommand.Punishment.TIMEOUT,
                    duration = parsedDuration
                )
            }
        }
    }
}