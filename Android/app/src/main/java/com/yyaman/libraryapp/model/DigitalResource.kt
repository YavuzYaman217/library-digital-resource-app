package com.yyaman.libraryapp.model

import com.squareup.moshi.Json

data class DigitalResource(
    val id: Int,
    val title: String,
    val author: String,
    val type: String,       // PDF, ePub…
    @Json(name="file_path") val filePath: String
)

