package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.DoctorProfileResponse
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(onBack: () -> Unit, onSettingsClick: () -> Unit) {
    var profileData by remember { mutableStateOf<DoctorProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getDoctorProfile()
            if (response.isSuccessful) {
                profileData = response.body()
            } else {
                errorMessage = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Connection Failed"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Profile", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OceanBlue)
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
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClinicTeal.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Professional Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Manage your clinic presence", color = TextGray, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClinicTeal)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage, color = Color.Red)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Section
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = ClinicTeal.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp), 
                                tint = ClinicTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Dr. ${profileData?.name ?: "Unknown"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = profileData?.specialization ?: "Specialist",
                        fontSize = 16.sp,
                        color = OceanBlue,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Professional Details", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(20.dp))

                            ProfileInfoItem(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = profileData?.email ?: "N/A"
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = BackgroundWhite)
                            Spacer(modifier = Modifier.height(20.dp))

                            ProfileInfoItem(
                                icon = Icons.Default.Home,
                                label = "Affiliated Clinic",
                                value = profileData?.hospitalName ?: "N/A"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Settings Suggestion Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "To update security settings, visit the Settings page.",
                                fontSize = 13.sp,
                                color = TextGray
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
fun ProfileInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ClinicTeal.copy(alpha = 0.05f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = TextGray, fontSize = 12.sp)
            Text(value, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
