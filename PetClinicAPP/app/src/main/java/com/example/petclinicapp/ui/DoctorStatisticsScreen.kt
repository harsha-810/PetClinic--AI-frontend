package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorStatisticsScreen(onBack: () -> Unit) {
    var stats by remember { mutableStateOf<Map<String, Int>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getDoctorStatistics()
            if (response.isSuccessful) {
                stats = response.body()
            } else {
                errorMessage = "Failed to load statistics."
            }
        } catch (_: Exception) {
            errorMessage = "Network error."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Analytics", fontWeight = FontWeight.Bold, color = TextDark) },
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
                            Icon(Icons.Default.Info, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Operational Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Performance metrics for today", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else if (errorMessage.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = Color.Red)
                    }
                } else if (stats != null) {
                    DoctorStatsColumnCard(
                        label = "Pending Consultations",
                        value = stats!!["totalPending"] ?: 0,
                        color = OceanBlue,
                        icon = Icons.Default.List,
                        description = "Patients waiting in the triage queue"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DoctorStatsColumnCard(
                        label = "Completed Today",
                        value = stats!!["completedToday"] ?: 0,
                        color = ClinicTeal,
                        icon = Icons.Default.CheckCircle,
                        description = "Cases finalized and documented"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DoctorStatsColumnCard(
                        label = "Critical Cases",
                        value = stats!!["emergenciesCritical"] ?: 0,
                        color = Color.Red,
                        icon = Icons.Default.Warning,
                        description = "High priority emergency triage"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DoctorStatsColumnCard(label: String, value: Int, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(text = value.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(text = description, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}
