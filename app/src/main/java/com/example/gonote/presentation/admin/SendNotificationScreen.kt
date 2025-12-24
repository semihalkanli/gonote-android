package com.example.gonote.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gonote.ui.theme.AccentBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationScreen(
    usersState: AdminUsersState,
    onBackClick: () -> Unit,
    onSendNotification: (List<String>?, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("all") } // "all" or "selected"
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSuccess by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Success snackbar
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar(
                message = "Notification sent successfully!",
                duration = SnackbarDuration.Short
            )
            showSuccess = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Send Notification",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        val userIds = if (selectedOption == "all") {
                            null
                        } else {
                            selectedUserIds.toList()
                        }
                        onSendNotification(userIds, title, message)
                        title = ""
                        message = ""
                        selectedUserIds = emptySet()
                        showSuccess = true
                    }
                },
                icon = { Icon(Icons.Default.Send, contentDescription = "Send") },
                text = { Text("Send Notification") },
                containerColor = AccentBlue
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                placeholder = { Text("e.g., New Feature Available") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    focusedLabelColor = AccentBlue
                )
            )

            // Message Input
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                placeholder = { Text("Enter your notification message...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    focusedLabelColor = AccentBlue
                )
            )

            Divider()

            // Recipient Selection
            Text(
                text = "Recipients",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(
                modifier = Modifier.selectableGroup()
            ) {
                // All Users Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedOption == "all",
                            onClick = { selectedOption = "all" },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == "all",
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "All Users",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Send to everyone (${usersState.users.size} users)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Selected Users Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedOption == "selected",
                            onClick = { selectedOption = "selected" },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == "selected",
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Selected Users",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (selectedUserIds.isEmpty()) {
                                "No users selected"
                            } else {
                                "${selectedUserIds.size} user(s) selected"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // User Selection List (only show if "selected" is chosen)
            if (selectedOption == "selected") {
                Divider()
                Text(
                    text = "Select Users",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                usersState.users.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedUserIds.contains(user.userId),
                            onCheckedChange = { checked ->
                                selectedUserIds = if (checked) {
                                    selectedUserIds + user.userId
                                } else {
                                    selectedUserIds - user.userId
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentBlue
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = user.email,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${user.noteCount} notes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}



