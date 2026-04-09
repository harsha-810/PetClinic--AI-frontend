package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.RegisterPatientRequest
import com.example.petclinicapp.network.RetrofitClient
import kotlinx.coroutines.launch
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPatientScreen(onBackToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Branding Header
            Surface(
                shape = CircleShape,
                color = ClinicTeal.copy(alpha = 0.1f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(36.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "PetClinic",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ClinicTeal,
                letterSpacing = 1.sp
            )
            Text(
                text = "Clinical Patient Portal",
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "New Registration",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Join our specialized medical community",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Number", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Password", fontSize = 12.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                try {
                                    val req = RegisterPatientRequest(email, password, name, phone)
                                    val res = RetrofitClient.api.registerPatient(req)
                                    if (res.isSuccessful) {
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = "Registration declined. Please verify inputs."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network coordination failed."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Complete Onboarding", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Already a member? ", color = TextGray, fontSize = 14.sp)
                        Text("Sign In", color = ClinicTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } // Column
        } // Card
    } // Column
} // Box
} // Function