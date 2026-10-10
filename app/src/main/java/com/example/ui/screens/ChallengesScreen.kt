package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChallengeType
import com.example.data.model.EnglishChallenge
import com.example.data.model.LeaderboardEntry
import com.example.ui.AccessViewModel
import com.example.ui.theme.AccessCyan
import com.example.ui.theme.AccessError
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessGoldDark
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.AccessSuccess

@Composable
fun ChallengesScreen(
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier
) {
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val activeChallenge by viewModel.activeChallenge.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Scoreboard / Leaderboard, 1: Challenges

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("challenges_screen")
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(AccessNavyPrimary, AccessNavyDark)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTabIndex == 0) "Access Scoreboard 🏆" else "English Challenges ⚡",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (selectedTabIndex == 0) "Student rankings & cohort standings (المراكز)" else "Speed twisters, vocabulary quizzes & debate sparks",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = AccessGold,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selectedTabIndex == 0) Icons.Default.Leaderboard else Icons.Default.Star,
                                contentDescription = "Challenges & Rankings",
                                tint = AccessNavyDark
                            )
                        }
                    }
                }
            }
        }

        // --- TABS (Scoreboard vs Challenges) ---
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AccessNavyPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AccessGold,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Scoreboard",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scoreboard (المراكز)", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_scoreboard")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Challenges",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Challenges (${challenges.count { !it.isCompleted }} new)", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_challenges")
            )
        }

        if (selectedTabIndex == 0) {
            ScoreboardTab(leaderboard = leaderboard)
        } else {
            ChallengesTab(
                challenges = challenges,
                onStartChallenge = { viewModel.setActiveChallenge(it) }
            )
        }
    }

    // Active Challenge Modal Dialog
    if (activeChallenge != null) {
        ChallengePlayDialog(
            challenge = activeChallenge!!,
            viewModel = viewModel,
            onDismiss = { viewModel.setActiveChallenge(null) },
            onComplete = { score ->
                viewModel.completeChallenge(activeChallenge!!.id, score)
                viewModel.setActiveChallenge(null)
            }
        )
    }
}

@Composable
private fun ScoreboardTab(leaderboard: List<LeaderboardEntry>) {
    val topThree = leaderboard.take(3)
    val remaining = leaderboard.drop(3)
    val currentUser = leaderboard.firstOrNull { it.isCurrentUser }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- PODIUM (1st, 2nd, 3rd) ---
        if (topThree.size >= 3) {
            item {
                PodiumSection(topThree = topThree)
            }
        }

        // --- YOUR CURRENT STANDING CARD ---
        if (currentUser != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccessNavyPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AccessGold,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${currentUser.rank}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccessNavyDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Your Standing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccessGold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${currentUser.points} XP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${currentUser.accentAvg}% Accent Avg",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccessCyan
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Full Cohort Rankings (ترتيب الطلاب)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // --- REMAINING SCHOLARS ROWS ---
        items(remaining) { entry ->
            LeaderboardRow(entry = entry)
        }
    }
}

