// app/src/main/java/com/yyaman/libraryapp/data/ReservationRepository.kt
package com.yyaman.libraryapp.data

import android.content.Context
import com.yyaman.libraryapp.network.RetrofitClient
import com.yyaman.libraryapp.reservations.ReservationResponse

class ReservationRepository(context: Context) {
    private val api = RetrofitClient.create(context)

    suspend fun fetchAll(): List<ReservationResponse> =
        api.getReservations()

    suspend fun cancel(id: Int): String =
        api.cancelReservation(id).msg
}