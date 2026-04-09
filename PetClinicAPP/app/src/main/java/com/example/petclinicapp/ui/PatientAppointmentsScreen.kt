package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.network.Appointment
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.petclinicapp.utils.DateUtils
import com.example.petclinicapp.network.UpdateStatusRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentsScreen(
    onBack: () -> Unit,
    petIdFilter: Int? = null,
    onlyUpcoming: Boolean = false
) {
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedPrescription by remember { mutableStateOf<com.example.petclinicapp.network.Prescription?>(null) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }
    
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            isLoading = true
            try {
                val response = RetrofitClient.api.getMyAppointments()
                if (response.isSuccessful) {
                    appointments = response.body() ?: emptyList()
                    errorMessage = ""
                } else {
                    errorMessage = "Failed to load appointments: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.message}"
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
                                    appointments = appointments.map { appt ->
                                        if (appt.id == appointmentId) appt.copy(status = "Cancelled") else appt
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
                title = { Text("My Visits", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        Box(modifier = Modifier.padding(padding).fillMaxSize().background(com.example.petclinicapp.ui.theme.BackgroundWhite)) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(600, easing = FastOutSlowInEasing))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ClinicTeal.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = ClinicTeal)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Your Appointments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Scheduled visits for your furry family", color = TextGray, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ClinicTeal)
                        }
                    } else if (errorMessage.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(errorMessage, color = Color.Red)
                        }
                    } else if (appointments.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ClinicTeal.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No appointments yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                            Text(
                                "Your scheduled visits will appear here", 
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        val filteredAppointments = if (petIdFilter != null || onlyUpcoming) {
                            appointments.filter { appt ->
                                val matchesPet = petIdFilter == null || appt.petId == petIdFilter
                                val matchesUpcoming = if (onlyUpcoming) {
                                    appt.status?.contains("Pending", ignoreCase = true) == true || 
                                    appt.status?.contains("Accepted", ignoreCase = true) == true
                                } else true
                                matchesPet && matchesUpcoming
                            }
                        } else {
                            appointments
                        }

                        if (filteredAppointments.isEmpty() && (petIdFilter != null || onlyUpcoming)) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = ClinicTeal.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No upcoming appointments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                                Text(
                                    "Your scheduled visits for this criteria will appear here", 
                                    color = TextGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp, start = 24.dp, end = 24.dp)
                            ) {
                                val pending = filteredAppointments.filter { it.status?.contains("Pending", ignoreCase = true) == true }
                                val accepted = filteredAppointments.filter { it.status?.contains("Accepted", ignoreCase = true) == true }
                                val completed = filteredAppointments.filter { it.status?.contains("Completed", ignoreCase = true) == true }
                                val rejected = filteredAppointments.filter { it.status?.contains("Rejected", ignoreCase = true) == true }

                                if (pending.isNotEmpty()) {
                                    item { SectionHeader("Pending Confirmation", ClinicTeal) }
                                    items(pending, key = { it.id }) { appointment ->
                                        AppointmentCard(
                                            petName = appointment.pet?.name ?: "Pet", 
                                            appointment = appointment,
                                            onCancel = { 
                                                appointmentToCancel = it
                                                showCancelDialog = true
                                            }
                                        )
                                    }
                                }

                                if (accepted.isNotEmpty()) {
                                    item { SectionHeader("Upcoming Visits", Color(0xFF4CAF50)) }
                                    items(accepted, key = { it.id }) { appointment ->
                                        AppointmentCard(
                                            petName = appointment.pet?.name ?: "Pet", 
                                            appointment = appointment,
                                            onCancel = { 
                                                appointmentToCancel = it
                                                showCancelDialog = true
                                            }
                                        )
                                    }
                                }

                                if (!onlyUpcoming && completed.isNotEmpty()) {
                                    item { SectionHeader("Past Visits", OceanBlue) }
                                    items(completed, key = { it.id }) { appointment ->
                                        AppointmentCard(appointment.pet?.name ?: "Pet", appointment, onViewPrescription = {
                                            selectedPrescription = it
                                            showPrescriptionDialog = true
                                        })
                                    }
                                }

                                if (!onlyUpcoming && rejected.isNotEmpty()) {
                                    item { SectionHeader("Cancelled", Color.Red) }
                                    items(rejected, key = { it.id }) { appointment ->
                                        AppointmentCard(appointment.pet?.name ?: "Pet", appointment)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        
            if (showPrescriptionDialog && selectedPrescription != null) {
                AlertDialog(
                    onDismissRequest = { showPrescriptionDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White,
                    title = { Text("Prescription Detail", fontWeight = FontWeight.Bold, color = TextDark) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PrescriptionItem("Diagnosis", selectedPrescription?.diagnosis ?: "N/A")
                            PrescriptionItem("Medicines", selectedPrescription?.medicines ?: "N/A")
                            PrescriptionItem("Advice", selectedPrescription?.advice ?: "N/A")
                            Text(
                                text = "Issued on: ${selectedPrescription?.date ?: "N/A"}", 
                                color = TextGray, 
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrescriptionDialog = false }) {
                            Text("Dismiss", color = ClinicTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp, 16.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
    }
}

@Composable
private fun PrescriptionItem(label: String, content: String) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, color = ClinicTeal, fontSize = 12.sp)
        Text(content, color = TextDark, fontSize = 14.sp)
    }
}

@Composable
fun AppointmentCard(
    petName: String, 
    appointment: Appointment, 
    onViewPrescription: (com.example.petclinicapp.network.Prescription) -> Unit = {},
    onCancel: (Appointment) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClinicTeal.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(petName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                        Text(com.example.petclinicapp.utils.DateUtils.formatDateTime(appointment.date), color = TextGray, fontSize = 13.sp)
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(appointment.status) {
                        "Accepted" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "Rejected" -> Color.Red.copy(alpha = 0.1f)
                        "Completed" -> OceanBlue.copy(alpha = 0.1f)
                        else -> ClinicTeal.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = appointment.status ?: "Pending",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when(appointment.status) {
                            "Accepted" -> Color(0xFF4CAF50)
                            "Rejected" -> Color.Red
                            "Completed" -> OceanBlue
                            else -> ClinicTeal
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Doctor: ${appointment.doctor?.name ?: "Assigned Soon"}", color = TextDark, fontSize = 14.sp)
            }
            
            if (appointment.status == "Rejected" && !appointment.rejectionReason.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    color = Color.Red.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reason: ${appointment.rejectionReason}", 
                        color = Color.Red, 
                        fontSize = 13.sp, 
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            val isUpcoming = appointment.status?.contains("Pending", ignoreCase = true) == true || 
                            appointment.status?.contains("Accepted", ignoreCase = true) == true

            if (isUpcoming) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onCancel(appointment) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Cancel Appointment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            if (appointment.status == "Completed" && appointment.prescription != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onViewPrescription(appointment.prescription) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Text("View Medical Report", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
