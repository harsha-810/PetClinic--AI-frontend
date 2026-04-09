package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.network.Pet
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.utils.DateUtils
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordsScreen() {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var petHistory by remember { mutableStateOf<Pet?>(null) }
    var petDropdownExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isHistoryLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    fun loadPetHistory(petId: Int) {
        scope.launch {
            isHistoryLoading = true
            try {
                val response = RetrofitClient.api.getPatientPetHistory(petId)
                if (response.isSuccessful) {
                    petHistory = response.body()
                    errorMessage = ""
                }
            } catch (_: Exception) {
                errorMessage = "Unable to retrieve clinical history."
            } finally {
                isHistoryLoading = false
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            isLoading = true
            try {
                val response = RetrofitClient.api.getMyPets()
                if (response.isSuccessful) {
                    pets = response.body() ?: emptyList()
                    if (selectedPet == null && pets.isNotEmpty()) {
                        selectedPet = pets[0]
                    }
                    selectedPet?.let { loadPetHistory(it.id) }
                } else {
                    errorMessage = "Unable to load patient list."
                }
            } catch (_: Exception) {
                errorMessage = "Network connection issue."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical History", fontWeight = FontWeight.Bold, color = TextDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    .padding(horizontal = 24.dp)
            ) {
                // Gradient Hero Header (Home Page Style)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(ClinicTeal, OceanBlue)))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Medical Archive",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Clinical History",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Access full care history and assessments",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClinicTeal)
                    }
                } else if (pets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No registered patients found.", color = TextGray)
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = petDropdownExpanded,
                        onExpandedChange = { petDropdownExpanded = !petDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPet?.name ?: "Select Patient",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Medical Record For", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(petDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.2f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = petDropdownExpanded,
                            onDismissRequest = { petDropdownExpanded = false },
                            modifier = Modifier.background(Color.White).padding(8.dp)
                        ) {
                            pets.forEach { pet ->
                                DropdownMenuItem(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = ClinicTeal.copy(alpha = 0.1f),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Pets, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(pet.name ?: "Unknown Pet", color = TextDark, fontWeight = FontWeight.Medium)
                                        }
                                    },
                                    onClick = { 
                                        selectedPet = pet
                                        petDropdownExpanded = false
                                        loadPetHistory(pet.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isHistoryLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ClinicTeal)
                        }
                    } else if (petHistory != null) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                RecordSectionHeader("Vaccination Records", ClinicTeal)
                            }
                            
                            if (petHistory!!.vaccinations.isNullOrEmpty()) {
                                item { 
                                    Text(
                                        "No vaccinations recorded.", 
                                        color = TextGray, 
                                        fontSize = 14.sp, 
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) 
                                }
                            } else {
                                items(petHistory!!.vaccinations!!, key = { it.id }) { vac ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = ClinicTeal.copy(alpha = 0.1f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Verified, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(vac.vaccineName ?: "Vaccine", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextDark)
                                            }
                                            
                                            Spacer(modifier = Modifier.height(20.dp))
                                            HorizontalDivider(color = BackgroundWhite, thickness = 1.dp)
                                            Spacer(modifier = Modifier.height(20.dp))
                                            
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                RecordGridItem(
                                                    label = "LAST ADMINISTERED",
                                                    value = DateUtils.formatDateTime(vac.lastDate),
                                                    modifier = Modifier.weight(1f),
                                                    labelColor = TextGray
                                                )
                                                RecordGridItem(
                                                    label = "RE-VACCINATION DUE",
                                                    value = DateUtils.formatDateTime(vac.nextDueDate),
                                                    modifier = Modifier.weight(1f),
                                                    labelColor = OceanBlue,
                                                    valueColor = OceanBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                RecordSectionHeader("Historical Clinical Visits", OceanBlue)
                            }
                            
                            val finishedAppointments = petHistory!!.appointments?.filter { it.status == "Completed" || it.status == "Accepted" }
                            if (finishedAppointments.isNullOrEmpty()) {
                                item { 
                                    Text(
                                        "No past clinical records.", 
                                        color = TextGray, 
                                        fontSize = 14.sp, 
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) 
                                }
                            } else {
                                items(finishedAppointments, key = { it.id }) { appointment ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = DateUtils.formatDateTime(appointment.date), 
                                                    fontWeight = FontWeight.ExtraBold, 
                                                    fontSize = 18.sp, 
                                                    color = TextDark
                                                )
                                                Surface(
                                                    color = ClinicTeal.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(
                                                        text = appointment.status ?: "Finalized",
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        color = ClinicTeal,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Medical Staff: Dr. ${appointment.doctor?.name ?: "N/A"}", color = TextGray.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            
                                            appointment.prescription?.let { rx ->
                                                Spacer(modifier = Modifier.height(24.dp))
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = BackgroundWhite.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(20.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, BackgroundWhite)
                                                ) {
                                                    Column(modifier = Modifier.padding(20.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Analytics, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text("VISIT EVALUATION", fontWeight = FontWeight.ExtraBold, color = ClinicTeal, fontSize = 12.sp, letterSpacing = 1.2.sp)
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.height(20.dp))
                                                        
                                                        MedicalRecordField(
                                                            label = "Clinical Diagnosis",
                                                            value = rx.diagnosis ?: "N/A",
                                                            labelColor = OceanBlue
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(20.dp))
                                                        
                                                        MedicalRecordField(
                                                            label = "Treatment Protocol",
                                                            value = rx.medicines ?: "N/A",
                                                            labelColor = Color(0xFF673AB7)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordGridItem(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    labelColor: Color = TextGray,
    valueColor: Color = TextDark
) {
    Column(modifier = modifier) {
        Text(
            text = label, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = labelColor.copy(alpha = 0.7f),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            color = valueColor
        )
    }
}

@Composable
private fun MedicalRecordField(
    label: String, 
    value: String,
    labelColor: Color
) {
    Column {
        Text(
            text = label.uppercase(), 
            fontWeight = FontWeight.Bold, 
            color = labelColor, 
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            color = TextDark, 
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun RecordSectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            letterSpacing = 0.5.sp
        )
    }
}
