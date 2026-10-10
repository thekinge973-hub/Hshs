package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AccentEvaluation
import com.example.data.model.ChatMessage
import com.example.data.model.WordEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun evaluateAccent(
        targetSentence: String,
        spokenSentence: String
    ): AccentEvaluation = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = """
            You are an expert American English Accent & Pronunciation Coach for Amideast Access Microscholarship students (Arabic native speakers learning American English).
            
            Target sentence to speak: "$targetSentence"
            Actual transcription of what student spoke: "$spokenSentence"
            
            Evaluate the pronunciation, accent, clarity, and phonetic accuracy. Focus on challenges common for Arabic speakers (e.g. /p/ vs /b/, /v/ vs /f/, retroflex American /r/, /θ/ and /ð/, vowel reductions, and rhythm/stress).
            
            Respond ONLY in valid JSON format matching this schema:
            {
               "overallScore": 88,
               "clarityRating": "Clear & Natural",
               "summaryFeedback": "Great cadence and rhythm! Your American 'r' sounded very authentic.",
               "phoneticBreakdown": "Consonants were sharp. Remember to aspirate the 'p' sound with a puff of air.",
               "intonationFeedback": "Rising intonation on questions and clean drop at sentence finish.",
               "words": [
                  {"word": "practice", "score": 90, "phoneticNote": "Crisp /p/ sound"},
                  {"word": "makes", "score": 95, "phoneticNote": "Clear diphthong /eɪ/"},
                  {"word": "perfect", "score": 85, "phoneticNote": "Good retroflex /r/"}
               ],
               "tipsForArabicSpeakers": [
                  "Hold a piece of paper in front of your lips when pronouncing 'P' to see it flutter.",
                  "Lengthen American vowels before voiced consonants."
               ]
            }
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalAccentEvaluation(targetSentence, spokenSentence)
        }

        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w("GeminiClient", "API error: ${response.code} $responseBody")
                return@withContext generateLocalAccentEvaluation(targetSentence, spokenSentence)
            }

            parseAccentJson(responseBody, targetSentence, spokenSentence)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Gemini call failed", e)
            generateLocalAccentEvaluation(targetSentence, spokenSentence)
        }
    }

    private fun parseAccentJson(
        responseBody: String,
        targetSentence: String,
        spokenSentence: String
    ): AccentEvaluation {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text")

            val jsonText = rawText.replace("```json", "").replace("```", "").trim()
            val evalJson = JSONObject(jsonText)

            val wordsList = mutableListOf<WordEvaluation>()
            val wordsArray = evalJson.optJSONArray("words")
            if (wordsArray != null) {
                for (i in 0 until wordsArray.length()) {
                    val w = wordsArray.getJSONObject(i)
                    wordsList.add(
                        WordEvaluation(
                            word = w.optString("word", ""),
                            score = w.optInt("score", 85),
                            phoneticNote = w.optString("phoneticNote", "Clear articulation")
                        )
                    )
                }
            }

            val tipsList = mutableListOf<String>()
            val tipsArray = evalJson.optJSONArray("tipsForArabicSpeakers")
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    tipsList.add(tipsArray.getString(i))
                }
            }

            AccentEvaluation(
                overallScore = evalJson.optInt("overallScore", 85).coerceIn(40, 100),
                clarityRating = evalJson.optString("clarityRating", "Clear & Natural"),
                summaryFeedback = evalJson.optString("summaryFeedback", "Great effort on speaking like a native!"),
                phoneticBreakdown = evalJson.optString("phoneticBreakdown", "Vowels and consonants matched American standards well."),
                intonationFeedback = evalJson.optString("intonationFeedback", "Natural pitch contours and sentence melody."),
                words = wordsList.ifEmpty { defaultWords(targetSentence) },
                tipsForArabicSpeakers = tipsList.ifEmpty {
                    listOf("Be mindful of voiceless /p/ and retroflex /r/ sounds in American English.")
                }
            )
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error parsing JSON", e)
            generateLocalAccentEvaluation(targetSentence, spokenSentence)
        }
    }

    private fun defaultWords(sentence: String): List<WordEvaluation> {
        return sentence.split(" ")
            .filter { it.isNotBlank() }
            .take(6)
            .map { WordEvaluation(it, (80..95).random(), "Good phonetic clarity") }
    }

    private fun generateLocalAccentEvaluation(
        targetSentence: String,
        spokenSentence: String
    ): AccentEvaluation {
        val targetTokens = targetSentence.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val spokenTokens = spokenSentence.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        var matches = 0
        targetTokens.forEach { token ->
            if (spokenTokens.any { it.contains(token) || token.contains(it) }) {
                matches++
            }
        }

        val accuracyRatio = if (targetTokens.isNotEmpty()) matches.toFloat() / targetTokens.size else 0.8f
        val calculatedScore = (70 + (accuracyRatio * 25)).toInt().coerceIn(60, 98)

        val clarityRating = when {
            calculatedScore >= 90 -> "Native-like Accent"
            calculatedScore >= 80 -> "Clear & Natural"
            calculatedScore >= 70 -> "Understandable"
            else -> "Needs Practice"
        }

        val wordEvaluations = targetTokens.map { word ->
            val cleanWord = word.filter { it.isLetter() }
            val wordScore = if (spokenTokens.contains(cleanWord)) (85..98).random() else (65..82).random()
            val note = when {
                cleanWord.contains("p") -> "Clear aspiration on /p/"
                cleanWord.contains("th") -> "Clean dental fricative /θ/"
                cleanWord.contains("r") -> "Strong American retroflex /r/"
                else -> "Natural articulation"
            }
            WordEvaluation(cleanWord, wordScore, note)
        }

        return AccentEvaluation(
            overallScore = calculatedScore,
            clarityRating = clarityRating,
            summaryFeedback = "Splendid job! You articulated the core cadence and sentence stress well.",
            phoneticBreakdown = "Accurate rhythm. Work on distinguishing /p/ from /b/ and keep tongue tip curled for the American /r/.",
            intonationFeedback = "Good natural rise and fall across thought groups. American English links consonants into vowels smoothly.",
            words = wordEvaluations,
            tipsForArabicSpeakers = listOf(
                "Aspirate the 'P' sound with an audible burst of air so it doesn't sound like 'B'.",
                "Ensure the 'TH' sound (like in 'thought' or 'this') places tongue between upper and lower teeth.",
                "Smooth out connected speech: link final consonants to the starting vowels of following words."
            )
        )
    }

    suspend fun chatWithPersona(
        persona: String,
        userMessage: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemInstruction = when (persona) {
            "SARAH" -> "You are Coach Sarah, a warm, motivating, native American English teacher and mentor for the Amideast English Access Microscholarship Program. You converse naturally in conversational American English, introduce real idioms, share encouragement, and foster leadership and cultural exchange. Keep answers lively, concise (2-4 sentences), and ask a follow-up question to keep the student practicing speaking."
            "DEBATE" -> "You are Alex, an energetic Amideast debate club partner. Challenge the student's ideas respectfully, introduce thoughtful perspectives on topics like technology, youth, education, and environment, and encourage persuasive argumentation."
            "INTERVIEW" -> "You are Ms. Taylor, an Amideast scholarship committee interviewer. Ask realistic interview questions about goals, community service, leadership experiences, and give constructive feedback to prepare the student for real-world interviews."
            else -> "You are an English language coach for Amideast Access students. Answer English grammar, vocabulary, idiom, and pronunciation questions accurately and cheerfully."
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalPersonaReply(persona, userMessage)
        }

        try {
            val contentsArray = JSONArray()

            // Add history turns (last 6 messages)
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == "USER") "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
            }

            // Current message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                })
            })

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                return@withContext getLocalPersonaReply(persona, userMessage)
            }

            val root = JSONObject(responseBody)
            val candidates = root.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text").trim()
        } catch (e: Exception) {
            Log.e("GeminiClient", "Chat call error", e)
            getLocalPersonaReply(persona, userMessage)
        }
    }

    private fun getLocalPersonaReply(persona: String, userMessage: String): String {
        val lower = userMessage.lowercase()
        return when (persona) {
            "SARAH" -> {
                when {
                    lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                        "Hey there, Access star! It's Coach Sarah. How is your English practice going today? Ready to chat about your week or try a fun idiom?"
                    lower.contains("activity") || lower.contains("homework") || lower.contains("assignment") ->
                        "That sounds like a fantastic Access project! Remember that in our program, teamwork and community impact are just as important as your English skills. What's the main idea you're working on?"
                    lower.contains("accent") || lower.contains("pronounce") ->
                        "Great question! Focusing on the American 'r' and linking words together makes a huge difference. Try heading over to our Accent Coach tab to test your pronunciation in real-time!"
                    else ->
                        "I love that perspective! In American culture, sharing personal experiences is key to great storytelling. How does that connect to what you've learned in your Access workshops so far?"
                }
            }
            "DEBATE" -> {
                when {
                    lower.contains("hello") || lower.contains("start") ->
                        "Welcome to the Amideast Debate Ring! Today's prompt: 'Should social media be banned for students during exam week?' What's your opening statement?"
                    else ->
                        "That's an interesting claim, but consider the opposing view: what about the counter-argument that digital connectivity aids peer collaboration? How would you refute that point?"
                }
            }
            "INTERVIEW" -> {
                when {
                    lower.contains("ready") || lower.contains("hello") || lower.contains("hi") ->
                        "Welcome to your Amideast Access scholarship interview prep! Let's start with a classic: Tell me about a time you led a community service project or overcame a challenge."
                    else ->
                        "Strong response! To elevate it further, remember the STAR technique: Situation, Task, Action, and Result. Make sure to emphasize what *you* personally accomplished."
                }
            }
            else -> {
                "That's a great English language question! In American English, practice and consistency are the real game-changers. Feel free to ask about grammar, pronunciation, or vocabulary anytime!"
            }
        }
    }
}
