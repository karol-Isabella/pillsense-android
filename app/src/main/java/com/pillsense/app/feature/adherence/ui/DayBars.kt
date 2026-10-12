package com.pillsense.app.feature.adherence.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DayBarsRow(byDayOfWeek: Map<DayOfWeek, Float>, intakesPerDay: Map<DayOfWeek, Int>, omittedPerDay: Map<DayOfWeek, Int>) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        DayOfWeek.entries.forEach { day ->
            val adherence = byDayOfWeek[day] ?: 0f
            val omitted = omittedPerDay[day] ?: 0
            DayBar(day.name.take(1), adherence, omitted)
        }
    }
}

@Composable
fun DayBar(dayLabel: String, adherence: Float, omitted: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
        Box(modifier = Modifier
            .width(30.dp)
            .height(60.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(adherence / 100f)
                    .background(MaterialTheme.colorScheme.primary)
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(omitted / 7f)
                    .background(MaterialTheme.colorScheme.error)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(if (omitted > 0) omitted.toString() else dayLabel, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}