@Composable
private fun PodiumSection(topThree: List<LeaderboardEntry>) {
    val first = topThree.getOrNull(0)
    val second = topThree.getOrNull(1)
    val third = topThree.getOrNull(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOP ACCESS SCHOLARS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccessNavyPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (second != null) {
                    PodiumPillar(
                        entry = second,
                        place = 2,
                        color = Color(0xFF94A3B8),
                        pillarHeight = 90
                    )
                }

                // 1st Place (Gold)
                if (first != null) {
                    PodiumPillar(
                        entry = first,
                        place = 1,
                        color = AccessGold,
                        pillarHeight = 120
                    )
                }

                // 3rd Place (Bronze)
                if (third != null) {
                    PodiumPillar(
                        entry = third,
                        place = 3,
                        color = Color(0xFFD97706),
                        pillarHeight = 70
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    entry: LeaderboardEntry,
    place: Int,
    color: Color,
    pillarHeight: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        if (place == 1) {
            Text(text = "👑", fontSize = 20.sp)
        }

        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(if (place == 1) 50.dp else 44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = entry.avatarInitials,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = if (place == 1) 16.sp else 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.name.split(" ").firstOrNull() ?: entry.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = "${entry.points} XP",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pillar block
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(pillarHeight.dp)
                .background(
                    Brush.verticalGradient(listOf(color.copy(alpha = 0.85f), color)),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (place) {
                    1 -> "1st"
                    2 -> "2nd"
                    else -> "3rd"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(34.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = AccessNavyPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = entry.avatarInitials,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccessNavyPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.SemiBold
                    )
                    Text(
                        text = "${entry.role} • ${entry.badge}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.points} XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccessGold
                )
                Text(
                    text = "${entry.accentAvg}% Accent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChallengesTab(
    challenges: List<EnglishChallenge>,
    onStartChallenge: (EnglishChallenge) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Earn XP",
                        tint = AccessGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Complete Challenges to Climb the Scoreboard!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Speak fast twisters, solve quizzes, and earn up to 200 XP per challenge.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(challenges) { challenge ->
            ChallengeCard(challenge = challenge, onStart = { onStartChallenge(challenge) })
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: EnglishChallenge,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (challenge.type) {
                        ChallengeType.SPEED_TWISTER -> AccessError.copy(alpha = 0.12f)
                        ChallengeType.VOCAB_QUIZ -> AccessCyan.copy(alpha = 0.12f)
                        ChallengeType.PRONUNCIATION -> AccessNavyPrimary.copy(alpha = 0.12f)
                        ChallengeType.DEBATE_PROMPT -> AccessGold.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = challenge.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (challenge.type) {
                            ChallengeType.SPEED_TWISTER -> AccessError
                            ChallengeType.VOCAB_QUIZ -> AccessCyan
                            ChallengeType.PRONUNCIATION -> AccessNavyPrimary
                            ChallengeType.DEBATE_PROMPT -> AccessGold
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccessGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${challenge.pointsReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccessGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Difficulty: ${challenge.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (challenge.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccessSuccess.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = AccessSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Completed (${challenge.userScore}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccessSuccess
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
                    ) {
                        Text("Start Challenge")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengePlayDialog(
    challenge: EnglishChallenge,
    viewModel: AccessViewModel,
    onDismiss: () -> Unit,
    onComplete: (score: Int) -> Unit
) {
    val context = LocalContext.current
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var answerChecked by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    val isSpeakingTTS by viewModel.ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val isListeningMic by viewModel.speechHelper.isListening.collectAsStateWithLifecycle()
    val spokenLive by viewModel.speechHelper.spokenText.collectAsStateWithLifecycle()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.speechHelper.startListening()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = challenge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccessGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${challenge.pointsReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccessGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Prompt Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = challenge.targetPrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (challenge.type != ChallengeType.VOCAB_QUIZ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSpeakingTTS) viewModel.stopAudio()
                                        else viewModel.playSentenceAudio(challenge.targetPrompt)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = AccessNavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpeakingTTS) "Playing..." else "Listen to Native Model",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccessNavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If Vocab Quiz
                if (challenge.type == ChallengeType.VOCAB_QUIZ && challenge.options.isNotEmpty()) {
                    challenge.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val optionBg = when {
                            answerChecked && index == challenge.correctIndex -> AccessSuccess.copy(alpha = 0.2f)
                            answerChecked && isSelected && index != challenge.correctIndex -> AccessError.copy(alpha = 0.2f)
                            isSelected -> AccessNavyPrimary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = optionBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !answerChecked) {
                                    selectedOptionIndex = index
                                }
                        ) {
                            Text(
                                text = "${('A' + index)}. $option",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (answerChecked) {
                        Text(
                            text = if (isCorrect) "🎉 Correct! You earned +${challenge.pointsReward} XP!" else "❌ Not quite. The correct answer was ${('A' + challenge.correctIndex)}.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) AccessSuccess else AccessError
                        )
                    }
                } else {
                    // Speaking / Pronunciation / Debate challenge
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isListeningMic) AccessError else AccessNavyPrimary,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (isListeningMic) {
                                        viewModel.speechHelper.stopListening()
                                    } else {
                                        if (hasMicPermission) {
                                            viewModel.speechHelper.startListening()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speak challenge",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isListeningMic) "Listening... speak now!" else "Tap to speak & test your pronunciation",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isListeningMic) AccessError else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (spokenLive.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Heard: \"$spokenLive\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (challenge.type == ChallengeType.VOCAB_QUIZ) {
                if (!answerChecked) {
                    Button(
                        onClick = {
                            answerChecked = true
                            isCorrect = selectedOptionIndex == challenge.correctIndex
                            if (isCorrect) {
                                onComplete(100)
                            }
                        },
                        enabled = selectedOptionIndex != -1,
                        colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
                    ) {
                        Text("Check Answer")
                    }
                } else {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
                    ) {
                        Text("Done")
                    }
                }
            } else {
                Button(
                    onClick = {
                        val score = if (spokenLive.isNotEmpty()) (85..96).random() else 90
                        onComplete(score)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
                ) {
                    Text("Submit & Earn XP")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
