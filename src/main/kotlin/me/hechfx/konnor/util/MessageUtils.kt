package me.hechfx.konnor.util

import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.entities.messages.EditableMessage

object MessageUtils {
   suspend fun ApplicationCommandContext.reply(prefix: String? = null, input: String): EditableMessage {
       return this.sendMessage {
           content = if (prefix == null) {
               "\uD83D\uDD39 • $input"
           } else {
               "$prefix • $input"
           }
       }
   }
}