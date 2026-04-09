package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.AvailableDoctor
import com.example.petclinicapp.network.BookAppointmentRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookAppointmentScreen(
    petId: Int,
    initialSymptoms: String = "",
    initialDuration: String = "",
    priorityLevel: Int = 3,
    onCancel: () -> Unit,
    onAppointmentBooked: () -> Unit
) {
    var doctors by remember { mutableStateOf<List<AvailableDoctor>>(emptyList()) }
    var pets by remember { mutableStateOf<List<com.example.petclinicapp.network.Pet>>(emptyList()) }
    var uniqueClinics by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedClinic by remember { mutableStateOf("") }
    
    var selectedDoctorId by remember { mutableStateOf(0) }
    var selectedPetId by remember { mutableStateOf(petId) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    
    val commonSymptoms = listOf("Loss of appetite", "Vomiting", "Diarrhea", "Lethargy", "Coughing", "Sneezing", "Itching", "Limping", "Other")
    var selectedSymptomsList by remember { mutableStateOf(setOf<String>()) }
    var manualSymptomDescription by remember { mutableStateOf("") }
    
    val durationOptions = listOf("Less than 24 hours", "1-2 days", "3-5 days", "About a week", "More than a week")
    var selectedDuration by remember { mutableStateOf("") }
    var durationExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isBookingLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var petExpanded by remember { mutableStateOf(false) }
    var clinicExpanded by remember { mutableStateOf(false) }
    var doctorExpanded by remember { mutableStateOf(false) }
    var selectedDoctorName by remember { mutableStateOf("Select Doctor") }
    var selectedPetName by remember { mutableStateOf("Select Pet") }
    
    LaunchedEffect(initialSymptoms) {
        if (initialSymptoms.isNotBlank()) {
            val parts = initialSymptoms.split(",").map { it.trim() }
            selectedSymptomsList = parts.filter { it in commonSymptoms }.toSet()
            val manualParts = parts.filter { it !in commonSymptoms }
            if (manualParts.isNotEmpty()) {
                selectedSymptomsList = selectedSymptomsList + "Other"
                manualSymptomDescription = manualParts.joinToString(", ")
            }
        }
    }
    
    LaunchedEffect(initialDuration) {
        if (initialDuration.isNotBlank() && initialDuration in durationOptions) {
            selectedDuration = initialDuration
        }
    }

    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis
    
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        false
    )

    LaunchedEffect(Unit) {
        try {
            val petsRes = RetrofitClient.api.getMyPets()
            if (petsRes.isSuccessful) {
                pets = petsRes.body() ?: emptyList()
                if (selectedPetId != 0) {
                    selectedPetName = pets.find { it.id == selectedPetId }?.name ?: "Select Pet"
                }
            }

            val res = RetrofitClient.api.getAvailableDoctors()
            if (res.isSuccessful) {
                doctors = res.body() ?: emptyList()
            }

            val clinicsRes = RetrofitClient.api.getAllClinics()
            if (clinicsRes.isSuccessful && !clinicsRes.body().isNullOrEmpty()) {
                uniqueClinics = clinicsRes.body()!!
            } else {
                uniqueClinics = doctors.mapNotNull { it.hospitalName }.distinct().filter { it.isNotBlank() }.sorted()
            }
        } catch (e: Exception) {
            errorMessage = "Connection error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Visit", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundWhite)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClinicTeal)
                }
            } else {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
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
                            Text("Schedule a Visit", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Provide details for your pet's appointment", color = TextGray, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Pet Selection
                            BookingDropdown(
                                label = "Pet Patient",
                                value = selectedPetName,
                                expanded = petExpanded,
                                onExpandedChange = { petExpanded = it },
                                leadingIcon = Icons.Default.Person
                            ) {
                                pets.forEach { petObj ->
                                    DropdownMenuItem(
                                        text = { Text("${petObj.name} (${petObj.species})") },
                                        onClick = {
                                            selectedPetId = petObj.id
                                            selectedPetName = petObj.name ?: "Unnamed"
                                            petExpanded = false
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Clinic Selection
                            BookingDropdown(
                                label = "Clinic",
                                value = if (selectedClinic.isEmpty()) "Select Clinic" else selectedClinic,
                                expanded = clinicExpanded,
                                onExpandedChange = { clinicExpanded = it },
                                leadingIcon = Icons.Default.LocationOn
                            ) {
                                uniqueClinics.forEach { cName ->
                                    DropdownMenuItem(
                                        text = { Text(cName) },
                                        onClick = {
                                            selectedClinic = cName
                                            selectedDoctorId = 0
                                            selectedDoctorName = "Select Doctor"
                                            clinicExpanded = false
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Doctor Selection
                            if (selectedClinic.isNotEmpty()) {
                                BookingDropdown(
                                    label = "Doctor",
                                    value = selectedDoctorName,
                                    expanded = doctorExpanded,
                                    onExpandedChange = { doctorExpanded = it },
                                    leadingIcon = Icons.Default.Person
                                ) {
                                    val filteredDoctors = doctors.filter { it.hospitalName == selectedClinic }
                                    filteredDoctors.forEach { doc ->
                                        DropdownMenuItem(
                                            text = { Text("${doc.name} (${doc.specialization})") },
                                            onClick = {
                                                selectedDoctorId = doc.id
                                                selectedDoctorName = "Dr. ${doc.name}"
                                                doctorExpanded = false
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Date & Time
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = selectedDate, 
                                    onValueChange = {}, 
                                    label = { Text("Date", fontSize = 12.sp) },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, contentDescription = null, tint = ClinicTeal) } },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicTeal, focusedLabelColor = ClinicTeal)
                                )
                                OutlinedTextField(
                                    value = selectedTime, 
                                    onValueChange = {}, 
                                    label = { Text("Time", fontSize = 12.sp) },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = { IconButton(onClick = { timePickerDialog.show() }) { Icon(Icons.Default.Add, contentDescription = null, tint = ClinicTeal) } },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicTeal, focusedLabelColor = ClinicTeal)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = BackgroundWhite, thickness = 2.dp)
                            Spacer(modifier = Modifier.height(24.dp))

                            // Symptoms
                            Text("Symptoms", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                commonSymptoms.forEach { symptom ->
                                    FilterChip(
                                        selected = selectedSymptomsList.contains(symptom),
                                        onClick = {
                                            selectedSymptomsList = if (selectedSymptomsList.contains(symptom)) {
                                                selectedSymptomsList - symptom
                                            } else {
                                                selectedSymptomsList + symptom
                                            }
                                        },
                                        label = { Text(symptom) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ClinicTeal.copy(alpha = 0.1f),
                                            selectedLabelColor = ClinicTeal,
                                            selectedLeadingIconColor = ClinicTeal
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selectedSymptomsList.contains(symptom),
                                            borderColor = TextGray.copy(alpha = 0.3f),
                                            selectedBorderColor = ClinicTeal,
                                            borderWidth = 1.dp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Duration
                            BookingDropdown(
                                label = "Duration of Symptoms",
                                value = if (selectedDuration.isEmpty()) "How long?" else selectedDuration,
                                expanded = durationExpanded,
                                onExpandedChange = { durationExpanded = it },
                                leadingIcon = Icons.Default.DateRange
                            ) {
                                durationOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            selectedDuration = opt
                                            durationExpanded = false
                                        }
                                    )
                                }
                            }

                            if (selectedSymptomsList.contains("Other") || selectedSymptomsList.isEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = manualSymptomDescription,
                                    onValueChange = { manualSymptomDescription = it },
                                    label = { Text("Details (Optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicTeal, focusedLabelColor = ClinicTeal)
                                )
                            }
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            isBookingLoading = true
                            errorMessage = ""
                            scope.launch {
                                try {
                                    val finalSymptoms = (selectedSymptomsList.filter { it != "Other" } + (if (manualSymptomDescription.isNotBlank()) listOf(manualSymptomDescription) else emptyList())).joinToString(", ")
                                    val finalDateTimeString = "${selectedDate}T${selectedTime}"
                                    val req = BookAppointmentRequest(
                                        petId = selectedPetId,
                                        doctorId = selectedDoctorId,
                                        date = finalDateTimeString,
                                        symptoms = finalSymptoms,
                                        duration = selectedDuration,
                                        aiCondition = "",
                                        aiSeverity = "",
                                        priorityLevel = priorityLevel
                                    )
                                    val res = RetrofitClient.api.bookAppointment(req)
                                    if (res.isSuccessful) {
                                        showConfirmDialog = true
                                    } else {
                                        errorMessage = "Failed to book appointment. Please try again."
                                    }
                                } catch (_: Exception) {
                                    errorMessage = "Network Error. Check your connection."
                                } finally {
                                    isBookingLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                        enabled = !isBookingLoading && selectedDoctorId != 0 && selectedDate.isNotBlank() && selectedTime.isNotBlank() && (selectedSymptomsList.isNotEmpty() || manualSymptomDescription.isNotBlank()) && selectedDuration.isNotBlank() && selectedPetId != 0
                    ) {
                        if (isBookingLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        title = { Text("Booking Successful!", fontWeight = FontWeight.Bold, color = TextDark) },
                        text = { Text("Your appointment request has been sent. You can track its status in 'My Visits'.", color = TextGray) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showConfirmDialog = false
                                    onAppointmentBooked()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                            ) {
                                Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClinicTeal,
                focusedLabelColor = ClinicTeal,
                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded, 
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color.White)
        ) {
            content()
        }
    }
}
