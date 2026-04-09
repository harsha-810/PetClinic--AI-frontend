package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDoctor: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onEditProfile: (Boolean) -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit
) {
    var patientData by remember { mutableStateOf<com.example.petclinicapp.network.PatientProfileResponse?>(null) }
    var doctorData by remember { mutableStateOf<com.example.petclinicapp.network.DoctorProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = if (isDoctor) {
                RetrofitClient.api.getDoctorProfile()
            } else {
                RetrofitClient.api.getPatientProfile()
            }
            
            if (response.isSuccessful) {
                if (isDoctor) {
                    doctorData = response.body() as? com.example.petclinicapp.network.DoctorProfileResponse
                } else {
                    patientData = response.body() as? com.example.petclinicapp.network.PatientProfileResponse
                }
            } else {
                errorMessage = "Failed to load profile"
            }
        } catch (e: Exception) {
            // Error handling could be improved with a Snackbar
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClinicTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(600, easing = FastOutSlowInEasing))
        ) {
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
                            Icon(Icons.Default.Settings, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("User Preferences", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Personalize your clinic experience", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else {
                    // Profile Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ClinicTeal.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isDoctor) "Dr. ${doctorData?.name ?: "Specialist"}" else (patientData?.name ?: "Valued Member"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = if (isDoctor) (doctorData?.specialization ?: "Medical Staff") else (patientData?.email ?: "PetClinic User"),
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    SettingsGroupTitle("ACCOUNT MANAGEMENT")
                    SettingsGroupCard {
                        SettingsEntry(
                            title = "Edit Profile",
                            subtitle = "Update your identifiable details",
                            icon = Icons.Default.Edit,
                            onClick = { onEditProfile(isDoctor) }
                        )
                        HorizontalDivider(color = BackgroundWhite, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsEntry(
                            title = "Security & Credentials",
                            subtitle = "Update your access password",
                            icon = Icons.Default.Lock,
                            onClick = onChangePassword
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingsGroupTitle("NOTIFICATIONS")
                    SettingsGroupCard {
                        SettingsToggleEntry(
                            title = "Push Alerts",
                            subtitle = "Receive real-time medical updates",
                            icon = Icons.Default.Notifications,
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingsGroupTitle("LEGAL & SUPPORT")
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    SettingsGroupCard {
                        SettingsEntry(
                            title = "Help Center",
                            subtitle = "Clinical guides and assistance",
                            icon = Icons.Default.Help,
                            onClick = { uriHandler.openUri("https://petclinic.com/help") }
                        )
                        HorizontalDivider(color = BackgroundWhite, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsEntry(
                            title = "Privacy Standards",
                            subtitle = "How we protect your data",
                            icon = Icons.Default.Security,
                            onClick = onPrivacyPolicy
                        )
                        HorizontalDivider(color = BackgroundWhite, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsEntry(
                            title = "Terms of Service",
                            subtitle = "Our operational guidelines",
                            icon = Icons.Default.Description,
                            onClick = onTermsOfService
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogout() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.Red.copy(alpha = 0.7f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
}

@Composable
fun SettingsGroupTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsEntry(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun SettingsToggleEntry(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ClinicTeal,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
