package com.somnium.app.repositories

import android.util.Log
import com.somnium.app.api.SupabaseService
import com.somnium.app.models.Dream
import com.somnium.app.ai.DreamAnalysis
import com.somnium.app.utils.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DreamRepository {
    
    private val TAG = "DreamRepository"
    private val apiService: SupabaseService = SupabaseClient.createService()
    
    suspend fun saveDream(dream: Dream): Result<Dream> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving dream: $dream")
            val result = apiService.insertDream(dream)
            if (result.isEmpty()) {
                Log.e(TAG, "Empty result when saving dream")
                return@withContext Result.failure(Exception("Failed to insert dream - empty response"))
            }
            Log.d(TAG, "Dream saved successfully: ${result.first()}")
            Result.success(result.first())
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error when saving dream: ${e.code()} ${e.message()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error body: $errorBody")
            Result.failure(Exception("Server error: ${e.code()} - ${errorBody ?: e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error when saving dream", e)
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error when saving dream", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserDreams(userId: String): Result<List<Dream>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting dreams for user: $userId")
            val dreams = apiService.getUserDreams("eq.$userId")
            Log.d(TAG, "Retrieved ${dreams.size} dreams for user")
            Result.success(dreams)
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error when getting user dreams: ${e.code()} ${e.message()}")
            Result.failure(Exception("Server error: ${e.code()} ${e.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error when getting user dreams", e)
            Result.failure(e)
        }
    }
    
    suspend fun getDreamById(dreamId: String): Result<Dream> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting dream with ID: $dreamId")
            val dreamList = apiService.getDreamById("eq.$dreamId")
            if (dreamList.isEmpty()) {
                Log.e(TAG, "Dream not found with ID: $dreamId")
                return@withContext Result.failure(Exception("Dream not found"))
            }
            Log.d(TAG, "Retrieved dream: ${dreamList.first()}")
            Result.success(dreamList.first())
        } catch (e: Exception) {
            Log.e(TAG, "Error when getting dream by ID", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateDream(dream: Dream): Result<Dream> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating dream: $dream")
            val dreamId = dream.id ?: throw Exception("Dream ID cannot be null for update")
            val result = apiService.updateDream("eq.$dreamId", dream)
            if (result.isEmpty()) {
                Log.e(TAG, "Empty result when updating dream")
                return@withContext Result.failure(Exception("Failed to update dream"))
            }
            Log.d(TAG, "Dream updated successfully: ${result.first()}")
            Result.success(result.first())
        } catch (e: Exception) {
            Log.e(TAG, "Error when updating dream", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteDream(dreamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting dream with ID: $dreamId")
            apiService.deleteDream("eq.$dreamId")
            Log.d(TAG, "Dream deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error when deleting dream", e)
            Result.failure(e)
        }
    }
    
    suspend fun saveAnalysis(dreamId: String, analysis: DreamAnalysis): Result<Dream> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving analysis for dream ID: $dreamId")
            
            // Get the current dream first
            val dreamResult = getDreamById(dreamId)
            if (dreamResult.isFailure) {
                return@withContext Result.failure(Exception("Failed to retrieve dream for analysis update"))
            }
            
            val dream = dreamResult.getOrNull()!!
            
            // Create updated dream with analysis data
            val updatedDream = dream.copy(
                analysis_summary = analysis.summary,
                analysis_meaning = analysis.meaning,
                analysis_advice = analysis.advice,
                analyzed_at = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            // Update the dream
            return@withContext updateDream(updatedDream)
        } catch (e: Exception) {
            Log.e(TAG, "Error when saving analysis", e)
            Result.failure(e)
        }
    }
} 