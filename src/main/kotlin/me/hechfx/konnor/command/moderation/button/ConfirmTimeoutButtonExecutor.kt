package me.hechfx.konnor.command.moderation.button

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.json.request.DMCreateRequest
import kotlinx.datetime.Instant
import me.hechfx.konnor.command.moderation.ModerationCommand
import me.hechfx.konnor.command.moderation.ModerationCommand.createConfirmationMessage
import me.hechfx.konnor.command.moderation.ModerationCommand.getPunishmentString
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants
import me.hechfx.konnor.util.GeneralUtils.arguments
import me.hechfx.konnor.util.GeneralUtils.locale
import me.hechfx.konnor.util.moderation.ModerationUtils
import me.hechfx.konnor.util.moderation.ModerationUtils.timeoutMember
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.GuildComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User

class ConfirmTimeoutButtonExecutor(val konnor: Konnor) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ConfirmTimeoutButtonExecutor::class, "confirm_timeout")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val splitData = data.split("\\")
        val author = splitData[0]
        val member = splitData[1]
        val reason = splitData[2]
        val duration = Instant.fromEpochMilliseconds(splitData[3].toLong())

        if (user.id != Snowflake(author)) return

        val punishmentString = getPunishmentString(ModerationCommand.Punishment.TIMEOUT)

        if (context is GuildComponentContext) {
            context.updateMessage {
                embed {
                    title = context.locale["command"]["moderation"]["incomingPunishment"].asText()
                    description = context.locale["command"]["moderation"]["punishmentConfirmation"].asText()
                        .arguments(punishmentString, member)

                    thumbnail {
                        url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                    }
                }
                components = mutableListOf()
            }

            try {
                context.timeoutMember(
                    konnor.client.rest,
                        Snowflake(member),
                        reason,
                        duration
                    )

                    val memberAsUser = konnor.retrieveUser(Snowflake(member))

                    try {
                        val dmRequest = DMCreateRequest(
                            memberAsUser.id
                        )

                        val dmChannel = konnor.client.rest.user.createDM(dmRequest)

                        konnor.client.rest.channel.createMessage(dmChannel.id) {
                            embed {
                                title = context.locale["command"]["moderation"]["timeout"]["dmEmbedTitle"].asText()
                                description = context.locale["command"]["moderation"]["timeout"]["dmEmbedDescription"].asText()
                                color = Constants.DEFAULT_COLOR

                                thumbnail {
                                    url = "https://cdn.discordapp.com/emojis/956131775338414130.png"
                                }

                                field {
                                    name = context.locale["command"]["moderation"]["timeout"]["dmEmbedServer"].asText()
                                    value = "${konnor.client.getGuild(context.guildId)?.name} (`${context.guildId}`)"
                                    inline = true
                                }

                                field {
                                    name = context.locale["command"]["moderation"]["timeout"]["dmEmbedReason"].asText()
                                    value = reason
                                    inline = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        ModerationCommand.logger.info { "Something has gone wrong... ${e.message}" }
                        e.printStackTrace()
                    }

                    context.sendEphemeralMessage {
                        content = context.locale["command"]["moderation"]["successfullyPunishment"].asText()
                            .arguments(memberAsUser.username, memberAsUser.discriminator, "${memberAsUser.id.value}")
                    }
                } catch (e: Exception) {
                    context.sendEphemeralMessage {
                        content = context.locale["command"]["somethingHasGoneWrong"].asText()
                            .arguments("${e.message}")
                    }
                }
        }
    }
}