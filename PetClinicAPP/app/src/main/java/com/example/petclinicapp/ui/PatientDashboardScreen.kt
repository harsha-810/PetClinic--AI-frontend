package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.currentStateAsState
import com.example.petclinicapp.network.Pet
import com.example.petclinicapp.network.Appointment
import com.example.petclinicapp.network.UpdateStatusRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.utils.DateUtils
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onProfileClick: () -> Unit,
    onAddPetClick: () -> Unit,
    onBookAppointmentClick: (Int) -> Unit,
    onPetHistoryClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpcomingAppointmentClick: (Int) -> Unit,
    onAiAnalysisClick: () -> Unit
) {
    // Silence unused parameter warnings for now (planned for implementation)
    val _unusedNavigation = listOf(onLogout, onChangePassword, onProfileClick)
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // scope is not used in this function, so it can be removed

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            isLoading = true
            try {
                val response = RetrofitClient.api.getMyPets()
                if (response.isSuccessful) {
                    pets = response.body() ?: emptyList()
                    errorMessage = ""
                } else {
                    errorMessage = "Failed to load pets: ${response.code()}"
                }
            } catch (_: Exception) {
                errorMessage = "Network Error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
            errorMessage = ""
        }
    }

    if (showCancelDialog && appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                if (!isCancelling) showCancelDialog = false 
            },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment for ${DateUtils.formatDateTime(appointmentToCancel?.date)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val appointmentId = appointmentToCancel?.id ?: return@Button
                        isCancelling = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.api.cancelAppointment(appointmentId)
                                if (response.isSuccessful) {
                                    // Update local state
                                    pets = pets.map { pet ->
                                        if (pet.appointments?.any { it.id == appointmentId } == true) {
                                            pet.copy(appointments = pet.appointments.map { appt ->
                                                if (appt.id == appointmentId) appt.copy(status = "Cancelled") else appt
                                            })
                                        } else {
                                            pet
                                        }
                                    }
                                    showCancelDialog = false
                                } else {
                                    errorMessage = "Failed to cancel: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error: ${e.message}"
                            } finally {
                                isCancelling = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isCancelling
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false },
                    enabled = !isCancelling
                ) {
                    Text("No")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetClinic Dashboard", fontWeight = FontWeight.Bold, color = TextDark) },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ClinicTeal)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OceanBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPetClick,
                containerColor = ClinicTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Pet", fontWeight = FontWeight.Bold) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(com.example.petclinicapp.ui.theme.BackgroundWhite)
                    .padding(start = 24.dp, end = 24.dp) // Standardized spacing
            ) {
                // No extra top spacer for seamless alignment

                // Welcome Header
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicTeal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(
                            Brush.linearGradient(listOf(ClinicTeal, OceanBlue))
                        ).padding(24.dp)
                    ) {
                        Column {
                            val greeting = remember {
                                when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
                                    in 0..11 -> "Good Morning"
                                    in 12..16 -> "Good Afternoon"
                                    else -> "Good Evening"
                                }
                            }
                            Text(
                                text = "$greeting, ${RetrofitClient.userName}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            // Redundant name line removed to satisfy lint "Good Morning, Name"
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "How is your furry family today?",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // AI Health Assistant Banner (Premium Design)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clickable { onAiAnalysisClick() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)), // Very light blue
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OceanBlue.copy(alpha=0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = OceanBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.HealthAndSafety,
                                    contentDescription = "Gemini Health Assistant",
                                    tint = OceanBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Gemini Health Assistant",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                "Instant symptom check & advice",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "Your Furry Family",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else if (pets.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.size(120.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = ClinicTeal.copy(alpha = 0.2f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No pets added yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextDark
                        )
                        Text(
                            "Add your first pet to start tracking!",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(pets, key = { it.id }) { pet ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        // Card content stays the same...
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    pet.name ?: "Unknown Pet",
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Surface(
                                                        color = ClinicTeal.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = pet.species ?: "Other",
                                                            modifier = Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 2.dp
                                                            ),
                                                            color = ClinicTeal,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "${pet.breed ?: "N/A"} • ${pet.age ?: 0} yrs",
                                                        color = TextGray,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                                color = Color(0xFFFFE1E1), // Soft Pink
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.Favorite,
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Button(
                                                    onClick = { onBookAppointmentClick(pet.id) },
                                                    modifier = Modifier.weight(1f).height(48.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                                                ) {
                                                    Text(
                                                        "Book Visit",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = { onPetHistoryClick(pet.id) },
                                                    modifier = Modifier.weight(1f).height(48.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        ClinicTeal
                                                    )
                                                ) {
                                                    Text(
                                                        "Medical Records",
                                                        color = ClinicTeal,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }

                                        if (!pet.appointments.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(20.dp))
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = OceanBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Upcoming Appointment",
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            pet.appointments.take(1).forEach { appointment ->
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth()
                                                        .padding(top = 12.dp)
                                                        .clickable { onUpcomingAppointmentClick(pet.id) },
                                                    color = OceanBlue.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp)
                                                            .fillMaxWidth(),
                                                         horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                DateUtils.formatDateTime(appointment.date),
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextDark,
                                                                fontSize = 14.sp
                                                            )
                                                            Text(
                                                                appointment.status ?: "Pending",
                                                                fontSize = 12.sp,
                                                                color = if (appointment.status == "Cancelled") Color.Red else ClinicTeal,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        
                                                        if (appointment.status != "Cancelled" && appointment.status != "Completed" && appointment.status != "Rejected") {
                                                            TextButton(
                                                                onClick = {
                                                                    appointmentToCancel = appointment
                                                                    showCancelDialog = true
                                                                },
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Text("Cancel", color = Color.Red, fontSize = 12.sp)
                                                            }
                                                        }
                                                        
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                            contentDescription = null,
                                                            tint = OceanBlue,
                                                            modifier = Modifier.size(14.dp)
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
            }
        }
    }
}