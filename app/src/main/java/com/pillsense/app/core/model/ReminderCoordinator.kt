package com.pillsense.app.core.model

interface ReminderCoordinator {
    suspend fun refresh()
    suspend fun act(id: String, status: String)
    fun stop()
}
