package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.ForgotPasswordRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBackClick: () -> Unit, onOtpSent: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter your email address and we'll send you an OTP to reset your password.",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TextGray) },
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                            if (response.isSuccessful) {
                                onOtpSent(email)
                            } else {
                                errorMessage = response.errorBody()?.string() ?: "Failed to send OTP"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Connection error"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
