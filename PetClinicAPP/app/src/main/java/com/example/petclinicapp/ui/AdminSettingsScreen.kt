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
fun AdminSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Preferences", fontWeight = FontWeight.Bold, color = TextDark) },
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
                            Text("System Admin", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Manage your administrative account", color = TextGray, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Profile Overview Card (Simplified for Admin)
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
                                    text = RetrofitClient.userName ?: "Administrator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "System Administrator",
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
                            title = "Security & Credentials",
                            subtitle = "Update your access password",
                            icon = Icons.Default.Lock,
                            onClick = onChangePassword
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


