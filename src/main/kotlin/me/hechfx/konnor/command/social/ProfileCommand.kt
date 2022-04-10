package me.hechfx.konnor.command.social

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.util.Constants.buildBadges
import me.hechfx.konnor.command.social.button.ChangeAboutMeButtonExecutor
import me.hechfx.konnor.command.social.menu.PronounsMenuExecutor
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.components.interactiveButton
import net.perfectdreams.discordinteraktions.common.components.selectMenu
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object ProfileCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("profile", "Profile Command") {
        subcommand("check", "Check your profile.") {
            executor = ProfileCheckCommandExecutor
        }

        subcommand("pronoun", "Check or change your in-bot pronoun") {
            executor = ProfilePronounCommandExecutor
        }
    }
}

class ProfileCheckCommandExecutor(val konnor: Konnor): SlashCommandExecutor() {
    companion object: SlashCommandExecutorDeclaration(ProfileCheckCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions() {
            val user = optionalUser("user", "Check someone's profile.")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.sender

        val profile = newSuspendedTransaction {
            User.getOrInsert(user.id.value.toLong())
        }

        context.sendMessage {
            embed {
                title = if (user.name.endsWith("s")) "${user.name}' Profile" else "${user.name}'s Profile"
                description = profile.bio

                val profileColor = java.awt.Color.decode(profile.color)

                color = if (profileColor == null) {
                    Color(0, 0, 0)
                } else {
                    Color(profileColor.red, profileColor.green, profileColor.blue)
                }

                if (profile.premium && profile.premiumType != null && profile.premiumDuration != null) {
                    field {
                        name = when (profile.premiumType) {
                            1 -> "VIP"
                            2 -> "VIP+"
                            3 -> "VIP++"
                            else -> ""
                        }
                        inline = true
                        value = "Vip will end: <t:${profile.premiumDuration!!.epochSecond}:R>"
                    }
                }

                val badges = buildBadges(user, konnor.client.rest)

                if (badges != null) {
                    field {
                        name = "Badges"
                        value = badges
                        inline = true
                    }
                }

                field {
                    name = "Pronouns"
                    inline = true
                    value = profile.pronoun ?: "He/Him"
                }

                field {
                    name = "Souls"
                    inline = true
                    value = if (profile.coins == 1L) "${profile.coins} Soul" else "${profile.coins} Souls"
                }

                thumbnail {
                    url = user.avatar.cdnUrl.toUrl()
                }
            }

            if (user.id.value == context.sender.id.value) {
                actionRow {
                    interactiveButton(ButtonStyle.Primary, ChangeAboutMeButtonExecutor, context.sender.id.value.toString()) {
                        label = "Change about me"
                    }
                }
            }
        }
    }
}

class ProfilePronounCommandExecutor(val konnor: Konnor): SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ProfilePronounCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions()

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        context.sendMessage {
            embed {
                title = "Current Pronoun"
                description = "`${profile.pronoun ?: "Your pronoun has not yet been defined."}`"
                color = DEFAULT_COLOR

                field {
                    name = "Available Pronouns"
                    inline = false
                    value = "He/Him\nShe/Her\nThey/Them"
                }

                footer {
                    text = "Select your pronoun on the select menu below."
                }

                actionRow {
                    selectMenu(PronounsMenuExecutor, context.sender.id.value.toString()) {
                        option("He/Him", "he_him")
                        option("She/Her", "she_her")
                        option("They/Them", "they_them")
                    }
                }
            }
        }
    }
}