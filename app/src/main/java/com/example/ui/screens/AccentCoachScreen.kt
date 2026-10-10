package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccentEvaluation
import com.example.data.repository.AccessWorkspaceRepository
import com.example.ui.AccentEvaluationState
import com.example.ui.AccessViewModel
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.ScoreGauge
import com.example.ui.theme.AccessCyan
import com.example.ui.theme.AccessError
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.AccessSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentCoachScreen(
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedSentence by viewModel.selectedSentence.collectAsStateWithLifecycle()
    val isCustomMode by viewModel.isCustomMode.collectAsStateWithLifecycle()
    val customTargetText by viewModel.customTargetText.collectAsStateWithLifecycle()
    val evalState by viewModel.accentEvalState.collectAsStateWithLifecycle()
    val isSpeakingTTS by viewModel.ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val isListeningMic by viewModel.speechHelper.isListening.collectAsStateWithLifecycle()
    val spokenTextLive by viewModel.speechHelper.spokenText.collectAsStateWithLifecycle()
    val micError by viewModel.speechHelper.errorMessage.collectAsStateWithLifecycle()
    val pastRecords by viewModel.accentRecords.collectAsStateWithLifecycle()

    var manualSpokenInput by remember { mutableStateOf("") }
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
            viewModel.startListeningForAccent()
        }
    }

    val currentTargetText = if (isCustomMode) customTargetText else selectedSentence.sentence
    val activeSpokenText = spokenTextLive.ifEmpty { manualSpokenInput }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("accent_coach_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- TOP HEADER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AccessNavyPrimary, AccessNavyDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AI Accent Coach 🎙️",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "American English pronunciation evaluation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = AccessGold,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Powered",
                                    tint = AccessNavyDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SENTENCE CATEGORY SELECTOR CHIPS ---
        item {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    text = "Select Practice Sound or Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccessWorkspaceRepository.practiceSentences.forEach { sentenceItem ->
                        val isSelected = !isCustomMode && selectedSentence.id == sentenceItem.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectSentence(sentenceItem) }
                        ) {
                            Text(
                                text = sentenceItem.category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Custom sentence option
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCustomMode) AccessGold else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setCustomMode(true) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Sentence",
                                tint = if (isCustomMode) AccessNavyDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Custom Sentence",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isCustomMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCustomMode) AccessNavyDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- TARGET SENTENCE CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET AMERICAN ENGLISH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccessNavyPrimary,
                            letterSpacing = 1.sp
                        )

                        if (!isCustomMode) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccessCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedSentence.difficulty,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AccessCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isCustomMode) {
                        OutlinedTextField(
                            value = customTargetText,
                            onValueChange = { viewModel.updateCustomTargetText(it) },
                            placeholder = { Text("Type any English sentence you want to practice...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    } else {
                        Text(
                            text = "\"${selectedSentence.sentence}\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Focus",
                                    tint = AccessGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Focus: ${selectedSentence.phoneticFocus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Listen Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioPlayButton(
                            onClick = {
                                if (isSpeakingTTS) viewModel.stopAudio()
                                else viewModel.playSentenceAudio(currentTargetText, slowRate = false)
                            },
                            isPlaying = isSpeakingTTS,
                            label = "Listen US Native",
                            testTag = "listen_normal_button",
                            modifier = Modifier.weight(1f)
                        )

                        AudioPlayButton(
                            onClick = {
                                if (isSpeakingTTS) viewModel.stopAudio()
                                else viewModel.playSentenceAudio(currentTargetText, slowRate = true)
                            },
                            isPlaying = isSpeakingTTS,
                            label = "Slow & Clear",
                            testTag = "listen_slow_button",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- RECORDING / SPEAKING CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR RECORDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Mic Button
                    Surface(
                        shape = CircleShape,
                        color = if (isListeningMic) AccessError else AccessNavyPrimary,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isListeningMic) {
                                    viewModel.stopListeningForAccent()
                                } else {
                                    if (hasMicPermission) {
                                        viewModel.startListeningForAccent()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("record_mic_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListeningMic) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListeningMic) "Stop listening" else "Start recording",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isListeningMic) "Listening... Speak clearly now! 🎙️" else "Tap microphone & speak sentence",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isListeningMic) FontWeight.Bold else FontWeight.Normal,
                        color = if (isListeningMic) AccessError else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (micError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = micError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccessError
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech transcription result display / manual edit
                    OutlinedTextField(
                        value = if (spokenTextLive.isNotEmpty()) spokenTextLive else manualSpokenInput,
                        onValueChange = { manualSpokenInput = it },
                        label = { Text("What you spoke (or edit transcription):") },
                        placeholder = { Text("Speech transcription will appear here...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Evaluate Button
                    Button(
                        onClick = {
                            viewModel.evaluateSpeech(currentTargetText, activeSpokenText)
                        },
                        enabled = currentTargetText.isNotBlank() && activeSpokenText.isNotBlank() && evalState !is AccentEvaluationState.Evaluating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccessGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("evaluate_accent_button")
                    ) {
                        if (evalState is AccentEvaluationState.Evaluating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = AccessNavyDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analyzing with Gemini AI...",
                                color = AccessNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Evaluate",
                                tint = AccessNavyDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Evaluate Accent with AI",
                                color = AccessNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- EVALUATION RESULT SECTION ---
        item {
            AnimatedVisibility(
                visible = evalState is AccentEvaluationState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (evalState is AccentEvaluationState.Success) {
                    val eval = (evalState as AccentEvaluationState.Success).evaluation
                    EvaluationResultCard(eval = eval, onPlayWord = { word ->
                        viewModel.playSentenceAudio(word)
                    })
                }
            }
        }

        // --- PAST EVALUATION RECORDS ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Practice History",
                        tint = AccessNavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accent Practice History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (pastRecords.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "No recorded sessions yet. Tap the mic and evaluate your first sentence!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    pastRecords.take(5).forEach { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.targetSentence,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    val formattedDate = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(record.timestamp))
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (record.overallScore >= 85) AccessSuccess.copy(alpha = 0.15f) else AccessGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${record.overallScore}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.overallScore >= 85) AccessSuccess else AccessGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvaluationResultCard(
    eval: AccentEvaluation,
    onPlayWord: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI ACCENT ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccessNavyPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = eval.clarityRating,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            eval.overallScore >= 90 -> AccessSuccess
                            eval.overallScore >= 80 -> AccessCyan
                            else -> AccessGold
                        }
                    )
                }

                ScoreGauge(score = eval.overallScore, size = 88, strokeWidth = 8)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Summary Feedback:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = eval.summaryFeedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phonetic & Intonation breakdown
            Text(
                text = "Phonetics & Rhythm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• ${eval.phoneticBreakdown}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• ${eval.intonationFeedback}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Word-by-word Breakdown
            Text(
                text = "Word-by-Word Clarity (Tap word to hear native sound):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                eval.words.forEach { w ->
                    val color = when {
                        w.score >= 88 -> AccessSuccess
                        w.score >= 75 -> AccessCyan
                        else -> AccessGold
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = color.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPlayWord(w.word) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Hear ${w.word}",
                                tint = color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${w.word} (${w.score}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tips for Arabic Speakers
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AccessGold.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tips",
                            tint = AccessGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tips for Arabic ESL Scholars:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccessNavyDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    eval.tipsForArabicSpeakers.forEach { tip ->
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccessNavyDark
                        )
                    }
                }
            }
        }
    }
}
