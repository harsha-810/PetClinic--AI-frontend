package com.example.petclinicapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.petclinicapp.network.Pet
import com.example.petclinicapp.network.RetrofitClient
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import com.example.petclinicapp.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(onCancel: () -> Unit, onPetAdded: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Patient", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
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
                            Icon(Icons.Default.Add, contentDescription = null, tint = ClinicTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("New Patient Registration", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Add a new family member to our clinic", color = TextGray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Pet Information", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = name, 
                            onValueChange = { name = it }, 
                            label = { Text("Pet Name", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            ),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = species, 
                            onValueChange = { species = it }, 
                            label = { Text("Species (e.g. Dog, Cat)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = breed, 
                            onValueChange = { breed = it }, 
                            label = { Text("Breed", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = age, 
                            onValueChange = { age = it }, 
                            label = { Text("Age (years)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicTeal, 
                                focusedLabelColor = ClinicTeal,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            ),
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(20.dp)) }
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val ageInt = age.toIntOrNull()
                        if (ageInt == null) {
                            errorMessage = "Please enter a valid age."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            try {
                                val req = Pet(name = name, species = species, breed = breed, age = ageInt)
                                val res = RetrofitClient.api.addPet(req)
                                if (res.isSuccessful) {
                                    Toast.makeText(context, "Pet successfully registered!", Toast.LENGTH_SHORT).show()
                                    onPetAdded()
                                } else {
                                    errorMessage = "Registration failed. Please check details."
                                }
                            } catch (_: Exception) {
                                errorMessage = "Check your connection and try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                    enabled = !isLoading && name.isNotBlank() && species.isNotBlank() && age.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
