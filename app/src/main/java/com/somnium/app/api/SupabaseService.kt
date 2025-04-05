package com.somnium.app.api

import com.somnium.app.models.Dream
import retrofit2.http.*

interface SupabaseService {
    
    @GET("dreams")
    suspend fun getUserDreams(
        @Query("user_id") userId: String,
        @Query("select") select: String = "*",
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Dream>
    
    @GET("dreams")
    suspend fun getDreamById(
        @Query("id") dreamId: String,
        @Query("select") select: String = "*",
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Dream>
    
    @POST("dreams")
    @Headers("Content-Type: application/json")
    suspend fun insertDream(
        @Body dream: Dream,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Dream>
    
    @PATCH("dreams")
    @Headers("Content-Type: application/json")
    suspend fun updateDream(
        @Query("id") eq: String,
        @Body dream: Dream,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Dream>
    
    @DELETE("dreams")
    suspend fun deleteDream(
        @Query("id") eq: String,
        @Header("Prefer") prefer: String = "return=representation"
    )
} 