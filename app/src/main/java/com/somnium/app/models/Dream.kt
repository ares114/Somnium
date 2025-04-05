package com.somnium.app.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Dream(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("user_id")
    val user_id: String? = null,
    
    @SerialName("title")
    val title: String = "",
    
    @SerialName("content")
    val content: String = "",
    
    @SerialName("created_at")
    val created_at: String? = null,
    
    @SerialName("dream_date")
    val dream_date: String? = null,
    
    @SerialName("analysis_summary")
    val analysis_summary: String? = null,
    
    @SerialName("analysis_meaning")
    val analysis_meaning: String? = null,
    
    @SerialName("analysis_advice")
    val analysis_advice: String? = null,
    
    @SerialName("analyzed_at")
    val analyzed_at: String? = null
) {
    companion object {
        fun formatDate(date: Date): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(date)
        }
    }
    
    fun hasAnalysis(): Boolean {
        return !analysis_summary.isNullOrEmpty() || 
               !analysis_meaning.isNullOrEmpty() || 
               !analysis_advice.isNullOrEmpty()
    }
} 