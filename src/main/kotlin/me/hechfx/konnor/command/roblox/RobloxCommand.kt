package me.hechfx.konnor.command.`fun`

import me.hechfx.api.roblox.RobloxInstance
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import java.time.Instant

object RobloxCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("roblox", "Roblox Commands") {
        subcommand("user", "Fetch user's information") {
            executor = RobloxUserCommandExecutor
        }
    }
}

class RobloxUserCommandExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(RobloxUserCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions() {
            val user = string("user", "Roblox user name")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val robloxInstance = RobloxInstance()
        val query = robloxInstance.searchUser(args[options.user]).data[0]
        val robloxUser = robloxInstance.fetchUser(query.userId)

        context.sendMessage {
            embed {
                color = DEFAULT_COLOR
                title = if (robloxUser.name.endsWith("s")) "${robloxUser.name}' roblox profile" else "${robloxUser.name}'s roblox profile"
                description = robloxUser.description

                field {
                    name = "Created At"
                    inline = true
                    value = "<t:${Instant.parse(robloxUser.createdAt).epochSecond}:F>"
                }

                field {
                    name = "Is Banned?"
                    inline = true
                    value = if (robloxUser.isBanned) "Yes" else "No"
                }
            }
        }
    }
}