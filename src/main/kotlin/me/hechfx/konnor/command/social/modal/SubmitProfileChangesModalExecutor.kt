package me.hechfx.konnor.command.social.modal

import dev.kord.common.entity.optional.optional
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.database.table.Users
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitContext
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutor
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.net.URL

class SubmitProfileChangesModalExecutor : ModalSubmitExecutor {
    companion object : ModalSubmitExecutorDeclaration(SubmitProfileChangesModalExecutor::class, "submit_about_me") {
        object ModalOptions : ModalComponents() {
            val bio = textInput("bio")
                .register()
            val color = textInput("color")
                .register()
            val backgroundUrl = textInput("background_url")
                .register()
        }

        override val options = ModalOptions
    }

    override suspend fun onModalSubmit(context: ModalSubmitContext, args: ModalArguments) {
        val u = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        newSuspendedTransaction {
            Users.update({ Users.id eq context.sender.id.value.toLong() }) {
                it[bio] = args[options.bio]
            }

            if (u.premium) {
                Users.update({ Users.id eq context.sender.id.value.toLong() }) {
                    it[color] = args[options.color]
                }

                if (isValidUrl(args[options.backgroundUrl])) {
                    Users.update({ Users.id eq context.sender.id.value.toLong() }) {
                        it[backgroundUrl] = args[options.backgroundUrl]
                    }
                } else {
                    Users.update({ Users.id eq context.sender.id.value.toLong() }) {
                        it[backgroundUrl] = null
                    }
                }
            }
        }

        context.sendEphemeralMessage {
            content = "Successfully updated changes."
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
}