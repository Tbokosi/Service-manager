package com.example.service_manager

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Service(
    val id: Int,
    val title: String,
    val providerName: String,
    val category: String,
    val description: String,
    val location: String,
    val isActive: Boolean = true
)

val dummyServices = mutableStateListOf(
    Service(1, "Custom Suit Tailoring", "Tee", "Tailoring",
        "Tailored suits, dresses, and school uniforms. Measurements taken in person.", "Lilongwe"),
    Service(2, "Wedding Photography", "Chimwemwe Phiri", "Photography",
        "Full-day coverage with edited photos delivered within two weeks.", "Blantyre"),
    Service(3, "Furniture Making", "John Mwale", "Carpentry",
        "Beds, wardrobes, tables, and custom orders built to your size.", "Lilongwe"),
    Service(4, "Home Painting", "Mercy Kamanga", "Painting",
        "Interior and exterior painting with a one-year touch-up guarantee.", "Mzuzu"),
    Service(5, "Event Catering", "Tee", "Catering",
        "Food for weddings, funerals, and parties of any size.", "Zomba", false)
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

val dummyForms = mutableStateMapOf(

    1 to listOf( // Tailoring
        FormField(1, "Full name", FieldType.TEXT, true),
        FormField(2, "Chest (cm)", FieldType.NUMBER, true),
        FormField(3, "Waist (cm)", FieldType.NUMBER, true),
        FormField(4, "Needed by", FieldType.DATE, true),
        FormField(5, "Style reference photo", FieldType.IMAGE, false),
        FormField(6, "Extra notes", FieldType.TEXT, false)
    ),
    2 to listOf( // Photography
        FormField(1, "Your name", FieldType.TEXT, true),
        FormField(2, "Event date", FieldType.DATE, true),
        FormField(3, "Number of guests", FieldType.NUMBER, true),
        FormField(4, "Venue", FieldType.TEXT, true),
        FormField(5, "Inspiration photo", FieldType.IMAGE, false)
    ),
    3 to listOf( // Carpentry
        FormField(1, "Your name", FieldType.TEXT, true),
        FormField(2, "Item to build", FieldType.TEXT, true),
        FormField(3, "Width (cm)", FieldType.NUMBER, true),
        FormField(4, "Height (cm)", FieldType.NUMBER, true),
        FormField(5, "Sketch or reference photo", FieldType.IMAGE, true)
    )
)
val formPublished = mutableStateMapOf<Int, Boolean>()
const val currentUserName = "Tee"
class UserProfile {
    var email by mutableStateOf("tee@example.com")
    var phone by mutableStateOf("0999 000 000")
}

val currentUser = UserProfile()

enum class SubmissionStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class Submission(
    val id: Int,
    val serviceId: Int,
    val requesterName: String,
    val submittedOn: String,
    val status: SubmissionStatus,
    val answers: List<Pair<String, String>>
)

val dummySubmissions = mutableStateListOf(
    Submission(
        1, 4, "Tee", "28 Sep 2026", SubmissionStatus.COMPLETED,
        listOf("Full name" to "Tee", "Phone number" to "0999 123 456", "Describe what you need" to "Repaint two bedrooms")
    ),
    Submission(
        2, 3, "Tee", "01 Oct 2026", SubmissionStatus.IN_PROGRESS,
        listOf("Your name" to "Tee", "Item to build" to "Wardrobe", "Width (cm)" to "120", "Height (cm)" to "200")
    ),
    Submission(
        3, 2, "Tee", "03 Oct 2026", SubmissionStatus.PENDING,
        listOf("Your name" to "Tee", "Event date" to "20 Dec 2026", "Number of guests" to "150", "Venue" to "Kamuzu Palace Hall")
    ),
    Submission(
        4, 1, "Alice Moyo", "30 Sep 2026", SubmissionStatus.PENDING,
        listOf("Full name" to "Alice Moyo", "Chest (cm)" to "90", "Waist (cm)" to "76", "Needed by" to "15 Oct 2026")
    ),
    Submission(
        5, 1, "Peter Zulu", "02 Oct 2026", SubmissionStatus.IN_PROGRESS,
        listOf("Full name" to "Peter Zulu", "Chest (cm)" to "104", "Waist (cm)" to "92", "Needed by" to "25 Oct 2026")
    )
)