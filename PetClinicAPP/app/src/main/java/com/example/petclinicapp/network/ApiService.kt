package com.example.petclinicapp.network

import retrofit2.Response
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String, 
    @SerializedName("password") val password: String
)
data class LoginResponse(
    @SerializedName("token") val token: String, 
    @SerializedName("role") val role: String, 
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String
)

data class AvailableDoctor(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("hospitalName") val hospitalName: String? = null
)

data class PatientProfileResponse(
    val name: String,
    val phone: String,
    val email: String,
    val petCount: Int
)

data class DoctorProfileResponse(
    val name: String,
    val specialization: String,
    val email: String,
    val hospitalName: String,
    val isActive: Boolean
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

interface ApiService {
    
    @retrofit2.http.POST("auth/login")
    suspend fun login(@retrofit2.http.Body request: LoginRequest): Response<LoginResponse>

    @retrofit2.http.POST("auth/register/patient")
    suspend fun registerPatient(@retrofit2.http.Body request: RegisterPatientRequest): Response<Map<String, String>>

    @retrofit2.http.POST("auth/change-password")
    suspend fun changePassword(@retrofit2.http.Body request: ChangePasswordRequest): Response<Map<String, String>>

    @retrofit2.http.POST("auth/forgot-password")
    suspend fun forgotPassword(@retrofit2.http.Body request: ForgotPasswordRequest): Response<Map<String, String>>

    @retrofit2.http.POST("auth/reset-password")
    suspend fun resetPassword(@retrofit2.http.Body request: ResetPasswordRequest): Response<Map<String, String>>

    @retrofit2.http.PUT("Patient/profile")
    suspend fun updatePatientProfile(@retrofit2.http.Body request: UpdateProfileRequest): Response<Map<String, String>>

    // Patient Endpoints
    @retrofit2.http.GET("Patient/my-pets")
    suspend fun getMyPets(): Response<List<Pet>>

    @retrofit2.http.GET("clinics/doctors")
    suspend fun getAvailableDoctors(): Response<List<AvailableDoctor>>

    @retrofit2.http.GET("clinics/all")
    suspend fun getAllClinics(): Response<List<String>>

    @retrofit2.http.POST("Patient/add-pet")
    suspend fun addPet(@retrofit2.http.Body pet: Pet): Response<Pet>

    @retrofit2.http.POST("Patient/vaccination")
    suspend fun addVaccination(@retrofit2.http.Body request: Vaccination): Response<Map<String, String>>

    @retrofit2.http.POST("Doctor/vaccination")
    suspend fun doctorAddVaccination(@retrofit2.http.Body request: Vaccination): Response<Map<String, String>>

    @retrofit2.http.POST("appointment/book")
    suspend fun bookAppointment(@retrofit2.http.Body request: BookAppointmentRequest): Response<Map<String, Any>>

    @retrofit2.http.GET("Patient/my-appointments")
    suspend fun getMyAppointments(): Response<List<Appointment>>

    // Doctor Endpoints
    @retrofit2.http.GET("appointment/queue")
    suspend fun getDoctorQueue(): Response<List<Appointment>>

    @retrofit2.http.GET("Patient/profile")
    suspend fun getPatientProfile(): Response<PatientProfileResponse>

    @retrofit2.http.GET("Doctor/profile")
    suspend fun getDoctorProfile(): Response<DoctorProfileResponse>

    @retrofit2.http.PUT("Doctor/profile")
    suspend fun updateDoctorProfile(@retrofit2.http.Body request: UpdateDoctorProfileRequest): Response<Map<String, String>>

    @retrofit2.http.PUT("appointment/{appointmentId}/status")
    suspend fun updateAppointmentStatus(
        @retrofit2.http.Path("appointmentId") appointmentId: Int,
        @retrofit2.http.Body request: UpdateStatusRequest
    ): Response<Map<String, String>>

    @retrofit2.http.POST("appointment/{appointmentId}/prescribe")
    suspend fun addPrescription(
        @retrofit2.http.Path("appointmentId") appointmentId: Int,
        @retrofit2.http.Body request: Prescription
    ): Response<Map<String, String>>

    @retrofit2.http.GET("doctor/statistics")
    suspend fun getDoctorStatistics(): Response<Map<String, Int>>

    @retrofit2.http.GET("doctor/pet/{petId}/history")
    suspend fun getDoctorPetHistory(@retrofit2.http.Path("petId") petId: Int): Response<Pet>

    @retrofit2.http.GET("Patient/pet/{petId}/history")
    suspend fun getPatientPetHistory(@retrofit2.http.Path("petId") petId: Int): Response<Pet>

    @retrofit2.http.POST("ai/check")
    suspend fun checkCondition(@retrofit2.http.Body request: AiCheckRequest): Response<AiCheckResponse>

    // Notifications
    @retrofit2.http.GET("notification")
    suspend fun getNotifications(): Response<List<Notification>>

    @retrofit2.http.PUT("notification/{id}/read")
    suspend fun markNotificationAsRead(@retrofit2.http.Path("id") id: Int): Response<Map<String, String>>

    @retrofit2.http.DELETE("notification/delete/{id}")
    suspend fun deleteNotification(@retrofit2.http.Path("id") id: Int): Response<Map<String, String>>

    @retrofit2.http.DELETE("notification/clear")
    suspend fun clearAllNotifications(): Response<Map<String, String>>

    @retrofit2.http.DELETE("appointment/{appointmentId}")
    suspend fun cancelAppointment(@retrofit2.http.Path("appointmentId") appointmentId: Int): Response<Map<String, String>>

    // Admin Endpoints
    @retrofit2.http.GET("hospital")
    suspend fun getHospitals(): Response<List<Clinic>>

    @retrofit2.http.POST("hospital")
    suspend fun createHospital(@retrofit2.http.Body hospital: Clinic): Response<Clinic>

    @retrofit2.http.POST("hospital/{hospitalId}/doctors")
    suspend fun addDoctor(
        @retrofit2.http.Path("hospitalId") hospitalId: Int,
        @retrofit2.http.Body request: AddDoctorRequest
    ): Response<Map<String, String>>

    @retrofit2.http.DELETE("hospital/{hospitalId}")
    suspend fun deleteHospital(@retrofit2.http.Path("hospitalId") hospitalId: Int): Response<Map<String, String>>

    @retrofit2.http.DELETE("hospital/doctors/{doctorId}")
    suspend fun deleteDoctor(@retrofit2.http.Path("doctorId") doctorId: Int): Response<Map<String, String>>
}
