// BookmarkViewModel.kt
package com.yyaman.libraryapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.yyaman.libraryapp.data.BookmarkRepository
import com.yyaman.libraryapp.data.BookmarkResponse
import kotlinx.coroutines.launch

sealed class BookmarkState {
    object Loading : BookmarkState()
    data class Success(val items: List<BookmarkResponse>) : BookmarkState()
    data class Error(val message: String) : BookmarkState()
}

class BookmarkViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BookmarkRepository(app)
    private val _state = MutableLiveData<BookmarkState>(BookmarkState.Loading)
    val state: LiveData<BookmarkState> = _state

    fun loadAll() {
        _state.value = BookmarkState.Loading
        viewModelScope.launch {
            runCatching { repo.fetchAll() }
                .onSuccess { _state.value = BookmarkState.Success(it) }
                .onFailure { _state.value = BookmarkState.Error(it.localizedMessage ?: "Fetch failed") }
        }
    }

    fun add(itemType: String, itemId: Int, onDone: (BookmarkResponse?) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.add(itemType, itemId) }
                .onSuccess { onDone(it); loadAll() }
                .onFailure { onDone(null) }
        }
    }

    fun delete(bmId: Int, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.delete(bmId) }
                .onSuccess { onDone(true); loadAll() }
                .onFailure { onDone(false) }
        }
    }
}
