package com.example.petclinicapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.ResetPasswordRequest
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(email: String, onBackClick: () -> Unit, onResetSuccess: () -> Unit) {
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", color = TextDark) },
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
                text = "Enter the 6-digit OTP sent to $email and your new password.",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("OTP (6 digits)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Pin, contentDescription = null, tint = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicTeal,
                    focusedLabelColor = ClinicTeal,
                    cursorColor = ClinicTeal
                )
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.resetPassword(ResetPasswordRequest(email, otp, newPassword))
                            if (response.isSuccessful) {
                                onResetSuccess()
                            } else {
                                errorMessage = response.errorBody()?.string() ?: "Reset failed"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Connection error"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && otp.length == 6 && newPassword.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
