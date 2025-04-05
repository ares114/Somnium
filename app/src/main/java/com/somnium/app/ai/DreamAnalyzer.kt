package com.somnium.app.ai

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DreamAnalyzer(private val context: Context) {
    companion object {
        private const val TAG = "DreamAnalyzer"
        private const val API_KEY = "AIzaSyD4TopFkTm3h86gAqWWiMNjW4ttB5ogW5o"
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = API_KEY
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    interface AnalysisCallback {
        fun onSuccess(analysis: DreamAnalysis)
        fun onError(error: String)
        fun onLoading()
    }

    fun analyzeDream(dreamTitle: String, dreamContent: String, callback: AnalysisCallback) {
        callback.onLoading()
        
        scope.launch {
            try {
                Log.d(TAG, "Starting dream analysis")
                val prompt = createAnalysisPrompt(dreamTitle, dreamContent)
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

    private fun createAnalysisPrompt(dreamTitle: String, dreamContent: String): String {
        return """
            You are a dream interpreter providing brief, insightful analyses.
            Analyze this dream and provide a clean, concise response in JSON format.
            
            Title: $dreamTitle
            Dream Content: $dreamContent
            
            Format your response EXACTLY like this JSON:
            {
                "summary": "Brief 1-2 sentence summary of the key elements",
                "meaning": "Short paragraph explaining the possible psychological meaning (2-3 sentences)",
                "advice": "Single paragraph with 1-2 pieces of practical advice (max 3 sentences)"
            }
            
            Keep each section brief and focused. DO NOT include any bullet points, numbering, or markdown formatting.
            Return ONLY valid JSON with no extra text.
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
} 