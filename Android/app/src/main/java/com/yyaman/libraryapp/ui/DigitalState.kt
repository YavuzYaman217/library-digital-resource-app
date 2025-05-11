package com.yyaman.libraryapp.ui

import com.yyaman.libraryapp.data.DigitalResource

sealed class DigitalState {
    object Loading : DigitalState()
    data class Success(val items: List<DigitalResource>) : DigitalState()
    data class Error(val error: String) : DigitalState()
}