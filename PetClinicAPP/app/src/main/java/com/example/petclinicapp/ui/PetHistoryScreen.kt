package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.Pet
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.utils.DateUtils
import com.example.petclinicapp.ui.components.LogVaccinationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHistoryScreen(
    petId: Int,
    isDoctor: Boolean,
    onBack: () -> Unit
) {
    var pet by remember { mutableStateOf<Pet?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    var showVaccinationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = if (isDoctor) {
                RetrofitClient.api.getDoctorPetHistory(petId)
            } else {
                RetrofitClient.api.getPatientPetHistory(petId)
            }
            
            if (response.isSuccessful) {
                pet = response.body()
            } else {
                errorMessage = "Failed to load medical history."
            }
        } catch (_: Exception) {
            errorMessage = "Network Error"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical History", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClinicTeal)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color.Red)
                }
            } else if (pet != null) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ClinicTeal.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = ClinicTeal)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("${pet?.name ?: "Pet"}'s Records", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Historical health and treatment data", color = TextGray, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Vaccinations Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader("Vaccination Records", ClinicTeal)
                            if (isDoctor) {
                                TextButton(onClick = { showVaccinationDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Record", color = ClinicTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    
                    if (pet!!.vaccinations.isNullOrEmpty()) {
                        item {
                            Text("No vaccinations recorded yet.", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    } else {
                        items(pet!!.vaccinations!!) { vax ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(vax.vaccineName ?: "Vaccine", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Status: Administered", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Date Given", color = TextGray, fontSize = 12.sp)
                                            Text(DateUtils.formatDateTime(vax.lastDate), color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Next Due", color = TextGray, fontSize = 12.sp)
                                            Text(DateUtils.formatDateTime(vax.nextDueDate), color = OceanBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Medical Visits Section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader("Clinical Visits", OceanBlue)
                    }

                    if (pet!!.appointments.isNullOrEmpty()) {
                        item {
                            Text("No clinical visits recorded yet.", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    } else {
                        items(pet!!.appointments!!.filter { it.status == "Completed" || it.status == "Accepted" }) { appointment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(DateUtils.formatDateTime(appointment.date), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                        Surface(
                                            color = if (appointment.status == "Completed") ClinicTeal.copy(alpha = 0.1f) else OceanBlue.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = appointment.status ?: "Visit",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = if (appointment.status == "Completed") ClinicTeal else OceanBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text("Provider: ${appointment.doctor?.name ?: "N/A"}", color = TextGray, fontSize = 13.sp)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BackgroundWhite)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text("Reason for Visit:", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 13.sp)
                                    Text(appointment.symptoms ?: "N/A", color = TextGray, fontSize = 14.sp)
                                    
                                    appointment.prescription?.let { rx ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = ClinicTeal.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Clinical Assessment", fontWeight = FontWeight.Bold, color = ClinicTeal, fontSize = 14.sp)
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                
                                                Text("Diagnosis:", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 13.sp)
                                                Text(rx.diagnosis ?: "N/A", color = TextGray, fontSize = 13.sp)
                                                
                                                Spacer(modifier = Modifier.height(12.dp))
                                                
                                                Text("Prescription:", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 13.sp)
                                                Text(rx.medicines ?: "N/A", color = TextGray, fontSize = 13.sp)
                                                
                                                if (!rx.advice.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text("Doctor's Advice:", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 13.sp)
                                                    Text(rx.advice, color = TextGray, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (showVaccinationDialog) {
                    LogVaccinationDialog(
                        petName = pet?.name ?: "Pet",
                        onDismiss = { showVaccinationDialog = false },
                        onConfirm = { name, last, next ->
                            scope.launch {
                                try {
                                    val request = com.example.petclinicapp.network.Vaccination(
                                        petId = petId,
                                        vaccineName = name,
                                        lastDate = last,
                                        nextDueDate = next
                                    )
                                    val response = RetrofitClient.api.doctorAddVaccination(request)
                                    if (response.isSuccessful) {
                                        showVaccinationDialog = false
                                        // Refresh
                                        isLoading = true
                                        val refreshResponse = if (isDoctor) RetrofitClient.api.getDoctorPetHistory(petId) else RetrofitClient.api.getPatientPetHistory(petId)
                                        if (refreshResponse.isSuccessful) pet = refreshResponse.body()
                                        isLoading = false
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}
