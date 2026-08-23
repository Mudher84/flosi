package com.flosi.app

import android.app.Application
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.settings.FlosiPreferences
import com.flosi.app.worker.FlosiWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlosiApplication : Application() {
    lateinit var database: FlosiDatabase
        private set
    lateinit var repository: FinanceRepository
        private set
    lateinit var preferences: FlosiPreferences
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = FlosiDatabase.get(this)
        preferences = FlosiPreferences(this)
        repository = FinanceRepository(database,preferences)

        appScope.launch { repository.seedIfEmpty() }
        FlosiWorkScheduler.ensureScheduled(this)
    }
}
