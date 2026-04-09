package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Lock
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.LoginRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, onRegisterClick: () -> Unit, onForgotPasswordClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Branding
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = ClinicTeal.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = ClinicTeal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "PetClinic",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ClinicTeal
            )
            Text(
                text = "Client & Doctor Portal",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Sign in to continue",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            cursorColor = ClinicTeal
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            focusedLabelColor = ClinicTeal,
                            cursorColor = ClinicTeal
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
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
                                    val response = RetrofitClient.api.login(LoginRequest(email, password))
                                    if (response.isSuccessful && response.body() != null) {
                                        val body = response.body()!!
                                        RetrofitClient.token = body.token
                                        RetrofitClient.userName = body.name
                                        onLoginSuccess(body.role)
                                    } else {
                                        errorMessage = when (response.code()) {
                                            401 -> "Invalid email or password"
                                            500 -> "Server error. Please try again later."
                                            else -> "Error: ${response.code()}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Connection error"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onForgotPasswordClick) {
                        Text("Forgot Password?", color = ClinicTeal, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(onClick = onRegisterClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("New here? ", color = TextGray)
                            Text("Create Account", color = ClinicTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
