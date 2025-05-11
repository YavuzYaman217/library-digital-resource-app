package com.yyaman.libraryapp.network

import okhttp3.ResponseBody
import retrofit2.http.*
import com.yyaman.libraryapp.data.*
import com.yyaman.libraryapp.reservations.ReservationResponse

interface ApiService {
    // ─────────── Auth ───────────
    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): User                   // <- return User data class

    // ─────────── Books ───────────
    @GET("books/search")
    suspend fun searchBooks(@Query("q") query: String): List<Book>

    @POST("books/{id}/reserve")
    suspend fun reserveBook(@Path("id") bookId: Int): ReserveResponse

    // ─────── Digital Resources ───────
    @GET("digital/")
    suspend fun listDigitalResources(): List<DigitalResource>

    @Streaming
    @GET("digital/{id}/download")
    suspend fun downloadPdf(@Path("id") resId: Int): ResponseBody

    // in ApiService.kt
    @GET("reservations/")
    suspend fun getReservations(): List<ReservationResponse>

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: Int): CancelResponse

    @GET("bookmarks/")
    suspend fun listBookmarks(): List<BookmarkResponse>

    @POST("bookmarks/")
    suspend fun addBookmark(@Body req: AddBookmarkRequest): BookmarkResponse

    @DELETE("bookmarks/{id}")
    suspend fun deleteBookmark(@Path("id") bmId: Int): ResponseBody

    @PUT("auth/me")
    suspend fun updateProfile(
        @Body req: UpdateProfileRequest
    ): UpdateProfileResponse

    @DELETE("auth/delete")
    suspend fun deleteAccount(): DeleteAccountResponse

    data class DeleteAccountResponse(val msg: String)
    data class CancelResponse(val msg: String)
}
