package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.ChangePasswordRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Credentials", fontWeight = FontWeight.Bold, color = TextDark) },
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
                            Icon(Icons.Default.Security, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Privacy & Security", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Keep your account data protected", color = TextGray, fontSize = 14.sp)
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
                        Text("Update Password", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = currentPassword, 
                            onValueChange = { currentPassword = it }, 
                            label = { Text("Current Password", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            ),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPassword, 
                            onValueChange = { newPassword = it }, 
                            label = { Text("New Password", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            ),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPassword, 
                            onValueChange = { confirmPassword = it }, 
                            label = { Text("Confirm New Password", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            ),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp)
                }
                if (successMessage.isNotEmpty()) {
                    Text(successMessage, color = ClinicTeal, modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            errorMessage = "The passwords you entered do not match."
                            return@Button
                        }
                        if (newPassword.length < 6) {
                            errorMessage = "Your new password must be at least 6 characters long."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""
                        successMessage = ""
                        
                        scope.launch {
                            try {
                                val response = RetrofitClient.api.changePassword(
                                    ChangePasswordRequest(currentPassword, newPassword)
                                )
                                if (response.isSuccessful) {
                                    successMessage = "Your password has been successfully updated!"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    errorMessage = "Update failed. Please check your current password."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Check your connection and try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                    enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Update Credentials", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
