package com.pillsense.app.feature.adherence.data

import com.pillsense.app.feature.adherence.domain.Intake
import com.pillsense.app.feature.adherence.domain.Schedule
import java.time.LocalDate

class DebugTestDataProviderImpl : TestDataProvider {

    override fun loadTestData() {
        val intakes = generateTestIntakes(2)
        val schedules = generateTestSchedules(2)
    }

    override fun deleteTestData() {
        // Placeholder
    }

    private fun generateTestIntakes(weeks: Int = 2): List<Intake> {
        val intakes = mutableListOf<Intake>()
        val today = LocalDate.now()
        for (w in 0 until weeks) {
            for (d in 0..6) {
                val date = today.minusDays((weeks * 7 - w * 7 - d).toLong())
                if (Math.random() > 0.2) {
                    intakes.add(Intake("i_${w}_${d}", "m1", "s${d}", date, 8))
                }
            }
        }
        return intakes
    }

    private fun generateTestSchedules(weeks: Int = 2): List<Schedule> {
        val schedules = mutableListOf<Schedule>()
        val today = LocalDate.now()
        val start = today.minusDays((weeks * 7).toLong())
        for (d in 0..6) {
            schedules.add(Schedule("s$d", "m1", 8, start, today))
        }
        return schedules
    }
}

