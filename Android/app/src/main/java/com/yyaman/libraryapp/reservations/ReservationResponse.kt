package com.yyaman.libraryapp.reservations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yyaman.libraryapp.data.Book

// Tell Moshi to generate an adapter for this class:
@JsonClass(generateAdapter = true)
data class ReservationResponse(
    val id: Int,
    val book: Book,
    @Json(name = "created_at") val createdAt: String,
    @Json(name="due_date")     val dueDate:   String
)
