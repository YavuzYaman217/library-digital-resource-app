package com.yyaman.libraryapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.yyaman.libraryapp.data.Book
import com.yyaman.libraryapp.data.BookRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for searching and reserving books.
 */
class BookViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BookRepository(app)

    private val _state = MutableLiveData<BookState>(BookState.Idle)
    val state: LiveData<BookState> = _state

    /** Perform a search and emit results into [state]. */
    fun search(query: String) {
        _state.value = BookState.Loading
        viewModelScope.launch {
            runCatching { repo.searchBooks(query) }
                .onSuccess { list -> _state.value = BookState.Success(list) }
                .onFailure { thr ->
                    _state.value = BookState.Error(thr.localizedMessage ?: "Search failed")
                }
        }
    }

    /**
     * Reserve [book]; calls [onComplete] with (success, message).
     * On success, caller should re-run search to refresh availability.
     */
    fun reserve(book: Book, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.reserve(book) }
                .onSuccess { msg -> onComplete(true, msg) }
                .onFailure { thr ->
                    onComplete(false, thr.localizedMessage ?: "Reservation failed")
                }
        }
    }
}
