package me.hechfx.konnor.database.table

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object Users: LongIdTable() {
    var bio = text("bio")
    var likes = integer("likes")
    var color = text("color")
    var backgroundUrl = text("background-url").nullable()
    var coins = long("coins")
    var pronoun = text("pronoun").nullable()
    var dailyTimeout = timestampWithTimeZone("daily-timeout").nullable()
    var premium = bool("premium")
    var premiumType = integer("premium-type").nullable()
    var premiumDuration = timestampWithTimeZone("premium-duration").nullable()
    var banned = bool("banned")
    var banReason = text("ban-reason").nullable()
}