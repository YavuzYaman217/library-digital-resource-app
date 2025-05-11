// app/src/main/java/com/yyaman/libraryapp/data/BookmarkModels.kt
package com.yyaman.libraryapp.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Sent when the user taps “Bookmark” */
@JsonClass(generateAdapter = true)
data class AddBookmarkRequest(
    @Json(name="item_type") val itemType: String,  // "book" or "digital"
    @Json(name="item_id")   val itemId:   Int
)

/** Returned by GET /bookmarks and POST /bookmarks */
@JsonClass(generateAdapter = true)
data class BookmarkResponse(
    val id: Int,
    @Json(name = "item_type")   val itemType:  String,
    @Json(name = "item_id")     val itemId:    Int,
    @Json(name = "created_at")  val createdAt: String,
    val item: BookmarkItem
)

@JsonClass(generateAdapter = true)
data class BookmarkItem(
    val id:        Int,
    val title:     String,
    val author:    String,
    val year:      Int?     = null,  // only for books
    val available: Boolean? = null,  // only for books
    val type:      String?  = null   // only for digital
)
