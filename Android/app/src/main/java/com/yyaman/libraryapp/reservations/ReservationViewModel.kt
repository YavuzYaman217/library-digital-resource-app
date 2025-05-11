// app/src/main/java/com/yyaman/libraryapp/reservations/ReservationViewModel.kt
package com.yyaman.libraryapp.reservations

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.yyaman.libraryapp.data.ReservationRepository
import com.yyaman.libraryapp.notifications.OverdueWorker
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ReservationViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ReservationRepository(app)
    private val _state = MutableLiveData<ReservationState>(ReservationState.Loading)
    val state: LiveData<ReservationState> = _state
    private val TAG = "ReservationViewModel"

    fun loadReservations() {
        _state.value = ReservationState.Loading
        viewModelScope.launch {
            runCatching { repo.fetchAll() }
                .onSuccess { list ->
                    _state.value = ReservationState.Success(list)
                    scheduleOverdueAlerts(list)
                }
                .onFailure { e ->
                    _state.value = ReservationState.Error(e.localizedMessage ?: "Fetch failed")
                }
        }
    }

    private fun scheduleOverdueAlerts(reservations: List<ReservationResponse>) {
        val ctx = getApplication<Application>()

        reservations.forEach { res ->
            try {
                // Parse the due date with multiple fallback strategies
                val dueMillis = parseDueDate(res.dueDate)
                OverdueWorker.schedule(ctx, res.id, res.book.title, dueMillis)
                Log.d(TAG, "Scheduled notification for book '${res.book.title}' due at ${res.dueDate}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule notification for book '${res.book.title}': ${e.message}")
            }
        }
    }

    /**
     * Parse a due date string into epoch milliseconds
     * Tries multiple date formats to ensure robustness
     */
    private fun parseDueDate(dueDateStr: String): Long {
        // First try ISO_OFFSET_DATE_TIME format
        try {
            val offsetDateTime = OffsetDateTime.parse(dueDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return offsetDateTime.toInstant().toEpochMilli()
        } catch (e1: DateTimeParseException) {
            Log.d(TAG, "Failed to parse due date with ISO_OFFSET_DATE_TIME: $dueDateStr")
        }

        // Then try parsing as ZonedDateTime
        try {
            val zonedDateTime = ZonedDateTime.parse(dueDateStr)
            return zonedDateTime.toInstant().toEpochMilli()
        } catch (e2: DateTimeParseException) {
            Log.d(TAG, "Failed to parse due date as ZonedDateTime: $dueDateStr")
        }

        // Then try parsing as LocalDateTime with system default zone
        try {
            val localDateTime = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e3: DateTimeParseException) {
            Log.d(TAG, "Failed to parse due date as LocalDateTime with ISO_LOCAL_DATE_TIME: $dueDateStr")
        }

        // Then try parsing as plain LocalDateTime
        try {
            val localDateTime = LocalDateTime.parse(dueDateStr)
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e4: DateTimeParseException) {
            Log.d(TAG, "Failed to parse due date as plain LocalDateTime: $dueDateStr")
        }

        // If all parsing attempts fail, log and throw exception
        val errorMessage = "Failed to parse due date: $dueDateStr"
        Log.e(TAG, errorMessage)
        throw IllegalArgumentException(errorMessage)
    }

    fun cancelReservation(reservationId: Int, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.cancel(reservationId) }
                .onSuccess {
                    callback(true, "Reservation canceled successfully")
                }
                .onFailure { e ->
                    callback(false, e.localizedMessage ?: "Cancellation failed")
                }
        }
    }
}