package me.hechfx.konnor.command.economy

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import me.hechfx.konnor.command.economy.button.AcceptSoulsGamblingTransactionButtonExecutor
import me.hechfx.konnor.command.economy.button.ConfirmSoulsTransactionButtonExecutor
import me.hechfx.konnor.command.economy.button.DenySoulsTransactionButtonExecutor
import me.hechfx.konnor.command.economy.button.GetDailyButtonExecutor
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.database.table.Users
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.components.interactiveButton
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object SoulsCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("souls", "Commands that have Souls involved.") {
        subcommand("check", "Check how many Souls you have") {
            executor = SoulsCheckCommandExecutor
        }

        subcommand("pay", "Pay or donate to someone with Souls") {
            executor = SoulsPayCommandExecutor
        }

        subcommand("top", "See users' Soul ranking") {
            executor = SoulsTopCommandExecutor
        }

        subcommand("daily", "Get your daily Souls") {
            executor = SoulsDailyCommandExecutor
        }

        subcommand("bet", "Bet some Souls with a user") {
            executor = SoulsBetCommandExecutor
        }
    }
}

class SoulsCheckCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
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
                        value = "You can get your daily: ${if (profile.dailyTimeout == null) "**Now**" else "<t:${profile.dailyTimeout!!.epochSecond}:R>"}"
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

class SoulsPayCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(SoulsPayCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val user = user("user", "user to pay or donate souls")
                .register()
            val quantity = integer("quantity", "how many souls you want to pay or donate")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val authorProfile = newSuspendedTransaction {
            User[context.sender.id.value.toLong()]
        }

        if (authorProfile.coins < args[options.quantity]) {
            context.sendMessage {
                content = "You don't have ${args[options.quantity]} Souls to transfer!"
            }
            return
        }

        context.sendMessage {
            embed {
                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                }

                color = DEFAULT_COLOR
                title = "Incoming Transaction"
                description = "You want to transfer ${if (args[options.quantity] == 1L) "${args[options.quantity]} Soul" else "${args[options.quantity]} Souls"} to <@${args[options.user].id.value}>\n\n**Is that right?**"
            }

            actionRow {
                interactiveButton(ButtonStyle.Success, ConfirmSoulsTransactionButtonExecutor, "${context.sender.id.value}:${args[options.user].id.value}:${args[options.quantity]}") {
                    label = "Yes"
                }

                interactiveButton(ButtonStyle.Danger, DenySoulsTransactionButtonExecutor, "${context.sender.id.value}") {
                    label = "No"
                }
            }
        }
    }
}

class SoulsTopCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(SoulsTopCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val page = optionalInteger("page", "select a page to show")
                .register()
        }

        override val options = CommandOptions
    }
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        var pg = args[options.page]

        if (pg == null) {
            pg = 0
        } else {
            pg -= 1
        }

        context.deferChannelMessage()

        val users = newSuspendedTransaction {
            Users.selectAll()
                .orderBy(Users.coins, SortOrder.DESC)
                .limit(10, pg * 10)
                .toMutableList()
        }

        var string = ""

        for (e in users) {
            string += "\uD83D\uDD38 | ${konnor.retrieveUser(Snowflake(e[Users.id].value.toString())).username}#${konnor.retrieveUser(Snowflake(e[Users.id].value.toString())).discriminator} (`${e[Users.id].value}`) — ${if (e[Users.coins] == 1L) "${e[Users.coins]} Soul" else "${e[Users.coins]} Souls"}\n"
        }

        context.sendMessage {
            embed {
                title = "Souls Ranking"
                description = string
                color = DEFAULT_COLOR
            }
        }
    }
}

class SoulsDailyCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(SoulsDailyCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions()

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val authorProfile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        context.sendMessage {
            embed {
                color = DEFAULT_COLOR
                title = "Your Daily Status"
                description = "You can get your daily: ${if (authorProfile.dailyTimeout == null || Clock.System.now().toEpochMilliseconds() >= authorProfile.dailyTimeout!!.toEpochMilli()) "**Now**" else "<t:${authorProfile.dailyTimeout!!.epochSecond}:R>"}"
                if (authorProfile.premium && authorProfile.premiumType != null) {
                    field {
                        name = when (authorProfile.premiumType!!) {
                            1 -> "VIP"
                            2 -> "VIP+"
                            3 -> "VIP++"
                            else -> { "None" }
                        }
                        value = "You can get more Souls in the daily because of your VIP!"
                    }
                }
            }

            actionRow {
                if (authorProfile.dailyTimeout != null && Clock.System.now().toEpochMilliseconds() <= authorProfile.dailyTimeout!!.toEpochMilli()) {
                    interactiveButton(ButtonStyle.Success, GetDailyButtonExecutor, "${context.sender.id.value}") {
                        label = "Get Daily"
                        disabled = true
                    }
                } else if (authorProfile.dailyTimeout == null || (authorProfile.dailyTimeout != null && Clock.System.now().toEpochMilliseconds() >= authorProfile.dailyTimeout!!.toEpochMilli())){
                    interactiveButton(ButtonStyle.Success, GetDailyButtonExecutor, "${context.sender.id.value}") {
                        label = "Get Daily"
                    }
                }
            }
        }
    }
}

class SoulsBetCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(SoulsBetCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val target = user("user", "user that you want to bet with")
                .register()
            val amount = integer("amount", "amount of souls that you want to bet")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val authorProfile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        val targetProfile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        if (authorProfile.coins < args[options.amount]) {
            context.sendMessage {
                content = "You don't have Souls enough to bet."
            }
            return
        }

        if (targetProfile.coins < args[options.amount]) {
            context.sendMessage {
                content = "<@${args[options.target].id.value}> does not have enough Souls to bet."
            }
            return
        }

        context.sendMessage {
            content = "<@${args[options.target].id.value}>"
            embed {
                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                }

                color = DEFAULT_COLOR
                title = "Gambling Souls"
                description = "<@${context.sender.id.value}> wanna bet ${args[options.amount]} Souls with you!\n\n**Will you accept?**"
            }
            actionRow {
                interactiveButton(ButtonStyle.Success, AcceptSoulsGamblingTransactionButtonExecutor, "${args[options.amount]}:${args[options.target].id.value}:${context.sender.id.value}") {
                    label = "Yes"
                }
                interactiveButton(ButtonStyle.Danger, DenySoulsTransactionButtonExecutor, "${args[options.target].id.value}") {
                    label = "No"
                }
            }
        }
    }
}