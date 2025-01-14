package pl.inpost.shipment.implementation.presentation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.inpost.common.utils.formatDate
import pl.inpost.common.utils.formatTime
import pl.inpost.shipment.implementation.R
import pl.inpost.shipment.implementation.presentation.model.DateTimeDisplayable
import java.time.ZonedDateTime
import javax.inject.Inject

class DateFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun format(date: ZonedDateTime): DateTimeDisplayable {
        return DateTimeDisplayable(
            dayOfWeekShort = formatDayOfWeek(date),
            date = date.formatDate(),
            time = date.formatTime()
        )
    }

    private fun formatDayOfWeek(date: ZonedDateTime): String {
        return when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> context.getString(R.string.monday_short)
            java.time.DayOfWeek.TUESDAY -> context.getString(R.string.tuesday_short)
            java.time.DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday_short)
            java.time.DayOfWeek.THURSDAY -> context.getString(R.string.thursday_short)
            java.time.DayOfWeek.FRIDAY -> context.getString(R.string.friday_short)
            java.time.DayOfWeek.SATURDAY -> context.getString(R.string.saturday_short)
            java.time.DayOfWeek.SUNDAY -> context.getString(R.string.sunday_short)
            else -> ""
        }
    }
}