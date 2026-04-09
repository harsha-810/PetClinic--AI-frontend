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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.network.Appointment
import com.example.petclinicapp.network.UpdateStatusRequest
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.utils.DateUtils
import com.example.petclinicapp.ui.components.LogVaccinationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onLogout: () -> Unit,
    onPrescribeClick: (Int) -> Unit,
    onPetHistoryClick: (Int) -> Unit,
    onStatsClick: () -> Unit,
    onChangePassword: () -> Unit,
    onProfileClick: () -> Unit
) {
    var queue by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Accepted", "Completed", "Rejected")
    
    var showRejectDialog by remember { mutableStateOf<Appointment?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    var selectedPrescription by remember { mutableStateOf<com.example.petclinicapp.network.Prescription?>(null) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    var showVaccinationDialog by remember { mutableStateOf(false) }
    var vaccinationPetId by remember { mutableIntStateOf(0) }
    var vaccinationPetName by remember { mutableStateOf("") }

    fun loadQueue() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.getDoctorQueue()
                if (response.isSuccessful) {
                    queue = (response.body() ?: emptyList()).sortedBy { it.priorityLevel }
                } else {
                    errorMessage = "Failed to load queue"
                }
            } catch (_: Exception) {
                errorMessage = "Network Error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadQueue() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Portal", fontWeight = FontWeight.Bold, color = TextDark) },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.Info, contentDescription = "Stats", tint = ClinicTeal)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = OceanBlue)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundWhite)

        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(ClinicTeal, OceanBlue)),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Hello, Dr. ${RetrofitClient.userName}!", 
                        color = Color.White, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your medical queue is ready for triage.", 
                        color = Color.White.copy(alpha = 0.8f), 
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Online", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = ClinicTeal,
                modifier = Modifier.padding(horizontal = 24.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ClinicTeal,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title, 
                                fontSize = 11.sp, 
                                maxLines = 1, 
                                softWrap = false,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        selectedContentColor = ClinicTeal,
                        unselectedContentColor = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClinicTeal)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color.Red)
                }
            } else {
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    modifier = Modifier.weight(1f)
                ) { targetIndex ->
                    val filteredQueue = queue.filter { appointment ->
                        when (targetIndex) {
                            0 -> appointment.status == "Pending"
                            1 -> appointment.status == "Accepted"
                            2 -> appointment.status == "Completed"
                            3 -> appointment.status == "Rejected"
                            else -> false
                        }
                    }

                    if (filteredQueue.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(), 
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ClinicTeal.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No tasks in this section", fontWeight = FontWeight.Medium, color = TextGray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp, start = 24.dp, end = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredQueue, key = { it.id }) { appointment ->
                                DoctorAppointmentCard(
                                    appointment = appointment,
                                    onAccept = {
                                        scope.launch {
                                            RetrofitClient.api.updateAppointmentStatus(appointment.id, UpdateStatusRequest("Accepted"))
                                            loadQueue()
                                        }
                                    },
                                    onReject = { showRejectDialog = appointment },
                                    onPrescribe = { onPrescribeClick(appointment.id) },
                                    onHistory = { onPetHistoryClick(appointment.pet?.id ?: 0) },
                                    onViewPrescription = { 
                                        selectedPrescription = it
                                        showPrescriptionDialog = true
                                    },
                                    onLogVaccination = { petId, petName ->
                                        vaccinationPetId = petId
                                        vaccinationPetName = petName
                                        showVaccinationDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showRejectDialog != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showRejectDialog = null 
                        rejectionReason = "" 
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White,
                    title = { Text("Reject Request", fontWeight = FontWeight.Bold, color = TextDark) },
                    text = {
                        Column {
                            Text("Provide a reason for the client:", color = TextGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = rejectionReason,
                                onValueChange = { rejectionReason = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Schedule full, etc...") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicTeal, focusedLabelColor = ClinicTeal)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (rejectionReason.isNotBlank()) {
                                    scope.launch {
                                        val appointmentId = showRejectDialog!!.id
                                        RetrofitClient.api.updateAppointmentStatus(appointmentId, UpdateStatusRequest("Rejected", rejectionReason))
                                        showRejectDialog = null
                                        rejectionReason = ""
                                        loadQueue()
                                    }
                                }
                            },
                            enabled = rejectionReason.isNotBlank()
                        ) {
                            Text("Confirm Rejection", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showRejectDialog = null
                            rejectionReason = "" 
                        }) {
                            Text("Cancel", color = TextGray)
                        }
                    }
                )
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

            if (showVaccinationDialog) {
                LogVaccinationDialog(
                    petName = vaccinationPetName,
                    onDismiss = { showVaccinationDialog = false },
                    onConfirm = { name, last, next ->
                        scope.launch {
                            try {
                                val request = com.example.petclinicapp.network.Vaccination(
                                    petId = vaccinationPetId,
                                    vaccineName = name,
                                    lastDate = last,
                                    nextDueDate = next
                                )
                                val response = RetrofitClient.api.doctorAddVaccination(request)
                                if (response.isSuccessful) {
                                    showVaccinationDialog = false
                                }
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }
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
fun DoctorAppointmentCard(
    appointment: Appointment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onPrescribe: () -> Unit,
    onHistory: (Int) -> Unit,
    onViewPrescription: (com.example.petclinicapp.network.Prescription) -> Unit = {},
    onLogVaccination: (Int, String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.pet?.name ?: "Pet", 
                        color = TextDark, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Owner: ${appointment.pet?.patient?.name ?: "N/A"}", 
                        color = TextGray, 
                        fontSize = 13.sp
                    )
                    Text(
                        text = DateUtils.formatDateTime(appointment.date),
                        color = OceanBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Surface(
                    color = if (appointment.priorityLevel == 1) Color.Red.copy(alpha = 0.1f) else ClinicTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (appointment.priorityLevel == 1) "Urgent" else "Regular", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (appointment.priorityLevel == 1) Color.Red else ClinicTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundWhite, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Chief Complaint:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            Text(
                text = appointment.symptoms ?: "General Checkup", 
                color = TextGray, 
                fontSize = 14.sp, 
                maxLines = 2
            )
            
            if (!appointment.aiCondition.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = OceanBlue.copy(alpha = 0.05f), 
                    shape = RoundedCornerShape(12.dp), 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI: ${appointment.aiCondition} (${appointment.aiSeverity})", 
                            fontSize = 12.sp, 
                            color = OceanBlue, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (appointment.status == "Pending") {
                    Button(
                        onClick = onAccept, 
                        modifier = Modifier.weight(1.5f).height(44.dp),
                        shape = RoundedCornerShape(12.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                    ) {
                        Text("Accept", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    OutlinedButton(
                        onClick = onReject, 
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Reject", color = Color.Red, fontSize = 13.sp)
                    }
                } else if (appointment.status == "Accepted") {
                    Button(
                        onClick = onPrescribe,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                    ) {
                        Text("Start Consultation", fontWeight = FontWeight.Bold)
                    }
                } else if (appointment.status == "Completed") {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ClinicTeal.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Consultation Finalized", fontWeight = FontWeight.Bold, color = ClinicTeal, fontSize = 13.sp)
                            }
                            
                            if (appointment.prescription != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onViewPrescription(appointment.prescription) },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                                ) {
                                    Text("View Medical Report", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                
                if (appointment.status != "Rejected") {
                    IconButton(
                        onClick = { onLogVaccination(appointment.pet?.id ?: 0, appointment.pet?.name ?: "Pet") },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ClinicTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Vaccinate", tint = ClinicTeal)
                    }
                }

                if (appointment.status != "Completed") {
                    IconButton(
                        onClick = { appointment.pet?.id?.let { onHistory(it) } },
                        modifier = Modifier
                            .size(44.dp)
                            .background(BackgroundWhite, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.List, contentDescription = "History", tint = TextDark)
                    }
                }
            }
        }
    }
}
