package me.hechfx.konnor.command.economy.button

import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AcceptSoulsGamblingTransactionButtonExecutor(val konnor: Konnor): ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(AcceptSoulsGamblingTransactionButtonExecutor::class, "souls_gambling_accept")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val splitData = data.split(":")

        if (user.id != Snowflake(splitData[1])) return

        val author = Snowflake(splitData[2])
        val target = Snowflake(splitData[1])
        val quantity = splitData[0].toLong()

        context.updateMessage {
            content = "<@${target.value}>"
            embed {
                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                }

                title = "Gambling Souls"
                description = "<@${author.value}> wanna bet $quantity Souls with you!\n\n**Will you accept?**"
            }

            components = mutableListOf()
        }

        val authorProfile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User.getOrInsert(author.value.toLong())
        }

        val targetProfile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User.getOrInsert(target.value.toLong())
        }

        if (authorProfile.coins < quantity) {
            context.sendMessage {
                content = "You don't have Souls enough to bet."
            }
            return
        }

        if (targetProfile.coins < quantity) {
            context.sendMessage {
                content = "<@${target.value}> does not have enough Souls to bet."
            }
            return
        }

        newSuspendedTransaction {
            val authorDice = Konnor.random.nextInt(1, 100)
            val targetDice = Konnor.random.nextInt(1, 100)

            val targetUser = konnor.retrieveUser(target)
            val authorUser = konnor.retrieveUser(author)

            if (authorDice > targetDice) {
                targetProfile.coins -= quantity
                authorProfile.coins += quantity

                context.sendMessage {
                    embed {
                        title = if (authorUser.username.endsWith("s")) "${authorUser.username}' Congratulations" else "${authorUser.username}'s Congratulations"
                        description = "You have won $quantity Souls from <@${target.value}>."

                        field {
                            name = "Your Dice"
                            value = "`$authorDice`"
                        }
                        field {
                            name = if (targetUser.username.endsWith("s")) "${targetUser.username}' Dice" else "${targetUser.username}'s Dice"
                            value = "`$targetDice`"
                        }
                    }
                }
            } else if (targetDice > authorDice) {
                targetProfile.coins += quantity
                authorProfile.coins -= quantity

                context.sendMessage {
                    embed {
                        title = if (targetUser.username.endsWith("s")) "${targetUser.username}' Congratulations" else "${targetUser.username}'s Congratulations"
                        description = "You have won $quantity Souls from <@${author.value}>."

                        field {
                            name = "Your Dice"
                            value = "`$targetDice`"
                        }
                        field {
                            name = if (authorUser.username.endsWith("s")) "${authorUser.username}' Dice" else "${authorUser.username}'s Dice"
                            value = "`$authorDice`"
                        }
                    }
                }
            } else {
                context.sendMessage {
                    embed {
                        title = "Draw"
                        description = "No one has won $quantity Souls."

                        field {
                            name = if (authorUser.username.endsWith("s")) "${authorUser.username}' Dice" else "${authorUser.username}'s Dice"
                            value = "`$authorDice`"
                        }
                        field {
                            name = if (targetUser.username.endsWith("s")) "${targetUser.username}' Dice" else "${targetUser.username}'s Dice"
                            value = "`$targetDice`"
                        }
                    }
                }
            }
        }
    }
}