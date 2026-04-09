package com.example.petclinicapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.Pet
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.network.AiCheckRequest
import com.example.petclinicapp.network.AiCheckResponse
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiAnalysisScreen(
    onBack: () -> Unit,
    onBookAppointment: (Int, String, String, Int) -> Unit
) {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    var duration by remember { mutableStateOf("< 1 week") }
    var isDurationExpanded by remember { mutableStateOf(false) }
    val durationOptions = listOf("< 24 hours", "1-3 days", "< 1 week", "More than 1 week")

    val allSymptoms = listOf(
        "Lethargy", "Coughing", "Loss of Appetite", "Vomiting", "Diarrhea",
        "Fever", "Breathing Difficulty", "Unconscious", "Seizures", "Bleeding",
        "Excessive Thirst", "Weight Loss", "Skin Irritation"
    )
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    var isLoading by remember { mutableStateOf(false) }
    var isFetchingPets by remember { mutableStateOf(true) }
    var otherSymptoms by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AiCheckResponse?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getMyPets()
            if (response.isSuccessful) {
                pets = response.body() ?: emptyList()
                if (pets.isNotEmpty()) {
                    selectedPet = pets[0]
                }
            } else {
                errorMessage = "Failed to load pets"
            }
        } catch (e: Exception) {
            errorMessage = "Network error while loading pets."
        } finally {
            isFetchingPets = false
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
            errorMessage = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Assistant", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Description
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OceanBlue.copy(alpha=0.05f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = OceanBlue,
                        modifier = Modifier.size(32.dp).padding(end = 12.dp)
                    )
                    Text(
                        "Powered by Advanced AI. Get instant, real-time medical analysis for your pet's symptoms.",
                        color = OceanBlue,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Consultation Details
            Text("Consultation Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark, modifier = Modifier.padding(bottom = 16.dp))

            // Pet Selection
            Text("Select Pet", fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
            if (isFetchingPets) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ClinicTeal)
                Spacer(modifier = Modifier.height(16.dp))
            } else if (pets.isEmpty()) {
                Text("You need to add a pet first.", color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedPet?.name ?: "Select a pet",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = ClinicTeal
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        pets.forEach { pet ->
                            DropdownMenuItem(
                                text = { Text("${pet.name} (${pet.species})") },
                                onClick = {
                                    selectedPet = pet
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Duration Selection
            Text("Duration of Symptoms", fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = isDurationExpanded,
                onExpandedChange = { isDurationExpanded = !isDurationExpanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDurationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = ClinicTeal
                    )
                )
                ExposedDropdownMenu(
                    expanded = isDurationExpanded,
                    onDismissRequest = { isDurationExpanded = false }
                ) {
                    durationOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                duration = opt
                                isDurationExpanded = false
                            }
                        )
                    }
                }
            }

            // Symptoms Selection
            Text("Observed Symptoms", fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
            Text("Select all that apply", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allSymptoms.forEach { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedSymptoms.remove(symptom)
                            else selectedSymptoms.add(symptom)
                        },
                        label = { Text(symptom) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = ClinicTeal.copy(alpha = 0.1f),
                            selectedLabelColor = ClinicTeal
                        ),
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = "Selected", modifier = Modifier.size(16.dp), tint = ClinicTeal) }
                        } else null,
                        shape = RoundedCornerShape(16.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.LightGray,
                            selectedBorderColor = ClinicTeal
                        )
                    )
                }
            }

            // Other Symptoms / Clinical Notes
            Text("Other Symptoms / Clinical Notes", fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = otherSymptoms,
                onValueChange = { if (it.length <= 500) otherSymptoms = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                placeholder = { Text("Describe any other symptoms or behavioral changes...", fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${otherSymptoms.length}/500",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        fontSize = 11.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = ClinicTeal
                )
            )

            // Generate Button
            Button(
                onClick = {
                    if (selectedPet == null) {
                        errorMessage = "Please select a pet."
                        return@Button
                    }
                    val currentSymptoms = selectedSymptoms.toMutableList()
                    if (otherSymptoms.trim().isNotEmpty()) {
                        currentSymptoms.add(otherSymptoms.trim())
                    }

                    if (currentSymptoms.isEmpty()) {
                        errorMessage = "Please select or describe at least one symptom."
                        return@Button
                    }
                    
                    isLoading = true
                    analysisResult = null
                    
                    scope.launch {
                        try {
                            val request = AiCheckRequest(
                                petId = selectedPet!!.id,
                                symptoms = currentSymptoms.toList(),
                                duration = duration
                            )
                            val response = RetrofitClient.api.checkCondition(request)
                            if (response.isSuccessful && response.body() != null) {
                                analysisResult = response.body()
                            } else {
                                errorMessage = "Failed to get AI analysis."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Network error. Please try again."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                enabled = !isLoading && !isFetchingPets && pets.isNotEmpty()
            ) {

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Troubleshoot, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate AI Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Loading Animation State (Skeleton/Scanner)
            AnimatedVisibility(visible = isLoading) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                        label = "alpha"
                    )
                    
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(ClinicTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = ClinicTeal.copy(alpha = alpha), modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing symptoms...", fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Cross-referencing veterinary databases.", color = TextGray, fontSize = 12.sp)
                }
            }

            // Result State
            AnimatedVisibility(
                visible = analysisResult != null,
                enter = fadeIn() + expandVertically()
            ) {
                val result = analysisResult ?: return@AnimatedVisibility
                
                val severityColor = when (result.severity) {
                    "High" -> Color(0xFFFF5252) // Red
                    "Medium" -> Color(0xFFFF9800) // Orange
                    else -> ClinicTeal // Green/Teal
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Severity Header
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = severityColor.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (result.severity == "High") Icons.Default.Warning 
                                        else if (result.severity == "Medium") Icons.Default.Info 
                                        else Icons.Default.CheckCircle,
                                        contentDescription = "Severity Indicator",
                                        tint = severityColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Severity Level", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("${result.severity ?: "Low"} Priority", color = severityColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Condition Block
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BackgroundWhite,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Biotech, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Likely Condition", fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(result.condition ?: "Unknown", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Recommendation Block
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ClinicTeal.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Action Plan", fontWeight = FontWeight.Bold, color = ClinicTeal)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(result.recommendation ?: "No recommendation available.", color = TextDark, fontSize = 15.sp, lineHeight = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Disclaimer
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = TextGray, modifier = Modifier.size(16.dp).padding(top=2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Disclaimer: This AI analysis is for informational purposes only and does not replace professional veterinary advice. If your pet is in severe distress, contact an emergency clinic immediately.",
                                color = TextGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Connect button
                        Button(
                            onClick = {
                                selectedPet?.let { pet ->
                                    val symptomsStr = selectedSymptoms.joinToString(",")
                                    onBookAppointment(pet.id, symptomsStr, duration, result.priorityLevel ?: 3)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book Appointment with these details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
