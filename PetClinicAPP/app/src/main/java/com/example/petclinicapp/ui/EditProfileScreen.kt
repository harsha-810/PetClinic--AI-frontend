package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(isDoctor: Boolean, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var secondaryField by remember { mutableStateOf("") } // phone for patient, specialization for doctor
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = if (isDoctor) {
                RetrofitClient.api.getDoctorProfile()
            } else {
                RetrofitClient.api.getPatientProfile()
            }

            if (response.isSuccessful) {
                if (isDoctor) {
                    val body = response.body() as? com.example.petclinicapp.network.DoctorProfileResponse
                    body?.let {
                        name = it.name
                        secondaryField = it.specialization
                    }
                } else {
                    val body = response.body() as? com.example.petclinicapp.network.PatientProfileResponse
                    body?.let {
                        name = it.name
                        secondaryField = it.phone
                    }
                }
            }
        } catch (e: Exception) {
            resultMessage = "Unable to retrieve current profile data."
        } finally {
            isFetching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("General Profile", fontWeight = FontWeight.Bold, color = TextDark) },
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
                            Icon(Icons.Default.Settings, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Account Information", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Update your personal identity details", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isFetching) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Profile Fields", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = name, 
                                onValueChange = { name = it }, 
                                label = { Text("Full Name", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ClinicTeal, 
                                    focusedLabelColor = ClinicTeal,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                                ),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = secondaryField, 
                                onValueChange = { secondaryField = it }, 
                                label = { Text(if (isDoctor) "Specialization" else "Phone Number", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ClinicTeal, 
                                    focusedLabelColor = ClinicTeal,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                                ),
                                leadingIcon = { 
                                    if (isDoctor) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp))
                                    }
                                }
                            )
                        }
                    }

                    if (resultMessage.isNotEmpty()) {
                        Text(
                            text = resultMessage, 
                            color = if (resultMessage.contains("success")) ClinicTeal else Color.Red, 
                            modifier = Modifier.padding(vertical = 16.dp), 
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && secondaryField.isNotEmpty()) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val response = if (isDoctor) {
                                            RetrofitClient.api.updateDoctorProfile(
                                                com.example.petclinicapp.network.UpdateDoctorProfileRequest(name, secondaryField)
                                            )
                                        } else {
                                            RetrofitClient.api.updatePatientProfile(
                                                com.example.petclinicapp.network.UpdateProfileRequest(name, secondaryField)
                                            )
                                        }
                                        
                                        if (response.isSuccessful) {
                                            resultMessage = "Profile updated successfully!"
                                        } else {
                                            resultMessage = "Update failed. Error: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        resultMessage = "Connection failed. Please try again."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                resultMessage = "Please ensure all fields are correctly filled."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Apply Updates", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
