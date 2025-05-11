package com.yyaman.libraryapp.data

import com.yyaman.libraryapp.model.DigitalResource

object DummyStore {
    val bookmarks = mutableListOf<Any>()   // Book veya DigitalResource
    val reservations = mutableListOf<Book>()
}
