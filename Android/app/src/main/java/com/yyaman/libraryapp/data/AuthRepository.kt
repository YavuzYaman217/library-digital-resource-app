package com.yyaman.libraryapp.data

import android.content.Context
import com.yyaman.libraryapp.network.RetrofitClient

class AuthRepository(context: Context) {
    private val api = RetrofitClient.create(context)
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    suspend fun register(email: String, password: String, name: String): Result<User> =
        runCatching {
            val resp = api.register(RegisterRequest(email, password, name))
            saveToken(resp.token)
            resp.user
        }

    suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            val resp = api.login(LoginRequest(email, password))
            saveToken(resp.token)
        }

    suspend fun me(): User = api.me()

    suspend fun updateProfile(name: String, email: String): UpdateProfileResponse =
        api.updateProfile(UpdateProfileRequest(name, email))


    suspend fun deleteAccount(): String = api.deleteAccount().msg

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun clearToken() {
        prefs.edit().remove("token").apply()
    }


}
