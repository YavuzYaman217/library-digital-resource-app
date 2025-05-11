// app/src/main/java/com/yyaman/libraryapp/data/BookmarkRepository.kt
package com.yyaman.libraryapp.data

import android.content.Context
import com.yyaman.libraryapp.network.RetrofitClient

class BookmarkRepository(context: Context) {
    private val api = RetrofitClient.create(context)

    suspend fun fetchAll(): List<BookmarkResponse> =
        api.listBookmarks()

    suspend fun add(itemType: String, itemId: Int): BookmarkResponse =
        api.addBookmark(AddBookmarkRequest(itemType, itemId))

    suspend fun delete(bmId: Int) {
        api.deleteBookmark(bmId)
    }
}
