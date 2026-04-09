package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.network.Notification
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientNotificationsScreen(onBack: () -> Unit) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun loadNotifications() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.getNotifications()
                if (response.isSuccessful) {
                    notifications = response.body() ?: emptyList()
                    errorMessage = ""
                } else {
                    errorMessage = "Failed to load notifications."
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteNotification(id: Int) {
        // Optimistic update
        val previousNotifications = notifications
        notifications = notifications.filter { it.id != id }
        
        scope.launch {
            try {
                val response = RetrofitClient.api.deleteNotification(id)
                if (!response.isSuccessful) {
                    notifications = previousNotifications
                    val errorMsg = response.errorBody()?.string() ?: "No error body"
                    Toast.makeText(context, "Failed (${response.code()}): $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                notifications = previousNotifications
                Toast.makeText(context, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearAllNotifications() {
        val previousNotifications = notifications
        notifications = emptyList()
        showClearDialog = false
        
        scope.launch {
            try {
                val response = RetrofitClient.api.clearAllNotifications()
                if (!response.isSuccessful) {
                    notifications = previousNotifications
                    val errorMsg = response.errorBody()?.string() ?: "No error body"
                    Toast.makeText(context, "Failed (${response.code()}): $errorMsg", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "All notifications cleared", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                notifications = previousNotifications
                Toast.makeText(context, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) { loadNotifications() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear All Notifications") },
                text = { Text("Are you sure you want to delete all notifications? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { clearAllNotifications() }) {
                        Text("Clear All", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClinicTeal.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Activity Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Stay updated on medical progress", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading && notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else if (errorMessage.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = Color.Red)
                    }
                } else if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No new medical alerts.", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationCard(
                                notif = notif,
                                onClick = {
                                    if (!notif.isRead) {
                                        scope.launch {
                                            RetrofitClient.api.markNotificationAsRead(notif.id)
                                            loadNotifications()
                                        }
                                    }
                                },
                                onDelete = { deleteNotification(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: Notification, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notif.isRead) ClinicTeal.copy(alpha = 0.03f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notif.isRead) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = if (!notif.isRead) ClinicTeal.copy(alpha = 0.1f) else BackgroundWhite,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (!notif.isRead) ClinicTeal else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = if (!notif.isRead) "New Update" else "Medical Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!notif.isRead) ClinicTeal else TextGray
                    )
                    Text(
                        text = DateUtils.formatDateTime(notif.date),
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notif.message,
                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                    color = TextDark,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (!notif.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = ClinicTeal,
                    modifier = Modifier.size(8.dp).align(Alignment.CenterVertically)
                ) { }
            }
        }
    }
}
