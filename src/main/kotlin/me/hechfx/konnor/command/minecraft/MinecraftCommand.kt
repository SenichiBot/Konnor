package me.hechfx.konnor.command.minecraft

import me.hechfx.api.sra.SRAInstance
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

object MinecraftCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("minecraft", "Minecraft Commands") {
        subcommand("user", "Check a minecraft user's information [Premium-Only]") {
            executor = MinecraftUserCommandExecutor
        }

        subcommand("server", "Check a minecraft server's information") {
            executor = MinecraftServerCommandExecutor
        }
    }
}

val sraInstance = SRAInstance()

class MinecraftUserCommandExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(MinecraftUserCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val user = string("user", "minecraft username")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = sraInstance.retrieveMCUser(args[options.user])

        if (user == null) {
            context.sendMessage {
                content = "Invalid user."
            }
        } else {
            context.sendMessage {
                embed {
                    title = if (user.username.endsWith("s")) "${user.username}' information" else "${user.username}'s information"
                    color = DEFAULT_COLOR
                    field {
                        name = "UUID"
                        inline = false
                        value = user.uuid
                    }

                    field {
                        name = "Name History"
                        inline = false
                        value = user.nameHistory.joinToString(", ") { "${it.name} — ${it.changedAt}" }
                    }
                }
            }
        }
    }
}

class MinecraftServerCommandExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(MinecraftServerCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val ip = string("ip", "minecraft server ip")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val server = sraInstance.retrieveMCServer(args[options.ip])

        if (server == null) {
            context.sendMessage {
                content = "Cannot fetch the minecraft server, probably it's offline."
            }
        } else {
            context.sendMessage {
                embed {
                    color = DEFAULT_COLOR
                    title = args[options.ip]
                    description = server.description
                    thumbnail {
                        url = server.favicon
                    }
                    field {
                        name = "Versions"
                        inline = true
                        value = server.version.name
                    }
                    field {
                        name = "Players"
                        inline = true
                        value = "${server.players.online}/${server.players.max}"
                    }
                }
            }
        }
    }
}