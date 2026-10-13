package com.pillsense.app.core.util

import java.util.concurrent.TimeUnit

object IntakeLogic {
    fun shouldMarkAsSkipped(status: String, scheduledAt: Long, now: Long): Boolean {
        return status == "PENDING" && now > scheduledAt + TimeUnit.MINUTES.toMillis(60)
    }
}
