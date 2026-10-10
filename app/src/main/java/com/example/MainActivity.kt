package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AccessViewModel
import com.example.ui.AppDestination
import com.example.ui.screens.AccentCoachScreen
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AssignmentsScreen
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AccessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AccessApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AccessApp(viewModel: AccessViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()

    // Handle back button to return to Workspace if in a sub-screen
    BackHandler(enabled = currentDestination != AppDestination.WORKSPACE) {
        viewModel.navigateTo(AppDestination.WORKSPACE)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(AppDestination.WORKSPACE, "Hub", Icons.Default.School),
                    Triple(AppDestination.ACCENT_COACH, "Accent", Icons.Default.Mic),
                    Triple(AppDestination.AI_CHAT, "AI Tutor", Icons.Default.Forum),
                    Triple(AppDestination.CHALLENGES, "Rankings", Icons.Default.EmojiEvents),
                    Triple(AppDestination.ASSIGNMENTS, "Tasks", Icons.AutoMirrored.Filled.Assignment)
                )

                navItems.forEach { (destination, label, icon) ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(destination) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccessNavyDark,
                            selectedTextColor = AccessNavyPrimary,
                            indicatorColor = AccessGold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.WORKSPACE -> WorkspaceScreen(viewModel = viewModel)
                AppDestination.ACCENT_COACH -> AccentCoachScreen(viewModel = viewModel)
                AppDestination.AI_CHAT -> AiChatScreen(viewModel = viewModel)
                AppDestination.CHALLENGES -> ChallengesScreen(viewModel = viewModel)
                AppDestination.ASSIGNMENTS -> AssignmentsScreen(viewModel = viewModel)
            }
        }
    }
}
