package me.hechfx.konnor.command.senichi

import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.toList
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.entities.messages.editMessage
import kotlin.time.DurationUnit

object SenichiCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("senichi", "Commands that involves the bot.") {
        subcommand("ping", "Check the bot's ping") {
            executor = BotPingCommandExecutor
        }

        subcommand("info", "Check the bot's information") {
            executor = BotInformationCommandExecutor
        }
    }
}

class BotPingCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(BotPingCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions()

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val ms = System.currentTimeMillis()
        val msg = context.sendMessage {
            content = "**API**: `...` | **BOT**: `${konnor.client.gateway.averagePing?.toLong(DurationUnit.MILLISECONDS)}ms`"
        }

        msg.editMessage {
            val diff = (System.currentTimeMillis() - ms)
            content = "**API**: `${diff}ms` | **BOT**: `${konnor.client.gateway.averagePing?.toLong(DurationUnit.MILLISECONDS)}ms`"
        }
    }
}

class BotInformationCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(BotInformationCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions()

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            embed {
                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957353684990304348.png"
                }
                color = DEFAULT_COLOR
                title = "Hello, I'm Sen'ichi!"
                description = "Hello, my name is 千一 (or if you prefer... Sen'ichi)! I'm a 17 year old gender fluid Canadian boy with Japanese descent, thus my name! ^^\nI really like cold foods, coffee (also caffeine addicted xD) and, for being tall (1,86m), I love playing basketball and volleyball! I also really enjoy helping people with their respective tasks. :3\n\nMy hobbies are listening to music (electronic, pop and Brazilian funk), dancing, programming and surfing when I go to the beach!"

                field {
                    name = "Servers"
                    value = "```${konnor.client.guilds.toList().size}```"
                    inline = true
                }

                field {
                    name = "Cached Users"
                    value = "```${konnor.client.guilds.flatMapMerge { it.members }.toList().size}```"
                    inline = true
                }

                field {
                    name = "JDK"
                    value = "```${Runtime.version()}```"
                    inline = true
                }
            }
        }
    }
}