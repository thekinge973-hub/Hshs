package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Assignment
import com.example.data.model.AssignmentCategory
import com.example.data.model.Priority
import com.example.ui.AccessViewModel
import com.example.ui.components.CategoryPill
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.AccessGold
import com.example.ui.theme.AccessNavyDark
import com.example.ui.theme.AccessNavyPrimary
import com.example.ui.theme.AccessSuccess

@Composable
fun AssignmentsScreen(
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier
) {
    val allAssignments by viewModel.assignments.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.assignmentCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddAssignmentDialog.collectAsStateWithLifecycle()

    val filteredAssignments = allAssignments.filter { assignment ->
        val matchesCategory = categoryFilter == null || assignment.category == categoryFilter
        val matchesQuery = searchQuery.isBlank() ||
                assignment.title.contains(searchQuery, ignoreCase = true) ||
                assignment.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("assignments_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tasks & Activities",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Homework, projects, speaking & community events",
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
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = "Tasks",
                                tint = AccessNavyDark
                            )
                        }
                    }
                }
            }

            // --- SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search homework, activities or projects...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("assignments_search_field")
            )

            // --- CATEGORY FILTER CHIPS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (categoryFilter == null) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.setCategoryFilter(null) }
                ) {
                    Text(
                        text = "All Tasks (${allAssignments.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (categoryFilter == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (categoryFilter == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                AssignmentCategory.entries.forEach { cat ->
                    val isSelected = categoryFilter == cat
                    val count = allAssignments.count { it.category == cat }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setCategoryFilter(cat) }
                    ) {
                        Text(
                            text = "${cat.displayName} ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- TASK LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredAssignments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tasks found in this view.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the '+' button below to add an assignment or activity!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                items(filteredAssignments, key = { it.id }) { assignment ->
                    AssignmentItemCard(
                        assignment = assignment,
                        onToggle = { viewModel.toggleAssignmentCompleted(assignment) },
                        onDelete = { viewModel.deleteAssignment(assignment) }
                    )
                }
            }
        }

        // --- FLOATING ACTION BUTTON ---
        FloatingActionButton(
            onClick = { viewModel.setAddAssignmentDialogVisible(true) },
            containerColor = AccessGold,
            contentColor = AccessNavyDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_assignment_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Activity or Assignment"
            )
        }
    }

    if (showAddDialog) {
        AddAssignmentDialog(
            onDismiss = { viewModel.setAddAssignmentDialogVisible(false) },
            onCreate = { title, desc, cat, dueDate, priority ->
                viewModel.createAssignment(title, desc, cat, dueDate, priority)
            }
        )
    }
}

@Composable
private fun AssignmentItemCard(
    assignment: Assignment,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (assignment.isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = assignment.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccessSuccess,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        CategoryPill(category = assignment.category)
                        Spacer(modifier = Modifier.width(6.dp))
                        PriorityBadge(priority = assignment.priority)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Due: ${assignment.dueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (assignment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAssignmentDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, category: AssignmentCategory, dueDate: String, priority: Priority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AssignmentCategory.ASSIGNMENT) }
    var dueDate by remember { mutableStateOf("Next Session") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Assignment or Activity", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions or Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssignmentCategory.entries.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat) AccessNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { prio ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedPriority == prio) AccessGold else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPriority = prio }
                        ) {
                            Text(
                                text = prio.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPriority == prio) AccessNavyDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, selectedCategory, dueDate, selectedPriority)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccessNavyPrimary)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
