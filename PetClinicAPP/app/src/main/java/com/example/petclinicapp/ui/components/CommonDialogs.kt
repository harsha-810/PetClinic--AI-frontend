package com.example.petclinicapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.TextDark
import com.example.petclinicapp.ui.theme.TextGray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LogVaccinationDialog(
    petName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var vaccineName by remember { mutableStateOf("") }
    var lastDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var nextDueDate by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()

    // Date Administered Picker
    val lastDatePicker = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            lastDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
    lastDatePicker.datePicker.maxDate = calendar.timeInMillis

    // Next Due Date Picker
    val nextDatePicker = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            nextDueDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
    nextDatePicker.datePicker.minDate = calendar.timeInMillis

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text("Log Vaccination", fontWeight = FontWeight.Bold, color = TextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Adding record for $petName", color = TextGray, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name") },
                    placeholder = { Text("e.g. Rabies") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Administered
                OutlinedTextField(
                    value = lastDate,
                    onValueChange = {},
                    label = { Text("Date Administered") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { lastDatePicker.show() },
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        IconButton(onClick = { lastDatePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = ClinicTeal)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextDark,
                        disabledBorderColor = TextGray.copy(alpha = 0.5f),
                        disabledLabelColor = TextGray
                    )
                )

                // Next Due Date
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = {},
                    label = { Text("Next Due Date (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { nextDatePicker.show() },
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        IconButton(onClick = { nextDatePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = ClinicTeal)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextDark,
                        disabledBorderColor = TextGray.copy(alpha = 0.5f),
                        disabledLabelColor = TextGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(vaccineName, lastDate, nextDueDate) },
                enabled = vaccineName.isNotBlank() && lastDate.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
