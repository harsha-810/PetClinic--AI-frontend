package com.example.petclinicapp.network

import com.google.gson.annotations.SerializedName

data class RegisterPatientRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class RegisterResponse(
    @SerializedName("message") val message: String
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class UpdateDoctorProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("specialization") val specialization: String
)

data class Patient(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)

data class Pet(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("species") val species: String? = null,
    @SerializedName("breed") val breed: String? = null,
    @SerializedName("age") val age: Int? = null,
    @SerializedName("patientId") val patientId: Int = 0,
    @SerializedName("patient") val patient: Patient? = null,
    @SerializedName("appointments") val appointments: List<Appointment>? = null,
    @SerializedName("vaccinations") val vaccinations: List<Vaccination>? = null
)

data class Vaccination(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("petId") val petId: Int = 0,
    @SerializedName("vaccineName") val vaccineName: String? = null,
    @SerializedName("lastDate") val lastDate: String? = null,
    @SerializedName("nextDueDate") val nextDueDate: String? = null
)

data class Appointment(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("petId") val petId: Int = 0,
    @SerializedName("doctorId") val doctorId: Int = 0,
    @SerializedName("date") val date: String? = null,
    @SerializedName("symptoms") val symptoms: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("aiCondition") val aiCondition: String? = null,
    @SerializedName("aiSeverity") val aiSeverity: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: Int = 3,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("pet") val pet: Pet? = null,
    @SerializedName("doctor") val doctor: AvailableDoctor? = null,
    @SerializedName("prescription") val prescription: Prescription? = null
)

data class BookAppointmentRequest(
    @SerializedName("petId") val petId: Int,
    @SerializedName("doctorId") val doctorId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("symptoms") val symptoms: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("aiCondition") val aiCondition: String,
    @SerializedName("aiSeverity") val aiSeverity: String,
    @SerializedName("priorityLevel") val priorityLevel: Int
)

data class UpdateStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("rejectionReason") val rejectionReason: String? = null
)

data class Prescription(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("appointmentId") val appointmentId: Int = 0,
    @SerializedName("diagnosis") val diagnosis: String? = null,
    @SerializedName("medicines") val medicines: String? = null,
    @SerializedName("advice") val advice: String? = null,
    @SerializedName("date") val date: String? = null
)

data class AiCheckRequest(
    @SerializedName("petId") val petId: Int,
    @SerializedName("symptoms") val symptoms: List<String>,
    @SerializedName("duration") val duration: String
)

data class AiCheckResponse(
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("recommendation") val recommendation: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("priorityLevel") val priorityLevel: Int? = 3
)

data class Notification(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("message") val message: String,
    @SerializedName("date") val date: String,
    @SerializedName("isRead") val isRead: Boolean
)

data class Clinic(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("timings") val timings: String = "9:00 AM - 5:00 PM",
    @SerializedName("doctors") val doctors: List<AvailableDoctor>? = null
)

data class AddDoctorRequest(
    @SerializedName("name") val name: String,
    @SerializedName("specialization") val specialization: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
