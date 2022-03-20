package me.hechfx.konnor.command.social.modal

import me.hechfx.konnor.database.table.Users
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitContext
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutor
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class SubmitAboutMeChangesModalExecutor : ModalSubmitExecutor {
    companion object : ModalSubmitExecutorDeclaration(SubmitAboutMeChangesModalExecutor::class, "submit_about_me") {
        object ModalOptions : ModalComponents() {
            val bio = textInput("bio")
                .register()
        }

        override val options = ModalOptions
    }

    override suspend fun onModalSubmit(context: ModalSubmitContext, args: ModalArguments) {
        newSuspendedTransaction {
            Users.update({ Users.id eq context.sender.id.value.toLong() }) {
                it[bio] = args[options.bio]
            }
        }

        context.sendEphemeralMessage {
            content = "Successfully changed your about me to: `${args[options.bio]}`"
        }
    }
}