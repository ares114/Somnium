package com.somnium.app.ai

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DreamAnalyzer(context: Context) {
    companion object {
        private const val TAG = "DreamAnalyzer"
    }

    private val apiKey = getApiKey(context) // Securely retrieve API key
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyD4TopFkTm3h86gAqWWiMNjW4ttB5ogW5o"
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    interface AnalysisCallback {
        fun onSuccess(analysis: DreamAnalysis)
        fun onError(error: String)
    }

    fun analyzeDream(dreamContent: String, callback: AnalysisCallback) {
        scope.launch {
            try {
                Log.d(TAG, "Starting dream analysis")
                val prompt = createAnalysisPrompt(dreamContent)
                Log.d(TAG, "Generated prompt: $prompt")

                val content = content {
                    text(prompt)
                }

                val response = withContext(Dispatchers.IO) {
                    Log.d(TAG, "Calling Gemini API...")
                    generativeModel.generateContent(content)
                }

                Log.d(TAG, "Received response: $response")
                val text = response.text
                Log.d(TAG, "Extracted text: $text")

                if (!text.isNullOrEmpty()) {
                    val analysis = parseDreamAnalysis(text)
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(analysis)
                    }
                } else {
                    Log.e(TAG, "Empty response text")
                    withContext(Dispatchers.Main) {
                        callback.onError("Failed to generate analysis: Empty response")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing dream: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback.onError("Error analyzing dream: ${e.message}")
                }
            }
        }
    }

    private fun createAnalysisPrompt(dreamContent: String): String {
        return """
            You are a friendly dream interpreter. Analyze the following dream and provide a simple, easy-to-read analysis in JSON format.
            Format your response EXACTLY like this, with short, clear explanations:
            {
                "summary": "A brief 1-2 sentence summary of the dream",
                "meaning": "A simple explanation of what this dream might mean for the person",
                "advice": "One practical piece of advice based on this dream"
            }

            Dream Content: $dreamContent

            Keep your analysis friendly, simple, and easy to understand. Remember to ONLY return valid JSON format, no additional text.
        """.trimIndent()
    }

    private fun parseDreamAnalysis(analysisText: String): DreamAnalysis {
        return try {
            // Remove any non-JSON text that might be in the response
            val jsonStart = analysisText.indexOf("{")
            val jsonEnd = analysisText.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonText = analysisText.substring(jsonStart, jsonEnd)
                DreamAnalysis(jsonText)
            } else {
                DreamAnalysis(analysisText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON response: ${e.message}", e)
            DreamAnalysis(analysisText)
        }
    }

    private fun getApiKey(context: Context): String {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(
                context.packageName, 
                android.content.pm.PackageManager.GET_META_DATA
            )
            applicationInfo.metaData.getString("com.google.ai.client.generativeai.API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving API key: ${e.message}", e)
            ""
        }
    }
}