package com.example.petclinicapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import com.example.petclinicapp.network.Prescription
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrescriptionScreen(
    appointmentId: Int,
    onCancel: () -> Unit,
    onPrescriptionSaved: () -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var medicines by remember { mutableStateOf("") }
    var advice by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Assessment", fontWeight = FontWeight.Bold, color = TextDark) },
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
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                // Header
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
                        Text("Clinical Documentation", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Recording findings for Appointment #$appointmentId", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Examination Details", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = diagnosis, 
                            onValueChange = { diagnosis = it }, 
                            label = { Text("Diagnosis", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = medicines, 
                            onValueChange = { medicines = it }, 
                            label = { Text("Prescribed Treatment", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = advice, 
                            onValueChange = { advice = it }, 
                            label = { Text("Doctor's Advice", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            try {
                                val req = Prescription(appointmentId = appointmentId, diagnosis = diagnosis, medicines = medicines, advice = advice)
                                val res = RetrofitClient.api.addPrescription(appointmentId, req)
                                if (res.isSuccessful) {
                                    Toast.makeText(context, "Assessment Finalized!", Toast.LENGTH_SHORT).show()
                                    onPrescriptionSaved()
                                } else {
                                    errorMessage = "Failed to submit documentation. Error: ${res.code()}"
                                }
                            } catch (_: Exception) {
                                errorMessage = "Network Error. Please try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                    enabled = !isLoading && diagnosis.isNotBlank() && medicines.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Finalize & Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
