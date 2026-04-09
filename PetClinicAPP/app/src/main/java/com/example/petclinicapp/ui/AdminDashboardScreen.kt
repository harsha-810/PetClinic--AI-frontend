package com.example.petclinicapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.AddDoctorRequest
import com.example.petclinicapp.network.Clinic
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var hospitals by remember { mutableStateOf<List<Clinic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    var showAddHospitalDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf<Clinic?>(null) }
    
    var newHospitalName by remember { mutableStateOf("") }
    var newHospitalAddress by remember { mutableStateOf("") }
    var newHospitalOpenTime by remember { mutableStateOf("09:00 AM") }
    var newHospitalCloseTime by remember { mutableStateOf("05:00 PM") }
    
    var docName by remember { mutableStateOf("") }
    var docSpec by remember { mutableStateOf("") }
    var docEmail by remember { mutableStateOf("") }
    var docPass by remember { mutableStateOf("") }
    
    var showDeleteClinicDialog by remember { mutableStateOf<Clinic?>(null) }
    var showDeleteDoctorDialog by remember { mutableStateOf<Pair<Clinic, Int>?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val calendar = java.util.Calendar.getInstance()
    
    val openTimePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val isPM = hourOfDay >= 12
            val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val amPm = if (isPM) "PM" else "AM"
            newHospitalOpenTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        false
    )

    val closeTimePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val isPM = hourOfDay >= 12
            val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val amPm = if (isPM) "PM" else "AM"
            newHospitalCloseTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        false
    )

    fun loadHospitals() {
        scope.launch {
            isLoading = true
            try {
                val res = RetrofitClient.api.getHospitals()
                if (res.isSuccessful) {
                    hospitals = res.body() ?: emptyList()
                    errorMessage = ""
                } else {
                    errorMessage = "Failed to load clinics"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadHospitals() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.Bold, color = TextDark) },
                actions = {
                    IconButton(onClick = { loadHospitals() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ClinicTeal)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OceanBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddHospitalDialog = true },
                containerColor = ClinicTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Clinic", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClinicTeal.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Clinic Management", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Oversee your veterinary network", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else if (errorMessage.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = Color.Red)
                    }
                } else if (hospitals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No clinics registered yet.", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(hospitals, key = { it.id }) { clinic ->
                            AdminClinicCard(
                                clinic = clinic,
                                onAddDoctor = { showAddDoctorDialog = clinic },
                                onDeleteClinic = { showDeleteClinicDialog = clinic },
                                onDeleteDoctor = { doctorId -> showDeleteDoctorDialog = clinic to doctorId }
                            )
                    }
                }
            }
        }
    }

    // Add Hospital Dialog
    if (showAddHospitalDialog) {
            AdminFormDialog(
                title = "Register New Clinic",
                onDismiss = { showAddHospitalDialog = false },
                onConfirm = {
                    if (newHospitalName.isNotBlank() && newHospitalAddress.isNotBlank()) {
                        scope.launch {
                            val combinedTimings = "$newHospitalOpenTime - $newHospitalCloseTime"
                            val res = RetrofitClient.api.createHospital(Clinic(
                                name = newHospitalName, 
                                address = newHospitalAddress,
                                timings = combinedTimings
                            ))
                            if (res.isSuccessful) {
                                showAddHospitalDialog = false
                                newHospitalName = ""
                                newHospitalAddress = ""
                                loadHospitals()
                            }
                        }
                    }
                }
            ) {
                Column {
                    OutlinedTextField(
                        value = newHospitalName, 
                        onValueChange = { newHospitalName = it }, 
                        label = { Text("Clinic Name") }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newHospitalAddress, 
                        onValueChange = { newHospitalAddress = it }, 
                        label = { Text("Address") }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newHospitalOpenTime, 
                            onValueChange = {}, 
                            label = { Text("Opening Time", fontSize = 12.sp) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { openTimePickerDialog.show() }) { 
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ClinicTeal) 
                                } 
                            }
                        )
                        OutlinedTextField(
                            value = newHospitalCloseTime, 
                            onValueChange = {}, 
                            label = { Text("Closing Time", fontSize = 12.sp) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { closeTimePickerDialog.show() }) { 
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ClinicTeal) 
                                } 
                            }
                        )
                    }
                }
            }
        }

        // Add Doctor Dialog
        if (showAddDoctorDialog != null) {
            val clinic = showAddDoctorDialog!!
            AdminFormDialog(
                title = "Register Specialist",
                onDismiss = { showAddDoctorDialog = null },
                onConfirm = {
                    if (docName.isNotBlank() && docSpec.isNotBlank() && docEmail.isNotBlank() && docPass.length >= 6) {
                        scope.launch {
                            val req = AddDoctorRequest(name = docName, specialization = docSpec, email = docEmail, password = docPass)
                            val res = RetrofitClient.api.addDoctor(clinic.id, req)
                            if (res.isSuccessful) {
                                showAddDoctorDialog = null
                                docName = ""; docSpec = ""; docEmail = ""; docPass = ""
                                loadHospitals()
                                Toast.makeText(context, "Doctor registered successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorBody = res.errorBody()?.string()
                                // The backend returns { "message": "..." }
                                val errorMsg = if (errorBody != null && errorBody.contains("message")) {
                                    // Simple manual extraction for Toast (could use Gson for robust parsing)
                                    errorBody.substringAfter("\"message\":\"").substringBefore("\"")
                                } else {
                                    "Registration failed: ${res.code()}"
                                }
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else if (docPass.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Column {
                    Text("Registering for: ${clinic.name}", color = ClinicTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = docSpec, onValueChange = { docSpec = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = docEmail, onValueChange = { docEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = docPass, onValueChange = { docPass = it }, label = { Text("Temporary Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            }
        }

        // --- DELETION DIALOGS ---
        if (showDeleteClinicDialog != null) {
            val clinic = showDeleteClinicDialog!!
            AlertDialog(
                onDismissRequest = { showDeleteClinicDialog = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Permanent Deletion", fontWeight = FontWeight.Bold, color = Color.Red) },
                text = { Text("This will permanently remove '${clinic.name}' and all assigned specialists. This action cannot be undone.", color = TextDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.api.deleteHospital(clinic.id)
                                    if (res.isSuccessful) {
                                        showDeleteClinicDialog = null
                                        loadHospitals()
                                        Toast.makeText(context, "Clinic deleted successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Delete Clinic", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteClinicDialog = null }) {
                        Text("Cancel", color = TextGray)
                    }
                }
            )
        }

        if (showDeleteDoctorDialog != null) {
            val (clinic, doctorId) = showDeleteDoctorDialog!!
            val docName = clinic.doctors?.find { it.id == doctorId }?.name ?: "Specialist"
            AlertDialog(
                onDismissRequest = { showDeleteDoctorDialog = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Revoke Access", fontWeight = FontWeight.Bold, color = OceanBlue) },
                text = { Text("Confirm removal of Dr. $docName from ${clinic.name} registry?", color = TextDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.api.deleteDoctor(doctorId)
                                    if (res.isSuccessful) {
                                        showDeleteDoctorDialog = null
                                        loadHospitals()
                                        Toast.makeText(context, "Specialist removed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Confirm", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDoctorDialog = null }) {
                        Text("Dismiss", color = TextGray)
                    }
                }
            )
        }
    }
}

@Composable
fun AdminClinicCard(
    clinic: Clinic, 
    onAddDoctor: () -> Unit,
    onDeleteClinic: () -> Unit,
    onDeleteDoctor: (Int) -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(clinic.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                IconButton(onClick = onDeleteClinic, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Clinic", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
            Text(clinic.address, fontSize = 14.sp, color = TextGray)
            
            if (clinic.timings.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Timings: ${clinic.timings}", fontSize = 13.sp, color = ClinicTeal, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundWhite)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Assigned Staff (${clinic.doctors?.size ?: 0})", fontWeight = FontWeight.Bold, color = ClinicTeal, fontSize = 13.sp)
            
            clinic.doctors?.forEach { doctor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• Dr. ${doctor.name} (${doctor.specialization})", fontSize = 13.sp, color = TextDark)
                    IconButton(onClick = { onDeleteDoctor(doctor.id) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Specialist", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            if (clinic.doctors.isNullOrEmpty()) {
                Text("No specialists registered.", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onAddDoctor,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Text("Register Specialist", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminFormDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Bold, color = TextDark) },
        text = { content() },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
