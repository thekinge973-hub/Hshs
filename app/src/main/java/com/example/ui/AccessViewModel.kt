package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.TextToSpeechHelper
import com.example.data.local.AppDatabase
import com.example.data.model.AccentEvaluation
import com.example.data.model.AccentRecord
import com.example.data.model.Assignment
import com.example.data.model.AssignmentCategory
import com.example.data.model.EnglishChallenge
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PracticeSentence
import com.example.data.model.Priority
import com.example.data.repository.AccessWorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val testTag: String) {
    WORKSPACE("Workspace", "nav_workspace"),
    ACCENT_COACH("Accent Coach", "nav_accent"),
    AI_CHAT("AI Buddy", "nav_chat"),
    CHALLENGES("Challenges", "nav_challenges"),
    ASSIGNMENTS("Tasks", "nav_assignments")
}

sealed interface AccentEvaluationState {
    data object Idle : AccentEvaluationState
    data object Evaluating : AccentEvaluationState
    data class Success(val evaluation: AccentEvaluation) : AccentEvaluationState
    data class Error(val message: String) : AccentEvaluationState
}

class AccessViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = AccessWorkspaceRepository(database)

    val ttsHelper = TextToSpeechHelper(application)
    val speechHelper = SpeechRecognizerHelper(application)

    // Navigation
    private val _currentDestination = MutableStateFlow(AppDestination.WORKSPACE)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Workspace & Tasks
    val assignments = repository.allAssignments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val communityPosts = repository.allCommunityPosts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val accentRecords = repository.allAccentRecords.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val challenges = repository.allChallenges.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val leaderboard = repository.leaderboardFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val completedCount = repository.completedAssignmentCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val averageScore = repository.averageAccentScore.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Accent Coach state
    private val _selectedSentence = MutableStateFlow(AccessWorkspaceRepository.practiceSentences.first())
    val selectedSentence: StateFlow<PracticeSentence> = _selectedSentence.asStateFlow()

    private val _customTargetText = MutableStateFlow("")
    val customTargetText: StateFlow<String> = _customTargetText.asStateFlow()

    private val _isCustomMode = MutableStateFlow(false)
    val isCustomMode: StateFlow<Boolean> = _isCustomMode.asStateFlow()

    private val _accentEvalState = MutableStateFlow<AccentEvaluationState>(AccentEvaluationState.Idle)
    val accentEvalState: StateFlow<AccentEvaluationState> = _accentEvalState.asStateFlow()

    // AI Chat state
    private val _currentPersona = MutableStateFlow("SARAH") // "SARAH", "DEBATE", "INTERVIEW", "GENERAL"
    val currentPersona: StateFlow<String> = _currentPersona.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _chatInputText = MutableStateFlow("")
    val chatInputText: StateFlow<String> = _chatInputText.asStateFlow()

    val currentChatMessages = combine(_currentPersona, repository.allAssignments) { persona, _ ->
        persona
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SARAH")

    // Filter states for assignments
    private val _assignmentCategoryFilter = MutableStateFlow<AssignmentCategory?>(null)
    val assignmentCategoryFilter: StateFlow<AssignmentCategory?> = _assignmentCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // New assignment dialog
    private val _showAddAssignmentDialog = MutableStateFlow(false)
    val showAddAssignmentDialog: StateFlow<Boolean> = _showAddAssignmentDialog.asStateFlow()

    // New post dialog
    private val _showAddPostDialog = MutableStateFlow(false)
    val showAddPostDialog: StateFlow<Boolean> = _showAddPostDialog.asStateFlow()

    // Challenge details
    private val _activeChallenge = MutableStateFlow<EnglishChallenge?>(null)
    val activeChallenge: StateFlow<EnglishChallenge?> = _activeChallenge.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // --- ACCENT COACH ACTIONS ---
    fun selectSentence(sentence: PracticeSentence) {
        _selectedSentence.value = sentence
        _isCustomMode.value = false
        speechHelper.resetSpokenText()
        _accentEvalState.value = AccentEvaluationState.Idle
    }

    fun setCustomMode(enabled: Boolean) {
        _isCustomMode.value = enabled
        speechHelper.resetSpokenText()
        _accentEvalState.value = AccentEvaluationState.Idle
    }

    fun updateCustomTargetText(text: String) {
        _customTargetText.value = text
    }

    fun playSentenceAudio(text: String, slowRate: Boolean = false) {
        ttsHelper.speak(text, slowRate)
    }

    fun stopAudio() {
        ttsHelper.stop()
    }

    fun startListeningForAccent() {
        speechHelper.startListening()
    }

    fun stopListeningForAccent() {
        speechHelper.stopListening()
    }

    fun evaluateSpeech(targetText: String, spokenText: String) {
        if (targetText.isBlank() || spokenText.isBlank()) return
        _accentEvalState.value = AccentEvaluationState.Evaluating
        viewModelScope.launch {
            try {
                val eval = repository.evaluateAndSaveAccent(targetText, spokenText)
                _accentEvalState.value = AccentEvaluationState.Success(eval)
            } catch (e: Exception) {
                _accentEvalState.value = AccentEvaluationState.Error(e.message ?: "Evaluation failed")
            }
        }
    }

    // --- AI CHAT ACTIONS ---
    fun setPersona(persona: String) {
        _currentPersona.value = persona
    }

    fun updateChatInput(text: String) {
        _chatInputText.value = text
    }

    fun getChatMessagesFlow(persona: String) = repository.getChatMessages(persona)

    fun sendChatMessage() {
        val text = _chatInputText.value.trim()
        if (text.isBlank() || _isChatSending.value) return
        _chatInputText.value = ""
        _isChatSending.value = true
        viewModelScope.launch {
            try {
                val reply = repository.sendChatMessage(_currentPersona.value, text)
                // Optionally auto-speak preview or user can tap audio button
            } catch (_: Exception) {
            } finally {
                _isChatSending.value = false
            }
        }
    }

    fun clearCurrentChat() {
        viewModelScope.launch {
            repository.clearChat(_currentPersona.value)
        }
    }

    // --- ASSIGNMENTS & ACTIVITIES ACTIONS ---
    fun setCategoryFilter(category: AssignmentCategory?) {
        _assignmentCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAssignmentCompleted(assignment: Assignment) {
        viewModelScope.launch {
            repository.setAssignmentCompleted(assignment.id, !assignment.isCompleted)
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun setAddAssignmentDialogVisible(visible: Boolean) {
        _showAddAssignmentDialog.value = visible
    }

    fun createAssignment(
        title: String,
        description: String,
        category: AssignmentCategory,
        dueDate: String,
        priority: Priority
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertAssignment(
                Assignment(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    dueDate = dueDate.trim().ifEmpty { "Next Week" },
                    priority = priority,
                    isCompleted = false
                )
            )
            _showAddAssignmentDialog.value = false
        }
    }

    // --- COMMUNITY POSTS ---
    fun setAddPostDialogVisible(visible: Boolean) {
        _showAddPostDialog.value = visible
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            repository.likePost(postId)
        }
    }

    fun createPost(title: String, content: String, tag: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.addPost(
                author = "You (Access Scholar)",
                role = "Access Cohort 15",
                title = title.trim(),
                content = content.trim(),
                tag = tag.trim().ifEmpty { "General" }
            )
            _showAddPostDialog.value = false
        }
    }

    // --- CHALLENGES & LEADERBOARD ---
    fun setActiveChallenge(challenge: EnglishChallenge?) {
        _activeChallenge.value = challenge
    }

    fun completeChallenge(challengeId: Long, score: Int) {
        viewModelScope.launch {
            repository.completeChallenge(challengeId, score)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
        speechHelper.stopListening()
    }
}
