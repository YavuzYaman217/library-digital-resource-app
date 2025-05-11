// app/src/main/java/com/yyaman/libraryapp/reservations/ReservationState.kt

package com.yyaman.libraryapp.reservations

import com.yyaman.libraryapp.network.ApiService

sealed class ReservationState {
    object Loading : ReservationState()
    data class Success(val reservations: List<ReservationResponse>) : ReservationState()
    data class Error(val error: String) : ReservationState()
}

