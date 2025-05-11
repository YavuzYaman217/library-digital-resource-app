package com.yyaman.libraryapp.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yyaman.libraryapp.data.AuthRepository
import kotlinx.coroutines.launch

// ————————————————————————————————————————————
// Represents the UI state of your authentication screens
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val msg: String = "") : AuthState()
    data class Error(val error: String)       : AuthState()
}

// ————————————————————————————————————————————
// ViewModel that drives Login & Register flows
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(app)
    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    /** Registers a new user, saves token on success */
    fun register(email: String, password: String, name: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            repo.register(email, password, name)
                .onSuccess { user ->
                    _state.value = AuthState.Success("Registered as ${user.name}")
                }
                .onFailure { thr ->
                    _state.value = AuthState.Error(thr.localizedMessage ?: "Registration failed")
                }
        }
    }

    /** Logs in an existing user, saves token on success */
    fun login(email: String, password: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            repo.login(email, password)
                .onSuccess {
                    _state.value = AuthState.Success("Logged in")
                }
                .onFailure { thr ->
                    _state.value = AuthState.Error(thr.localizedMessage ?: "Login failed")
                }
        }
    }

    /** Clears saved token and resets state */
    fun logout() {
        repo.clearToken()
        _state.value = AuthState.Idle
    }
}
