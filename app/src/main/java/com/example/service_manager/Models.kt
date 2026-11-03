package com.example.service_manager

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Current User Session State synced with Firebase Auth & Firestore
var currentUserId by mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "")
var currentUserName by mutableStateOf("User")
var currentUserEmail by mutableStateOf(FirebaseAuth.getInstance().currentUser?.email ?: "")
var currentUserPhone by mutableStateOf("")

class UserProfile {
    var email: String
        get() = currentUserEmail
        set(v) { currentUserEmail = v }
    var phone: String
        get() = currentUserPhone
        set(v) { currentUserPhone = v }
}

val currentUser = UserProfile()

// Database Reactive State Lists - Streamed live from Cloud Firestore
val dummyServices = mutableStateListOf<Service>()
val dummyCatalogue = mutableStateListOf<CatalogueItem>()
val dummyProviders = mutableStateListOf<ProviderProfile>()
val dummyRatings = mutableStateListOf<ServiceRating>()
val dummySubmissions = mutableStateListOf<Submission>()
val dummyForms = mutableStateMapOf<Int, List<FormField>>()
val formPublished = mutableStateMapOf<Int, Boolean>()

data class Service(
    val id: Int,
    val title: String,
    val providerId: String = "",
    val providerName: String = "",
    val category: String,
    val description: String,
    val location: String,
    val isActive: Boolean = true,
    val imageUrl: String? = null
)

data class CatalogueItem(
    val id: Int,
    val providerId: String = "",
    val providerName: String = "",
    val serviceId: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val imageUrl: String? = null
)

data class ProviderProfile(
    val providerId: String = "",
    val name: String = "",
    val bio: String = "",
    val location: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null
)

data class ServiceRating(
    val id: Int,
    val serviceId: Int,
    val submissionId: Int,
    val requesterId: String = "",
    val clientName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val stars: Int,
    val comment: String,
    val date: String
)

enum class FieldType { TEXT, NUMBER, DATE, IMAGE }

data class FormField(
    val id: Int,
    val label: String,
    val type: FieldType,
    val required: Boolean
)

val defaultForm = listOf(
    FormField(1, "Full name", FieldType.TEXT, true),
    FormField(2, "Phone number", FieldType.NUMBER, true),
    FormField(3, "Describe what you need", FieldType.TEXT, true)
)

enum class SubmissionStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class Submission(
    val id: Int,
    val serviceId: Int,
    val requesterId: String = "",
    val requesterName: String = "",
    val providerId: String = "",
    val submittedOn: String,
    val status: SubmissionStatus,
    val answers: List<Pair<String, String>>
)

fun getProviderOverallRating(providerIdOrName: String): Pair<Float, Int> {
    val ratings = dummyRatings.filter { r: ServiceRating ->
        r.providerId == providerIdOrName || r.providerName == providerIdOrName
    }
    if (ratings.isEmpty()) return 0f to 0
    val avg = ratings.map { r: ServiceRating -> r.stars }.average().toFloat()
    return avg to ratings.size
}

fun getServiceAverageRating(serviceId: Int): Pair<Float, Int> {
    val ratings = dummyRatings.filter { r: ServiceRating -> r.serviceId == serviceId }
    if (ratings.isEmpty()) return 0f to 0
    val avg = ratings.map { r: ServiceRating -> r.stars }.average().toFloat()
    return avg to ratings.size
}

fun addServiceRating(
    serviceId: Int,
    submissionId: Int,
    requesterId: String,
    clientName: String,
    providerId: String,
    providerName: String,
    stars: Int,
    comment: String
): Boolean {
    if (stars !in 1..5) return false
    val newId = (dummyRatings.maxOfOrNull { it.id } ?: 0) + 1
    val rating = ServiceRating(
        id = newId,
        serviceId = serviceId,
        submissionId = submissionId,
        requesterId = requesterId,
        clientName = clientName,
        providerId = providerId,
        providerName = providerName,
        stars = stars,
        comment = comment,
        date = "Today"
    )
    dummyRatings.add(rating)
    FirestoreRepository.addRating(rating)
    return true
}

fun getRatingForSubmission(submissionId: Int): ServiceRating? {
    return dummyRatings.find { it.submissionId == submissionId }
}

fun getProviderProfile(providerIdOrName: String): ProviderProfile {
    return dummyProviders.find { it.providerId == providerIdOrName || it.name == providerIdOrName }
        ?: ProviderProfile(
            providerId = providerIdOrName,
            name = providerIdOrName,
            bio = "Experienced service provider.",
            location = "Malawi",
            phone = "Contact provider",
            email = "contact@example.com"
        )
}

fun updateUserProfile(email: String, phone: String): Boolean {
    if (!Validator.isEmailValid(email) || !Validator.isPhoneValid(phone)) {
        return false
    }
    currentUserEmail = email.trim()
    currentUserPhone = phone.trim()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update(mapOf("email" to email.trim(), "phone" to phone.trim()))
    }
    return true
}

fun getProviderCatalogue(providerIdOrName: String): List<CatalogueItem> {
    return dummyCatalogue.filter {
        it.providerId == providerIdOrName || it.providerName == providerIdOrName
    }
}

fun getProviderServices(providerIdOrName: String): List<Service> {
    return dummyServices.filter {
        it.providerId == providerIdOrName || it.providerName == providerIdOrName
    }
}

fun getProviderRatings(providerIdOrName: String): List<ServiceRating> {
    return dummyRatings.filter {
        it.providerId == providerIdOrName || it.providerName == providerIdOrName
    }
}

fun getServiceForm(serviceId: Int): List<FormField> {
    return dummyForms[serviceId] ?: defaultForm
}

