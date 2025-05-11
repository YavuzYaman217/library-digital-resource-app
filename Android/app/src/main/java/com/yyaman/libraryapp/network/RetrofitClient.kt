package com.yyaman.libraryapp.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yyaman.libraryapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // Move this into BuildConfig if you like via a buildConfigField.
    private const val BASE_URL = "http://172.16.1.135:5000/"

    // Build a Moshi instance that knows about Kotlin data classes:
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)  // <-- use our Moshi
            )
            .build()
            .create(ApiService::class.java)
    }
}
