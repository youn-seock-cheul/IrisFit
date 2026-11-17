package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.RestHistory
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun RestHistoryCard(model: GuidedSessionViewModel) {
    val today = LocalDate.parse(model.today)
    val month = YearMonth.from(today)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!model.recordsReady) {
                Text(model.error ?: stringResource(R.string.history_loading))
            } else {
                val todayCounts = RestHistory.counts(model.records, today, today)
                val thisMonth = RestHistory.month(model.records, month, today)
                val lastMonth = RestHistory.month(model.records, month.minusMonths(1), today)
                Text(stringResource(R.string.history_today, todayCounts.rests, todayCounts.exercises))
                Text(stringResource(R.string.history_this_month, thisMonth.rests, thisMonth.exercises, thisMonth.activeDays))
                val segments = RestHistory.monthSegments(model.records, today)
                val maxCount = segments.maxOf { it.second.rests + it.second.exercises }.coerceAtLeast(1)
                segments.forEach { (range, counts) ->
                    val total = counts.rests + counts.exercises
                    Text(stringResource(R.string.history_segment, range.first, range.last, total),
                        style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { total.toFloat() / maxCount }, modifier = Modifier.fillMaxWidth())
                }
                Text(stringResource(R.string.history_last_month, lastMonth.rests, lastMonth.exercises, lastMonth.activeDays))
                Text(stringResource(R.string.history_note), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
