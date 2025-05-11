package com.yyaman.libraryapp.ui

import com.yyaman.libraryapp.data.Book

/** UI state for book search & reservation. */
sealed class BookState {
    object Idle : BookState()
    object Loading : BookState()
    data class Success(val books: List<Book>) : BookState()
    data class Error(val error: String) : BookState()
}
