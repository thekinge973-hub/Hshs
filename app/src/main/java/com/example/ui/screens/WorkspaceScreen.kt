package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CommunityPost
import com.example.ui.AccessViewModel
import com.example.ui.AppDestination
import com.example.ui.components.CategoryPill
import com.example.ui.theme.AccessCyan
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.AccessSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier
) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val posts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val averageScore by viewModel.averageScore.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedCount.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val showAddPostDialog by viewModel.showAddPostDialog.collectAsStateWithLifecycle()

    val currentUserEntry = leaderboard.firstOrNull { it.isCurrentUser }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- HEADER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AccessNavyPrimary, AccessNavyDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AccessGold,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Amideast Access",
                                        tint = AccessNavyDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AMIDEAST ACCESS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Collaborative Scholar Workspace",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Cohort 15 🌟",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccessGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hero Banner Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.amideast_banner),
                                contentDescription = "Amideast Access Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Welcome back, Scholar!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sharpen your American accent & collaborate today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- STATS OVERVIEW ROW ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accent Avg Card
                StatCard(
                    title = "Accent Avg",
                    value = if (averageScore != null) "${averageScore!!.toInt()}%" else "88%",
                    subtext = "AI Pronunciation",
                    icon = Icons.Default.Speed,
                    tint = AccessCyan,
                    modifier = Modifier.weight(1f)
                )

                // Tasks Done
                StatCard(
                    title = "Tasks Done",
                    value = "$completedCount / ${assignments.size}",
                    subtext = "Homework & Events",
                    icon = Icons.Default.CheckCircle,
                    tint = AccessSuccess,
                    modifier = Modifier.weight(1f)
                )

                // Current Rank & XP
                StatCard(
                    title = "Cohort Rank",
                    value = "#${currentUserEntry?.rank ?: 4}",
                    subtext = "${currentUserEntry?.points ?: 970} XP",
                    icon = Icons.Default.EmojiEvents,
                    tint = AccessGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- QUICK ACTIONS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Practice & AI Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Accent Coach",
                        desc = "Rate your pronunciation",
                        icon = Icons.Default.Mic,
                        color = AccessNavyPrimary,
                        testTag = "quick_accent_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.ACCENT_COACH) }
                    )

                    ActionTile(
                        title = "AI Mentor Chat",
                        desc = "Converse with Coach Sarah",
                        icon = Icons.Default.ChatBubbleOutline,
                        color = AccessGold,
                        testTag = "quick_chat_button",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.AI_CHAT) }
                    )
                }
            }
        }

        // --- UPCOMING DEADLINES / ACTIVITIES PREVIEW ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Access Tasks & Activities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppDestination.ASSIGNMENTS) }) {
                        Text("View All")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View all tasks",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val pendingTasks = assignments.filter { !it.isCompleted }.take(3)
                if (pendingTasks.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "🎉 All caught up! No pending assignments.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    pendingTasks.forEach { assignment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleAssignmentCompleted(assignment) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Complete task",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        CategoryPill(category = assignment.category)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Due ${assignment.dueDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- COMMUNITY WORKSPACE BOARD ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Access Collaborative Board",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Share speaking topics, ask questions & collaborate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { viewModel.setAddPostDialogVisible(true) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("new_post_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New post",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(posts) { post ->
            CommunityPostCard(
                post = post,
                onLike = { viewModel.likePost(post.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }

    if (showAddPostDialog) {
        NewPostDialog(
            onDismiss = { viewModel.setAddPostDialogVisible(false) },
            onSubmit = { title, content, tag ->
                viewModel.createPost(title, content, tag)
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AccessNavyPrimary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.author.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.author,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = post.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onLike)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.likes} Helpful",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Speaking Tip") }

    val tags = listOf("Speaking Tip", "Study Group", "Debate", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share with Access Scholars", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Post Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What's on your mind or what do you want to ask?") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category Tag",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTag == tag) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTag = tag }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTag == tag) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSubmit(title, content, selectedTag)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
