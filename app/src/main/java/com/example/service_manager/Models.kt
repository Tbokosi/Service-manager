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
    val isActive: Boolean = true,
    val imageUrl: String? = null
)

val dummyServices = mutableStateListOf(
    Service(1, "Custom Suit Tailoring", "Tee", "Tailoring",
        "Tailored suits, dresses, and school uniforms. Measurements taken in person.", "Lilongwe", true, "tailor.jpeg"),
    Service(2, "Wedding Photography", "Chimwemwe Phiri", "Photography",
        "Full-day coverage with edited photos delivered within two weeks.", "Blantyre", true, "photography.jpeg"),
    Service(3, "Furniture Making", "John Mwale", "Carpentry",
        "Beds, wardrobes, tables, and custom orders built to your size.", "Lilongwe", true, "carpently.jpeg"),
    Service(4, "Home Painting", "Mercy Kamanga", "Painting",
        "Interior and exterior painting with a one-year touch-up guarantee.", "Mzuzu", true, "painting.jpeg"),
    Service(5, "Event Catering", "Tee", "Catering",
        "Food for weddings, funerals, and parties of any size.", "Zomba", false, "tailor.jpeg")
)

data class CatalogueItem(
    val id: Int,
    val providerName: String,
    val serviceId: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val imageUrl: String? = null
)

val dummyCatalogue = mutableStateListOf(
    CatalogueItem(1, "Tee", 1, "Bespoke Royal Suit", "3-piece tailored wool suit crafted for a high-profile wedding.", "Aug 2026", "tailor.jpeg"),
    CatalogueItem(2, "Tee", 1, "Custom Evening Dress", "Hand-stitched evening dress with traditional chitenje accents.", "Jul 2026", "tailor.jpeg"),
    CatalogueItem(3, "Tee", 5, "Corporate Buffet Catering", "Full course meal catering for 150 guests at Lilongwe Hotel.", "Sep 2026", "tailor.jpeg"),
    CatalogueItem(4, "Chimwemwe Phiri", 2, "Lake Malawi Sunset Wedding", "Romantic outdoor wedding shoot at Sunbird Livingstonia Beach.", "Sep 2026", "photography.jpeg"),
    CatalogueItem(5, "Chimwemwe Phiri", 2, "Portrait Session", "Studio portrait session for corporate branding.", "Aug 2026", "photography.jpeg"),
    CatalogueItem(6, "John Mwale", 3, "Mahogany Dining Table", "Solid 8-seater dining set made from seasoned Malawian mahogany.", "May 2026", "carpently.jpeg"),
    CatalogueItem(7, "John Mwale", 3, "Master Closet Wardrobe", "Spacious 4-door wardrobe with sliding mirror panels.", "Jun 2026", "carpently.jpeg"),
    CatalogueItem(8, "Mercy Kamanga", 4, "Modern Villa Exterior", "Weather-resistant dual coat exterior painting in Mzuzu.", "Jul 2026", "painting.jpeg")
)

data class ProviderProfile(
    val name: String,
    val bio: String,
    val location: String,
    val phone: String,
    val email: String,
    val avatarUrl: String? = null
)

val dummyProviders = mutableStateListOf(
    ProviderProfile("Tee", "Professional fashion designer & tailor with 8+ years experience in custom suit fittings, ceremonial wear, and event catering.", "Lilongwe", "0999 000 000", "tee@example.com"),
    ProviderProfile("Chimwemwe Phiri", "Award-winning documentary and wedding photographer capturing timeless moments across Malawi.", "Blantyre", "0888 111 222", "chimwemwe@example.com"),
    ProviderProfile("John Mwale", "Master carpenter specializing in solid hardwood furniture, bespoke wardrobes, and modern dining sets.", "Lilongwe", "0995 333 444", "john@example.com"),
    ProviderProfile("Mercy Kamanga", "Expert interior & exterior painter offering premium finishes, color consulting, and wall restoration.", "Mzuzu", "0884 555 666", "mercy@example.com")
)

data class ServiceRating(
    val id: Int,
    val serviceId: Int,
    val submissionId: Int,
    val clientName: String,
    val providerName: String,
    val stars: Int,
    val comment: String,
    val date: String
)

val dummyRatings = mutableStateListOf(
    ServiceRating(1, 1, 4, "Alice Moyo", "Tee", 5, "Perfect fit for my suit! Excellent craftsmanship.", "15 Sep 2026"),
    ServiceRating(2, 1, 5, "Peter Zulu", "Tee", 4, "Great fabric selection and friendly service.", "20 Sep 2026")
)

fun getProviderOverallRating(providerName: String): Pair<Float, Int> {
    val ratings = dummyRatings.filter { r: ServiceRating -> r.providerName == providerName }
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

fun getProviderProfile(providerName: String): ProviderProfile {
    return dummyProviders.find { it.name == providerName }
        ?: ProviderProfile(
            name = providerName,
            bio = "Experienced service provider in Malawi.",
            location = "Malawi",
            phone = "Contact provider",
            email = "contact@example.com"
        )
}

fun getProviderCatalogue(providerName: String): List<CatalogueItem> {
    return dummyCatalogue.filter { it.providerName == providerName }
}

fun getProviderServices(providerName: String): List<Service> {
    return dummyServices.filter { it.providerName == providerName }
}

fun getProviderRatings(providerName: String): List<ServiceRating> {
    return dummyRatings.filter { it.providerName == providerName }
}

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
        2, 3, "Tee", "01 Oct 2026", SubmissionStatus.COMPLETED,
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