fun isFormPublished(serviceId: Int): Boolean {
    return formPublished[serviceId] ?: true
}

fun updateServiceForm(serviceId: Int, fields: List<FormField>, published: Boolean): Boolean {
    dummyForms[serviceId] = fields
    formPublished[serviceId] = published
    FirestoreRepository.saveFormFields(serviceId, fields)
    return true
}

fun getServiceById(id: Int): Service? {
    return dummyServices.find { it.id == id }
}

fun saveService(
    id: Int,
    title: String,
    category: String,
    location: String,
    description: String,
    isActive: Boolean
): Boolean {
    if (title.isBlank() || category.isBlank() || location.isBlank() || description.isBlank()) {
        return false
    }

    val existing = dummyServices.find { it.id == id }
    val serviceToSave: Service
    if (existing != null) {
        val index = dummyServices.indexOfFirst { it.id == existing.id }
        serviceToSave = existing.copy(
            title = title.trim(),
            category = category,
            location = location.trim(),
            description = description.trim(),
            isActive = isActive
        )
        dummyServices[index] = serviceToSave
    } else {
        val newId = (dummyServices.maxOfOrNull { it.id } ?: 0) + 1
        serviceToSave = Service(
            id = newId,
            title = title.trim(),
            providerId = currentUserId,
            providerName = currentUserName,
            category = category,
            description = description.trim(),
            location = location.trim(),
            isActive = isActive
        )
        dummyServices.add(serviceToSave)
    }
    FirestoreRepository.saveService(serviceToSave)
    return true
}

fun submitRequest(
    serviceId: Int,
    answers: Map<Int, String>
): Boolean {
    val service = dummyServices.find { it.id == serviceId } ?: return false
    val fields = dummyForms[serviceId] ?: defaultForm

    fields.forEach { field ->
        val value = answers[field.id].orEmpty().trim()
        if (field.required && value.isEmpty()) return false
        if (field.type == FieldType.NUMBER && value.isNotEmpty() && value.toDoubleOrNull() == null) return false
    }

    val newId = (dummySubmissions.maxOfOrNull { it.id } ?: 0) + 1
    val submission = Submission(
        id = newId,
        serviceId = serviceId,
        requesterId = currentUserId,
        requesterName = currentUserName,
        providerId = service.providerId.ifBlank { service.providerName },
        submittedOn = "Today",
        status = SubmissionStatus.PENDING,
        answers = fields.map { field ->
            val value = answers[field.id].orEmpty().trim()
            field.label to when {
                value.isEmpty() -> "-"
                field.type == FieldType.IMAGE -> "Image attached"
                else -> value
            }
        }
    )
    dummySubmissions.add(0, submission)
    FirestoreRepository.submitRequest(submission)
    return true
}

fun getSentRequests(userIdOrName: String): List<Submission> {
    return dummySubmissions.filter {
        (userIdOrName.isNotBlank() && it.requesterId == userIdOrName) || it.requesterName == userIdOrName
    }
}

fun getReceivedRequests(userIdOrName: String): List<Submission> {
    val myServices = dummyServices.filter {
        (userIdOrName.isNotBlank() && it.providerId == userIdOrName) || it.providerName == userIdOrName
    }
    val myServiceIds = myServices.map { it.id }
    return dummySubmissions.filter {
        (userIdOrName.isNotBlank() && it.providerId == userIdOrName) ||
                it.serviceId in myServiceIds
    }
}

fun getAccountStats(userIdOrName: String): Triple<Int, Int, Int> {
    val myServices = dummyServices.filter {
        (userIdOrName.isNotBlank() && it.providerId == userIdOrName) || it.providerName == userIdOrName
    }
    val myServiceIds = myServices.map { it.id }
    val servicesCount = myServices.size

    val sentCount = dummySubmissions.count {
        (userIdOrName.isNotBlank() && it.requesterId == userIdOrName) || it.requesterName == userIdOrName
    }
    val receivedCount = dummySubmissions.count {
        (userIdOrName.isNotBlank() && it.providerId == userIdOrName) ||
                it.serviceId in myServiceIds
    }
    return Triple(servicesCount, sentCount, receivedCount)
}

fun searchServices(query: String, userIdOrName: String): List<Service> {
    return dummyServices.filter {
        it.isActive &&
                (it.providerId != userIdOrName && it.providerName != userIdOrName) &&
                (
                        it.title.contains(query, ignoreCase = true) ||
                                it.providerName.contains(query, ignoreCase = true) ||
                                it.category.contains(query, ignoreCase = true)
                        )
    }
}

fun getSubmissionById(id: Int): Submission? {
    return dummySubmissions.find { it.id == id }
}

fun updateSubmissionStatus(submissionId: Int, status: SubmissionStatus): Boolean {
    val index = dummySubmissions.indexOfFirst { it.id == submissionId }
    if (index < 0) return false
    dummySubmissions[index] = dummySubmissions[index].copy(status = status)
    FirestoreRepository.updateSubmissionStatus(submissionId, status)
    return true
}

fun addToCatalogue(
    providerName: String,
    serviceId: Int?,
    title: String,
    description: String,
    imageUrl: String
): Boolean {
    if (title.isBlank() || description.isBlank()) return false
    val maxId = dummyCatalogue.maxOfOrNull { it.id } ?: 0
    val item = CatalogueItem(
        id = maxId + 1,
        providerId = currentUserId,
        providerName = providerName,
        serviceId = serviceId,
        title = title.trim(),
        description = description.trim(),
        date = "Recent",
        imageUrl = imageUrl
    )
    dummyCatalogue.add(item)
    FirestoreRepository.addCatalogueItem(item)
    return true
}
