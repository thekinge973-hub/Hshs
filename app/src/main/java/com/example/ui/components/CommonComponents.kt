package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignmentCategory
import com.example.data.model.Priority
import com.example.ui.theme.AccessCyan
import com.example.ui.theme.AccessError
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.AccessSuccess

@Composable
fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Int = 110,
    strokeWidth: Int = 10
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetProgress = (score.coerceIn(0, 100)) / 100f

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 90 -> AccessSuccess
        score >= 80 -> AccessCyan
        score >= 70 -> AccessGold
        else -> AccessError
    }

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = strokeWidth.dp
        )
        CircularProgressIndicator(
            progress = { animatedProgress.value },
            modifier = Modifier.fillMaxSize(),
            color = scoreColor,
            strokeWidth = strokeWidth.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Text(
                text = "Accent",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryPill(category: AssignmentCategory, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (category) {
        AssignmentCategory.ASSIGNMENT -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        AssignmentCategory.ACTIVITY -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        AssignmentCategory.SPEAKING -> Color(0xFFECFDF5) to Color(0xFF047857)
        AssignmentCategory.COMMUNITY -> Color(0xFFF3E8FF) to Color(0xFF6D28D9)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (color, text) = when (priority) {
        Priority.HIGH -> AccessError to "High"
        Priority.MEDIUM -> AccessGold to "Medium"
        Priority.LOW -> AccessSuccess to "Low"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AudioPlayButton(
    onClick: () -> Unit,
    isPlaying: Boolean,
    label: String = "Listen",
    testTag: String = "audio_play_button",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isPlaying) AccessGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = label,
                tint = if (isPlaying) AccessGold else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPlaying) "Playing..." else label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isPlaying) AccessGold else MaterialTheme.colorScheme.primary
            )
        }
    }
}
