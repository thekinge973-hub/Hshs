package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssignmentCategory(val displayName: String, val iconName: String) {
    ASSIGNMENT("Homework", "assignment"),
    ACTIVITY("Activity", "event"),
    SPEAKING("Speaking Task", "record_voice_over"),
    COMMUNITY("Community Service", "volunteer_activism")
}

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: AssignmentCategory,
    val dueDate: String,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "AI"
    val text: String,
    val persona: String = "SARAH", // "SARAH", "DEBATE", "INTERVIEW", "GENERAL"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "accent_records")
data class AccentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetSentence: String,
    val spokenText: String,
    val overallScore: Int,
    val phoneticFeedback: String,
    val intonationFeedback: String,
    val tips: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val author: String,
    val role: String, // "Student", "Access Mentor", "Cohort Lead"
    val title: String,
    val content: String,
    val likes: Int = 0,
    val tag: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

data class WordEvaluation(
    val word: String,
    val score: Int, // 0 - 100
    val phoneticNote: String
)

data class AccentEvaluation(
    val overallScore: Int,
    val clarityRating: String, // "Native-like", "Clear & Natural", "Understandable", "Needs Practice"
    val summaryFeedback: String,
    val phoneticBreakdown: String,
    val intonationFeedback: String,
    val words: List<WordEvaluation> = emptyList(),
    val tipsForArabicSpeakers: List<String> = emptyList()
)

data class PracticeSentence(
    val id: String,
    val category: String, // e.g. "P vs B", "TH Sounds", "American R & L", "Presentation Skills", "Debate & Arguments"
    val sentence: String,
    val phoneticFocus: String,
    val difficulty: String // "Easy", "Medium", "Advanced"
)

enum class ChallengeType(val label: String) {
    PRONUNCIATION("Pronunciation Duel"),
    SPEED_TWISTER("Tongue Twister Sprint"),
    VOCAB_QUIZ("Access Vocab & Idioms"),
    DEBATE_PROMPT("Access Debate Spark")
}

@Entity(tableName = "english_challenges")
data class EnglishChallenge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val targetPrompt: String,
    val options: List<String> = emptyList(), // for quiz
    val correctIndex: Int = -1,
    val pointsReward: Int = 100,
    val difficulty: String = "Medium",
    val isCompleted: Boolean = false,
    val userScore: Int = 0
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val role: String, // e.g. "Access Cohort 14", "Class of 2026"
    val avatarInitials: String,
    val points: Int,
    val accentAvg: Int,
    val challengesCompleted: Int,
    val badge: String,
    val isCurrentUser: Boolean = false
)
