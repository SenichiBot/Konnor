package me.hechfx.konnor.database.task

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.hechfx.konnor.database.table.Users
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class ResetDailyTask: Runnable {
    override fun run() {
        GlobalScope.launch {
            startListening()
        }
    }

    private suspend fun startListening() {
        newSuspendedTransaction {

        }
    }
}