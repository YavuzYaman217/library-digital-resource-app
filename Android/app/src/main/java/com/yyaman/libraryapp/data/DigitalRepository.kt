package com.yyaman.libraryapp.data

import android.content.Context
import com.yyaman.libraryapp.network.RetrofitClient
import okhttp3.ResponseBody

/**
 * Handles digital‐resource API calls.
 */
class DigitalRepository(context: Context) {
    private val api = RetrofitClient.create(context)

    /**
     * Fetches the list of all digital resources.
     */
    suspend fun list(): List<DigitalResource> =
        api.listDigitalResources()

    /**
     * Downloads the PDF for resource ID [id].
     * Returns the raw ResponseBody for saving to file.
     */
    suspend fun download(id: Int): ResponseBody =
        api.downloadPdf(id)
}
