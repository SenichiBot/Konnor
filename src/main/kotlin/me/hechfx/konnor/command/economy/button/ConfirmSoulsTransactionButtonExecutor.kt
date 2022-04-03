package me.hechfx.konnor.command.economy.button

import dev.kord.common.entity.Snowflake
import me.hechfx.konnor.command.economy.SoulsPayCommandExecutor
import me.hechfx.konnor.util.Constants
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ConfirmSoulsTransactionButtonExecutor: ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ConfirmSoulsTransactionButtonExecutor::class, "confirm_souls_transaction")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val splitData = data.split(":")

        if (user.id != Snowflake(splitData[0])) return

        val author = Snowflake(splitData[0])
        val target = Snowflake(splitData[1])
        val quantity = splitData[2].toLong()

        context.updateMessage {
            embed {
                thumbnail {
                    url = "https://cdn.discordapp.com/emojis/957371616864641034.png"
                }

                title = "Incoming Transaction"
                description = "You are about to transfer ${if (quantity == 1L) "$quantity Soul" else "$quantity Souls"} to <@${target.value}>.\n\n**Is that right?**"
            }

            components = mutableListOf()
        }

        val authorProfile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User[author.value.toLong()]
        }

        val targetProfile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User.findById(target.value.toLong())
        }

        if (authorProfile.coins < quantity) {
            context.sendMessage {
                content = "You don't have $quantity Souls to transfer!"
            }
            return
        }

        if (targetProfile == null) {
            newSuspendedTransaction {
                authorProfile.coins -= quantity

                me.hechfx.konnor.database.dao.User.new(target.value.toLong()) {
                    coins += quantity
                }
            }

            context.sendMessage {
                content = "You successfully transferred ${if (quantity == 1L) "$quantity Soul" else "$quantity Souls"} to <@${target.value.toLong()}>."
            }
        } else {
            newSuspendedTransaction {
                authorProfile.coins-= quantity
                targetProfile.coins += quantity
            }

            context.sendMessage {
                content = "You successfully transferred ${if (quantity == 1L) "$quantity Soul" else "$quantity Souls"} to <@${target.value.toLong()}>."
            }
        }
    }
}