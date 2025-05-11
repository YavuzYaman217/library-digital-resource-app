package com.yyaman.libraryapp.data

import android.content.Context
import com.yyaman.libraryapp.network.RetrofitClient

/**
 * Handles book-related API calls and local reservation state.
 */
class BookRepository(context: Context) {
    private val api = RetrofitClient.create(context)

    /**
     * Search for books matching [query].
     */
    suspend fun searchBooks(query: String): List<Book> =
        api.searchBooks(query)

    /**
     * Reserve the given [book] on the server and then record it locally.
     * Returns the server’s response message.
     */
    suspend fun reserve(book: Book): String {
        val resp = api.reserveBook(book.id)
        // record the reserved book in our in-memory store
        DummyStore.reservations.add(book.copy(available = false))
        return resp.msg
    }

    /**
     * Return the current list of reserved books.
     */
    fun getReservations(): List<Book> =
        DummyStore.reservations.toList()
}
