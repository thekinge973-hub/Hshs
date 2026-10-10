package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ChatMessage
import com.example.ui.AccessViewModel
import com.example.ui.theme.AccessCyan
import com.example.ui.theme.AccessError
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary

@Composable
fun AiChatScreen(
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val isSending by viewModel.isChatSending.collectAsStateWithLifecycle()
    val inputText by viewModel.chatInputText.collectAsStateWithLifecycle()
    val isSpeakingTTS by viewModel.ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val isListeningMic by viewModel.speechHelper.isListening.collectAsStateWithLifecycle()
    val spokenTextLive by viewModel.speechHelper.spokenText.collectAsStateWithLifecycle()

    val chatMessagesFlow = remember(currentPersona) { viewModel.getChatMessagesFlow(currentPersona) }
    val messages by chatMessagesFlow.collectAsStateWithLifecycle(emptyList())

    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }

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

    // When voice input transcribed, populate or append to text
    LaunchedEffect(spokenTextLive) {
        if (spokenTextLive.isNotEmpty()) {
            viewModel.updateChatInput(spokenTextLive)
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val personas = listOf(
        Triple("SARAH", "Coach Sarah 🇺🇸", "Access Native Mentor"),
        Triple("DEBATE", "Alex (Debate) 🎙️", "Debate & Persuasion"),
        Triple("INTERVIEW", "Ms. Taylor 📋", "Scholarship Interview Prep"),
        Triple("GENERAL", "English Tutor 💡", "Grammar & Questions")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_chat_screen")
    ) {
        // --- TOP HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(AccessNavyPrimary, AccessNavyDark)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Access AI Speaking Buddy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Converse, practice & ask anything in English",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- PERSONA SELECTOR BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personas.forEach { (id, name, desc) ->
                val isSelected = currentPersona == id
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.setPersona(id) }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) AccessGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // --- SUGGESTED CONVERSATION PROMPTS ---
        val starterChips = when (currentPersona) {
            "SARAH" -> listOf(
                "Tell me an authentic American idiom!",
                "How was your day, Coach Sarah?",
                "What's a good tip for speaking more fluently?",
                "Let's talk about youth leadership in my town."
            )
            "DEBATE" -> listOf(
                "Is artificial intelligence good for students?",
                "Should homework be abolished?",
                "Should public transport be completely free?"
            )
            "INTERVIEW" -> listOf(
                "Ask me a tough scholarship question.",
                "How do I answer 'Tell me about yourself'?",
                "Give me feedback on my leadership story."
            )
            else -> listOf(
                "Explain the difference between lend and borrow.",
                "How do I use 'ought to' vs 'should'?",
                "Suggest 3 words to replace 'very good'."
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            starterChips.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.updateChatInput(prompt)
                            viewModel.sendChatMessage()
                        }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- MESSAGES LIST ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccessNavyPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Chat",
                                    tint = AccessNavyPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Start speaking with ${personas.firstOrNull { it.first == currentPersona }?.second ?: "Mentor"}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type below or tap the microphone to talk with your voice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    isSpeakingTTS = isSpeakingTTS,
                    onSpeak = { text ->
                        if (isSpeakingTTS) viewModel.stopAudio()
                        else viewModel.playSentenceAudio(text)
                    }
                )
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = AccessNavyPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Coach is thinking...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- BOTTOM INPUT BAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = {
                        if (isListeningMic) {
                            viewModel.speechHelper.stopListening()
                        } else {
                            if (hasMicPermission) {
                                viewModel.speechHelper.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isListeningMic) AccessError else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListeningMic) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Speak input",
                        tint = if (isListeningMic) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateChatInput(it) },
                    placeholder = {
                        Text(
                            text = if (isListeningMic) "Listening... speak now" else "Chat or ask anything in English...",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccessNavyPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = { viewModel.sendChatMessage() },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (inputText.isNotBlank() && !isSending) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isSending) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Conversation") },
            text = { Text("Are you sure you want to delete all messages for this mentor persona?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCurrentChat()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = AccessError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSpeakingTTS: Boolean,
    onSpeak: (String) -> Unit
) {
    val isUser = message.sender == "USER"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = AccessNavyPrimary,
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) AccessNavyPrimary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isUser) 0.dp else 3.dp,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccessNavyPrimary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSpeak(message.text) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen to pronunciation",
                                    tint = AccessNavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Listen Aloud",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccessNavyPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
