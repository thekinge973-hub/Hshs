package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AccentEvaluation
import com.example.data.model.AccentRecord
import com.example.data.model.Assignment
import com.example.data.model.AssignmentCategory
import com.example.data.model.ChallengeType
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityPost
import com.example.data.model.EnglishChallenge
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PracticeSentence
import com.example.data.model.Priority
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class AccessWorkspaceRepository(
    private val database: AppDatabase,
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val assignmentDao = database.assignmentDao()
    private val chatMessageDao = database.chatMessageDao()
    private val accentRecordDao = database.accentRecordDao()
    private val communityPostDao = database.communityPostDao()
    private val challengeDao = database.challengeDao()

    val allAssignments: Flow<List<Assignment>> = assignmentDao.getAllAssignments()
    val allCommunityPosts: Flow<List<CommunityPost>> = communityPostDao.getAllPosts()
    val allAccentRecords: Flow<List<AccentRecord>> = accentRecordDao.getAllRecords()
    val allChallenges: Flow<List<EnglishChallenge>> = challengeDao.getAllChallenges()
    val averageAccentScore: Flow<Double?> = accentRecordDao.getAverageScore()
    val completedAssignmentCount: Flow<Int> = assignmentDao.getCompletedCount()

    fun getChatMessages(persona: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForPersona(persona)
    }

    suspend fun insertAssignment(assignment: Assignment): Long {
        return assignmentDao.insertAssignment(assignment)
    }

    suspend fun setAssignmentCompleted(id: Long, completed: Boolean) {
        assignmentDao.setCompleted(id, completed)
    }

    suspend fun deleteAssignment(assignment: Assignment) {
        assignmentDao.deleteAssignment(assignment)
    }

    suspend fun sendChatMessage(persona: String, userText: String): String {
        // Save user message
        val userMsg = ChatMessage(sender = "USER", text = userText, persona = persona)
        chatMessageDao.insertMessage(userMsg)

        // Fetch recent messages for context
        val history = chatMessageDao.getMessagesForPersona(persona).first()

        // Call Gemini
        val reply = geminiClient.chatWithPersona(persona, userText, history)

        // Save AI reply
        val aiMsg = ChatMessage(sender = "AI", text = reply, persona = persona)
        chatMessageDao.insertMessage(aiMsg)

        return reply
    }

    suspend fun clearChat(persona: String) {
        chatMessageDao.clearMessagesForPersona(persona)
    }

    suspend fun evaluateAndSaveAccent(
        targetSentence: String,
        spokenSentence: String
    ): AccentEvaluation {
        val evaluation = geminiClient.evaluateAccent(targetSentence, spokenSentence)

        val record = AccentRecord(
            targetSentence = targetSentence,
            spokenText = spokenSentence,
            overallScore = evaluation.overallScore,
            phoneticFeedback = evaluation.phoneticBreakdown,
            intonationFeedback = evaluation.intonationFeedback,
            tips = evaluation.tipsForArabicSpeakers.joinToString("\n• ", prefix = "• ")
        )
        accentRecordDao.insertRecord(record)

        return evaluation
    }

    suspend fun completeChallenge(challengeId: Long, score: Int) {
        challengeDao.completeChallenge(challengeId, score)
    }

    suspend fun likePost(postId: Long) {
        communityPostDao.likePost(postId)
    }

    suspend fun addPost(author: String, role: String, title: String, content: String, tag: String) {
        val post = CommunityPost(
            author = author,
            role = role,
            title = title,
            content = content,
            tag = tag
        )
        communityPostDao.insertPost(post)
    }

    /**
     * Dynamically builds the Leaderboard combined with real student progress.
     */
    val leaderboardFlow: Flow<List<LeaderboardEntry>> = combine(
        allAccentRecords,
        allChallenges,
        allAssignments
    ) { accents, challenges, assignments ->
        val completedChallenges = challenges.filter { it.isCompleted }
        val completedAssignments = assignments.filter { it.isCompleted }

        val accentAvg = if (accents.isNotEmpty()) {
            (accents.map { it.overallScore }.average()).toInt()
        } else {
            82
        }

        // Calculate current user's live points
        val userChallengePoints = completedChallenges.sumOf { it.pointsReward }
        val userAssignmentPoints = completedAssignments.size * 60
        val userAccentPoints = accents.size * 40
        val currentUserBasePoints = 850 + userChallengePoints + userAssignmentPoints + userAccentPoints

        val basePeers = listOf(
            LeaderboardEntry(0, "Yasmine Al-Khatib", "Access Cohort 15", "YA", 1420, 94, 8, "Accent Star 🏆", false),
            LeaderboardEntry(0, "Omar Khalil", "Access Cohort 15", "OK", 1280, 91, 7, "Debate Champion 🎙️", false),
            LeaderboardEntry(0, "Noor Haddad", "Access Cohort 15", "NH", 1150, 89, 6, "Cultural Ambassador 🌍", false),
            LeaderboardEntry(0, "Zaid Mansoor", "Access Cohort 15", "ZM", 970, 86, 5, "Rising Leader 🚀", false),
            LeaderboardEntry(0, "Layla Saleh", "Access Cohort 15", "LS", 890, 84, 4, "Community Hero 🤝", false),
            LeaderboardEntry(0, "Tariq Nabil", "Access Cohort 15", "TN", 760, 80, 3, "Word Smith 📚", false)
        )

        val currentUserEntry = LeaderboardEntry(
            rank = 0,
            name = "You (Access Scholar)",
            role = "Access Cohort 15",
            avatarInitials = "ME",
            points = currentUserBasePoints,
            accentAvg = accentAvg,
            challengesCompleted = completedChallenges.size,
            badge = when {
                currentUserBasePoints >= 1300 -> "Master Orator 👑"
                currentUserBasePoints >= 1000 -> "Accent Virtuoso 🌟"
                else -> "Active Access Scholar ✨"
            },
            isCurrentUser = true
        )

        val allList = (basePeers + currentUserEntry).sortedByDescending { it.points }
        allList.mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingAssignments = assignmentDao.getAllAssignments().first()
        if (existingAssignments.isEmpty()) {
            assignmentDao.insertAll(
                listOf(
                    Assignment(
                        title = "Community Service Initiative Proposal",
                        description = "Draft a 1-page proposal in English detailing your team's local environmental cleanup project for Amideast Access Day.",
                        category = AssignmentCategory.COMMUNITY,
                        dueDate = "Tomorrow, 5:00 PM",
                        priority = Priority.HIGH,
                        isCompleted = false
                    ),
                    Assignment(
                        title = "Accent & Speech: MLK 'I Have a Dream' Excerpt",
                        description = "Record your pronunciation of the famous speech stanza focusing on American retroflex 'r' and rhythmic pauses.",
                        category = AssignmentCategory.SPEAKING,
                        dueDate = "Oct 8, 11:59 PM",
                        priority = Priority.MEDIUM,
                        isCompleted = false
                    ),
                    Assignment(
                        title = "Access Cultural Exchange Video Log",
                        description = "Prepare a 2-minute introductory video talking about your local cultural traditions to share with US exchange partners.",
                        category = AssignmentCategory.ACTIVITY,
                        dueDate = "Oct 12, 6:00 PM",
                        priority = Priority.HIGH,
                        isCompleted = false
                    ),
                    Assignment(
                        title = "Vocabulary Log: Idioms of Leadership",
                        description = "Collect 5 American leadership idioms (e.g., 'lead by example', 'think outside the box') and write original sentences.",
                        category = AssignmentCategory.ASSIGNMENT,
                        dueDate = "Oct 15, 4:00 PM",
                        priority = Priority.LOW,
                        isCompleted = true
                    )
                )
            )
        }

        val existingPosts = communityPostDao.getAllPosts().first()
        if (existingPosts.isEmpty()) {
            communityPostDao.insertAll(
                listOf(
                    CommunityPost(
                        author = "Ms. Eleanor Vance",
                        role = "Amideast Access Coordinator",
                        title = "Welcome to Access Cohort 15 Collaborative Workspace! 🇺🇸🤝",
                        content = "Hello Access Scholars! Welcome to our shared digital hub. Here you can hone your American accent with our AI coach, chat with mentors, and collaborate on your weekly civic engagement projects. Make sure to check the challenges board!",
                        likes = 18,
                        tag = "Announcement"
                    ),
                    CommunityPost(
                        author = "Omar Khalil",
                        role = "Student (Cohort 15)",
                        title = "Tip for practicing the /p/ vs /b/ sound in English",
                        content = "Hey everyone! Coach Sarah gave me a tip during office hours: place a tissue paper in front of your mouth. When saying 'Peter Piper' the paper should fly forward! It helped my accent score jump from 78 to 92.",
                        likes = 14,
                        tag = "Study Tip"
                    ),
                    CommunityPost(
                        author = "Yasmine Al-Khatib",
                        role = "Student (Cohort 15)",
                        title = "Debate Club topic ideas for this Thursday",
                        content = "Who wants to practice the upcoming debate on 'Renewable Energy in Developing Nations'? Drop a message if you want to brainstorm arguments together!",
                        likes = 9,
                        tag = "Debate"
                    )
                )
            )
        }

        val existingChallenges = challengeDao.getAllChallenges().first()
        if (existingChallenges.isEmpty()) {
            challengeDao.insertAll(
                listOf(
                    EnglishChallenge(
                        title = "The Peter Piper Accent Sprint",
                        description = "Master the explosive voiceless /p/ sound without confusing it with /b/.",
                        type = ChallengeType.SPEED_TWISTER,
                        targetPrompt = "Peter Piper picked a peck of pickled peppers in Portland.",
                        pointsReward = 120,
                        difficulty = "Medium",
                        isCompleted = false
                    ),
                    EnglishChallenge(
                        title = "Access Leadership Idioms Quiz",
                        description = "What does the American idiom 'to step up to the plate' mean?",
                        type = ChallengeType.VOCAB_QUIZ,
                        targetPrompt = "Choose the correct meaning of 'to step up to the plate':",
                        options = listOf(
                            "To prepare a meal for guests",
                            "To take responsibility and act in a critical moment",
                            "To clean the dining table after a meeting",
                            "To argue against the team leader"
                        ),
                        correctIndex = 1,
                        pointsReward = 80,
                        difficulty = "Easy",
                        isCompleted = false
                    ),
                    EnglishChallenge(
                        title = "The 'TH' Sound Clarity Duel",
                        description = "Articulate both voiced /ð/ and unvoiced /θ/ with the tongue between teeth.",
                        type = ChallengeType.PRONUNCIATION,
                        targetPrompt = "Thirty-three thousand thinkers thought throughout Thursday.",
                        pointsReward = 150,
                        difficulty = "Hard",
                        isCompleted = false
                    ),
                    EnglishChallenge(
                        title = "60-Second Impromptu Speaking Spark",
                        description = "Speak spontaneously on how education empowers youth in your hometown.",
                        type = ChallengeType.DEBATE_PROMPT,
                        targetPrompt = "Youth empowerment through education is the most powerful catalyst for positive civic change.",
                        pointsReward = 200,
                        difficulty = "Advanced",
                        isCompleted = false
                    ),
                    EnglishChallenge(
                        title = "American Vowel Reductions & Schwa",
                        description = "Practice the natural unstressed /ə/ schwa sound in connected American speech.",
                        type = ChallengeType.PRONUNCIATION,
                        targetPrompt = "A photograph of an American photographer in California.",
                        pointsReward = 100,
                        difficulty = "Medium",
                        isCompleted = false
                    )
                )
            )
        }
    }

    companion object {
        val practiceSentences = listOf(
            PracticeSentence(
                id = "1",
                category = "P vs B Sound",
                sentence = "Please put the plastic paper clips into the purple pouch.",
                phoneticFocus = "Voiceless bilabial plosive /p/ with crisp puff of air.",
                difficulty = "Easy"
            ),
            PracticeSentence(
                id = "2",
                category = "TH Sounds (/θ/ & /ð/)",
                sentence = "They thought that their northern brother breathed through the thick cloth.",
                phoneticFocus = "Tongue tip placed between incisors for dental fricatives.",
                difficulty = "Medium"
            ),
            PracticeSentence(
                id = "3",
                category = "American Retroflex R",
                sentence = "Robert ordered rural roasted corn from the farmers market.",
                phoneticFocus = "Curled back tongue tip without touching the roof of the mouth.",
                difficulty = "Hard"
            ),
            PracticeSentence(
                id = "4",
                category = "Connected Speech & Flap T",
                sentence = "Better water bottle in the city center at little cost.",
                phoneticFocus = "American flap [ɾ] between vowels instead of harsh stop [t].",
                difficulty = "Medium"
            ),
            PracticeSentence(
                id = "5",
                category = "Access Presentation Skills",
                sentence = "Our community initiative empowers young leaders to foster sustainable solutions.",
                phoneticFocus = "Rhythmic thought grouping and confident intonation cadence.",
                difficulty = "Advanced"
            ),
            PracticeSentence(
                id = "6",
                category = "Debate & Argumentation",
                sentence = "While opponents argue otherwise, empirical evidence demonstrates substantial progress.",
                phoneticFocus = "Formal syllable stress and clear consonant cluster articulation.",
                difficulty = "Advanced"
            )
        )
    }
}
