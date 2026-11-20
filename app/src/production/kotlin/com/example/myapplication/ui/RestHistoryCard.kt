package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
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
    IrisCard {
        IrisCardHeader(
            icon = R.drawable.ic_ms_history,
            title = stringResource(R.string.history_title),
            subtitle = model.tr("history_card_sub")
        )
        if (!model.recordsReady) {
            IrisBodyText(model.error ?: stringResource(R.string.history_loading))
        } else {
            val todayCounts = RestHistory.counts(model.records, today, today)
            val thisMonth = RestHistory.month(model.records, month, today)
            val lastMonth = RestHistory.month(model.records, month.minusMonths(1), today)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.history_today, todayCounts.rests, todayCounts.exercises), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.history_this_month, thisMonth.rests, thisMonth.exercises, thisMonth.activeDays), style = MaterialTheme.typography.bodyMedium)
            }
            val segments = RestHistory.monthSegments(model.records, today)
            val maxCount = segments.maxOf { it.second.rests + it.second.exercises }.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                segments.forEach { (range, counts) ->
                    val total = counts.rests + counts.exercises
                    Text(
                        stringResource(R.string.history_segment, range.first, range.last, total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { total.toFloat() / maxCount },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        drawStopIndicator = {}
                    )
                }
            }
            Text(stringResource(R.string.history_last_month, lastMonth.rests, lastMonth.exercises, lastMonth.activeDays), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.history_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}