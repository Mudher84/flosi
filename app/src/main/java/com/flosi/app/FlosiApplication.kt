package com.flosi.app

import android.app.Application
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.settings.FlosiPreferences
import com.flosi.app.worker.FlosiWorkScheduler

class FlosiApplication : Application() {
    lateinit var database: FlosiDatabase
        private set
    lateinit var repository: FinanceRepository
        private set
    lateinit var preferences: FlosiPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = FlosiPreferences(this)
        openFinanceLayer()
        FlosiWorkScheduler.ensureScheduled(this)
    }

    fun reloadAfterRestore() {
        FlosiDatabase.resetInstance()
        openFinanceLayer()
    }

    private fun openFinanceLayer() {
        database = FlosiDatabase.get(this)
        repository = FinanceRepository(database,preferences)
    }
}
