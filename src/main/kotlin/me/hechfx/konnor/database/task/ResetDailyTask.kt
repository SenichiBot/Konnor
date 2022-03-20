package me.hechfx.konnor.database.task

import kotlinx.coroutines.runBlocking
import me.hechfx.konnor.database.table.Users
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ResetDailyTask: Runnable {
    override fun run() {
        runBlocking {
            startListening()
        }
    }

    private suspend fun startListening() {
        newSuspendedTransaction {
            Users.update({ Users.dailyTimeout greaterEq Instant.now() }) {
                it[dailyTimeout] = null
            }
        }
    }
}