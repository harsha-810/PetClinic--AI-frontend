package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.PatientProfileResponse
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(onSettingsClick: () -> Unit) {
    var profileData by remember { mutableStateOf<PatientProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getPatientProfile()
            if (response.isSuccessful) {
                profileData = response.body()
                errorMessage = ""
            } else {
                errorMessage = "Unable to retrieve medical profile."
            }
        } catch (e: Exception) {
            errorMessage = "Medical network connection issue."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Profile", fontWeight = FontWeight.Bold, color = TextDark) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Surface(
                            shape = CircleShape,
                            color = ClinicTeal.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ClinicTeal, modifier = Modifier.size(20.dp))
                            }
                        }
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
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClinicTeal)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage, color = Color.Red)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Header
                    Surface(
                        shape = CircleShape,
                        color = ClinicTeal.copy(alpha = 0.1f),
                        modifier = Modifier.size(110.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ClinicTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = profileData?.name ?: "Valued Member",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Authorized Patient Account",
                        fontSize = 14.sp,
                        color = ClinicTeal,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Account Details", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(20.dp))

                            ProfileEntryRow(
                                label = "Email Access",
                                value = profileData?.email ?: "N/A",
                                icon = Icons.Default.Email
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundWhite)
                            
                            ProfileEntryRow(
                                label = "Emergency Contact",
                                value = profileData?.phone ?: "Not Provided",
                                icon = Icons.Default.Phone
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundWhite)
                            
                            ProfileEntryRow(
                                label = "Verification Status",
                                value = "Verified Clinical Profile",
                                icon = Icons.Default.Shield
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ClinicTeal.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "To update your credentials or notification preferences, please visit the settings console.",
                                fontSize = 12.sp,
                                color = TextGray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileEntryRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OceanBlue.copy(alpha = 0.05f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextGray)
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}
