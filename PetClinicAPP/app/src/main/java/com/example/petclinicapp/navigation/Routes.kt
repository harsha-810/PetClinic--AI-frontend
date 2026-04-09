package com.example.petclinicapp.navigation

import kotlinx.serialization.Serializable

// No-argument routes
@Serializable data object Splash
@Serializable data object Login
@Serializable data object RegisterPatient
@Serializable data object DoctorDashboard
@Serializable data object DoctorStatistics
@Serializable data object PatientMain
@Serializable data object AdminDashboard
@Serializable data object AdminSettings
@Serializable data object AddPet
@Serializable data object DoctorProfile
@Serializable data class Settings(val isDoctor: Boolean)
@Serializable data object ChangePassword
@Serializable data class EditProfile(val isDoctor: Boolean)
@Serializable data object PrivacyPolicy
@Serializable data object TermsOfService
@Serializable data object ForgotPassword
@Serializable data class ResetPassword(val email: String)
@Serializable data object AiAnalysis

// Routes with arguments
@Serializable data class WritePrescription(
    val appointmentId: Int
)

@Serializable data class BookAppointment(
    val petId: Int,
    val symptoms: String? = null,
    val duration: String? = null,
    val priority: Int = 3
)

@Serializable data class PetHistory(
    val petId: Int,
    val isDoctor: Boolean
)

@Serializable data class FilteredAppointments(
    val petId: Int? = null,
    val onlyUpcoming: Boolean = false
)
