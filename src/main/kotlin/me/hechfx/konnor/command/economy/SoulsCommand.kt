package me.hechfx.konnor.command.economy

import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object SoulsCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("souls", "Commands that have Souls involved.") {
        subcommand("check", "Check how many souls you have") {
            executor = SoulsCheckCommandExecutor
        }
    }
}

class SoulsCheckCommandExecutor(val konnor: Konnor): SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(SoulsCheckCommandExecutor::class) {
        object Options: ApplicationCommandOptions() {
            val user = optionalUser("user", "Check how many Souls a specific user have").register()
        }

        override val options = Options
    }
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.sender

        val profile: User = newSuspendedTransaction {
            User[user.id.value.toLong()]
        }

        if (user.id.value == context.sender.id.value) {
            context.sendMessage {
                embed {
                    title = "Your Wallet"
                    description = "You have **${profile.coins}** ${if (profile.coins == 1L) "Soul" else "Souls"}"
                    color = DEFAULT_COLOR

                    field {
                        name = "Daily"
                        inline = false
                        value = "You can get your daily: ${if (profile.dailyTimeout == null) "**Now**" else "<t:${profile.dailyTimeout?.epochSecond}:R>"}"
                    }
                }
            }
        } else {
            context.sendMessage {
                embed {
                    title = if (user.name.endsWith("s")) "${user.name}' Wallet" else "${user.name}'s Wallet"
                    color = DEFAULT_COLOR
                    if (profile.pronoun == null) {
                        description = "He has **${profile.coins}** ${if (profile.coins == 1L) "Soul" else "Souls"}"

                        footer {
                            text = "Using default pronoun \"He/Him\", if you want to change it you can use \"/profile pronoun\""
                        }
                    } else {
                        description = "${profile.pronoun!!.split("/")[0]} has **${profile.coins}** ${if (profile.coins == 1L) "Soul" else "Souls"}"
                    }
                }
            }
        }
    }
}