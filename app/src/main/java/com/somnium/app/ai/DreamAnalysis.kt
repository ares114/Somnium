package com.somnium.app.ai

import android.util.Log
import org.json.JSONObject

data class DreamAnalysis(
    val rawResponse: String,
    val summary: String = "",
    val meaning: String = "",
    val advice: String = "",
    val symbols: String = ""
) {
    companion object {
        private const val TAG = "DreamAnalysis"
    }

    constructor(jsonText: String) : this(
        rawResponse = jsonText,
        summary = cleanFormatting(parseJsonField(jsonText, "summary")),
        meaning = cleanFormatting(parseJsonField(jsonText, "meaning")),
        advice = cleanFormatting(parseJsonField(jsonText, "advice")),
        symbols = cleanFormatting(parseJsonField(jsonText, "symbols"))
    )

    private fun isValid(): Boolean {
        return summary.isNotEmpty() || meaning.isNotEmpty() || advice.isNotEmpty()
    }

    override fun toString(): String {
        return if (isValid()) {
            """
            Summary: $summary
            
            Meaning: $meaning
            
            Advice: $advice
            """.trimIndent()
        } else {
            rawResponse
        }
    }
}

private fun cleanFormatting(text: String): String {
    return text
        .replace(Regex("^\\s*[-*•]\\s*"), "") // Remove bullet points at start of text
        .replace(Regex("\\n\\s*[-*•]\\s*"), "\n") // Remove bullet points after newlines
        .replace(Regex("\\d+\\.\\s+"), "") // Remove numbered list markers
        .replace(Regex("\\n{3,}"), "\n\n") // Replace excess newlines
        .trim()
}

private fun parseJsonField(jsonText: String, field: String): String {
    return try {
        val jsonObject = JSONObject(jsonText)
        jsonObject.optString(field, "")
    } catch (e: Exception) {
        Log.e("DreamAnalysis", "Error parsing JSON field $field: ${e.message}")
        ""
    }
} 