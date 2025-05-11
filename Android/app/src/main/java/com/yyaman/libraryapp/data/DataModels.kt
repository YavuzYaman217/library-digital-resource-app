package com.yyaman.libraryapp.data
import com.squareup.moshi.Json

// ─── User ────────────────────────────────────────────────────────
data class User(
    val id: Int,
    val email: String,
    val name: String
)

// ─── Auth ────────────────────────────────────────────────────────
data class RegisterRequest(val email: String, val password: String, val name: String)
data class RegisterResponse(val token: String, val user: User)
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

// ─── Books ───────────────────────────────────────────────────────
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val available: Boolean
)
data class ReserveResponse(val msg: String)

// ─── Digital Resources ──────────────────────────────────────────
data class DigitalResource(
    val id: Int,
    val title: String,
    val author: String,
    val type: String,

    /** maps the JSON `file_path` to this property */
    @Json(name = "file_path")
    val filePath: String
)

data class UpdateProfileRequest(
    val name: String,
    val email: String
)

data class UpdateProfileResponse(
    val id: Int,
    val email: String,
    val name: String
)
