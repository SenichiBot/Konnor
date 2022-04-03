package me.hechfx.konnor.database.dao

import me.hechfx.konnor.database.table.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class User(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<User>(Users) {
        fun getOrInsert(id: Long): User {
            return User.findById(id) ?: User.new(id) {

            }
        }
    }

    val userId = this.id.value
    var bio by Users.bio
    var likes by Users.likes
    var color by Users.color
    var coins by Users.coins
    var pronoun by Users.pronoun
    var dailyTimeout by Users.dailyTimeout
    var premium by Users.premium
    var premiumType by Users.premiumType
    var premiumDuration by Users.premiumDuration
    var banned by Users.banned
    var banReason by Users.banReason
}