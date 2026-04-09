package com.example.petclinicapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val displayDateTimeFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Date N/A"
        return try {
            if (isoString.contains("T")) {
                val date = isoFormat.parse(isoString)
                if (date != null) displayDateTimeFormat.format(date) else isoString
            } else {
                val date = dateFormat.parse(isoString)
                if (date != null) displayDateFormat.format(date) else isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
