package com.yyaman.libraryapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.yyaman.libraryapp.data.DigitalRepository
import com.yyaman.libraryapp.data.DigitalResource
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class DigitalViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DigitalRepository(app)

    private val _state = MutableLiveData<DigitalState>(DigitalState.Loading)
    val state: LiveData<DigitalState> = _state

    /** Load all digital resources into state */
    fun loadAll() {
        _state.value = DigitalState.Loading
        viewModelScope.launch {
            runCatching { repo.list() }
                .onSuccess { list -> _state.value = DigitalState.Success(list) }
                .onFailure { thr -> _state.value = DigitalState.Error(thr.localizedMessage ?: "Load failed") }
        }
    }

    /**
     * Download resource PDF; on success returns the raw ResponseBody,
     * on error returns a message.
     */
    fun download(
        id: Int,
        onFile: (ResponseBody) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching { repo.download(id) }
                .onSuccess { body -> onFile(body) }
                .onFailure { thr -> onError(thr.localizedMessage ?: "Download failed") }
        }
    }
}
