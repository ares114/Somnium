package com.somnium.app.utils

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object SupabaseClient {
    
    // Supabase credentials
    private const val SUPABASE_URL = "https://zaijyrdcwfgreipkgsjw.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InphaWp5cmRjd2ZncmVpcGtnc2p3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI5OTQzNjQsImV4cCI6MjA1ODU3MDM2NH0._MiMBN2h_wtDfDbg_DpYZ0H1rD78jtzIFGt4YWgERQY"
    private const val TAG = "SupabaseClient"
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true // Allow more flexible JSON parsing
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            
            // Create new request with additional headers
            val request = original.newBuilder()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .build()
                
            // Log the request for debugging
            Log.d(TAG, "Request URL: ${request.url}")
            
            val response = chain.proceed(request)
            
            // Log the response for debugging
            Log.d(TAG, "Response Code: ${response.code}")
            
            response
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/rest/v1/")
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
} 