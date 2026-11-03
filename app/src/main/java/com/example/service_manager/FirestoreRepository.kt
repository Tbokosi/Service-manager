package com.example.service_manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions

object FirestoreRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun loadCurrentUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            currentUserId = user.uid
            currentUserEmail = user.email ?: ""

            db.collection("users").document(user.uid).get().addOnSuccessListener { doc: DocumentSnapshot ->
                if (!doc.exists()) {
                    val userMap = mapOf(
                        "uid" to user.uid,
                        "fullName" to (if (!user.displayName.isNullOrBlank()) user.displayName!! else "User"),
                        "email" to (user.email ?: ""),
                        "phone" to ""
                    )
                    db.collection("users").document(user.uid).set(userMap)
                }
            }

            db.collection("users").document(user.uid).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val fullName = doc.getString("fullName")
                    if (!fullName.isNullOrBlank()) {
                        currentUserName = fullName
                    } else if (!user.displayName.isNullOrBlank()) {
                        currentUserName = user.displayName!!
                    }
                    val phone = doc.getString("phone")
                    if (!phone.isNullOrBlank()) {
                        currentUserPhone = phone
                    }
                    val email = doc.getString("email")
                    if (!email.isNullOrBlank()) {
                        currentUserEmail = email
                    }
                } else if (!user.displayName.isNullOrBlank()) {
                    currentUserName = user.displayName!!
                }
            }
        }
    }

    fun initRealtimeListeners() {
        // Seed initial data into Firestore if database is currently empty
        seedDatabaseIfEmpty()
        loadCurrentUserProfile()

        // 1. Listen to Services
        db.collection("services").addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val newServices = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val id = (doc.getLong("id") ?: 0L).toInt()
                val title = doc.getString("title") ?: ""
                val providerId = doc.getString("providerId") ?: ""
                val providerName = doc.getString("providerName") ?: ""
                val category = doc.getString("category") ?: ""
                val description = doc.getString("description") ?: ""
                val location = doc.getString("location") ?: ""
                val isActive = doc.getBoolean("isActive") ?: true
                val imageUrl = doc.getString("imageUrl")
                if (id > 0) Service(id = id, title = title, providerId = providerId, providerName = providerName, category = category, description = description, location = location, isActive = isActive, imageUrl = imageUrl)
                else null
            }
            dummyServices.clear()
            dummyServices.addAll(newServices)
        }

        // 2. Listen to Submissions
        db.collection("submissions").addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val newSubmissions = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val id = (doc.getLong("id") ?: 0L).toInt()
                val serviceId = (doc.getLong("serviceId") ?: 0L).toInt()
                val requesterId = doc.getString("requesterId") ?: ""
                val requesterName = doc.getString("requesterName") ?: ""
                val providerId = doc.getString("providerId") ?: ""
                val submittedOn = doc.getString("submittedOn") ?: ""
                val statusStr = doc.getString("status") ?: "PENDING"
                val status = runCatching { SubmissionStatus.valueOf(statusStr) }.getOrDefault(SubmissionStatus.PENDING)
                @Suppress("UNCHECKED_CAST")
                val rawAnswers = doc.get("answers") as? List<Map<String, String>> ?: emptyList()
                val answers = rawAnswers.map { map: Map<String, String> -> (map["label"] ?: "") to (map["value"] ?: "") }
                if (id > 0) Submission(id = id, serviceId = serviceId, requesterId = requesterId, requesterName = requesterName, providerId = providerId, submittedOn = submittedOn, status = status, answers = answers)
                else null
            }
            dummySubmissions.clear()
            dummySubmissions.addAll(newSubmissions)
        }

        // 3. Listen to Catalogue
        db.collection("catalogue").addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val newCatalogue = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val id = (doc.getLong("id") ?: 0L).toInt()
                val providerId = doc.getString("providerId") ?: ""
                val providerName = doc.getString("providerName") ?: ""
                val serviceId = doc.getLong("serviceId")?.toInt()
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                val date = doc.getString("date") ?: ""
                val imageUrl = doc.getString("imageUrl")
                if (id > 0) CatalogueItem(id = id, providerId = providerId, providerName = providerName, serviceId = serviceId, title = title, description = description, date = date, imageUrl = imageUrl)
                else null
            }
            dummyCatalogue.clear()
            dummyCatalogue.addAll(newCatalogue)
        }

        // 4. Listen to Ratings
        db.collection("ratings").addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val newRatings = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val id = (doc.getLong("id") ?: 0L).toInt()
                val serviceId = (doc.getLong("serviceId") ?: 0L).toInt()
                val submissionId = (doc.getLong("submissionId") ?: 0L).toInt()
                val requesterId = doc.getString("requesterId") ?: ""
                val clientName = doc.getString("clientName") ?: ""
                val providerId = doc.getString("providerId") ?: ""
                val providerName = doc.getString("providerName") ?: ""
                val stars = (doc.getLong("stars") ?: 5L).toInt()
                val comment = doc.getString("comment") ?: ""
                val date = doc.getString("date") ?: ""
                if (id > 0) ServiceRating(id = id, serviceId = serviceId, submissionId = submissionId, requesterId = requesterId, clientName = clientName, providerId = providerId, providerName = providerName, stars = stars, comment = comment, date = date)
                else null
            }
            dummyRatings.clear()
            dummyRatings.addAll(newRatings)
        }

        // 5. Listen to Providers
        db.collection("providers").addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val newProviders = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val providerId = doc.getString("providerId") ?: doc.id
                val name = doc.getString("name") ?: ""
                val bio = doc.getString("bio") ?: ""
                val location = doc.getString("location") ?: ""
                val phone = doc.getString("phone") ?: ""
                val email = doc.getString("email") ?: ""
                val avatarUrl = doc.getString("avatarUrl")
                if (name.isNotBlank()) ProviderProfile(providerId = providerId, name = name, bio = bio, location = location, phone = phone, email = email, avatarUrl = avatarUrl)
                else null
            }
            dummyProviders.clear()
            dummyProviders.addAll(newProviders)
        }

        // Seed initial data into Firestore if database is currently empty
        seedDatabaseIfEmpty()
    }

    private fun seedDatabaseIfEmpty() {
        db.collection("services").get().addOnSuccessListener { snapshot: QuerySnapshot ->
            if (snapshot.isEmpty) {
                val seedServices = listOf(
                    Service(id = 1, title = "Custom Suit Tailoring", providerId = "tee_uid", providerName = "Tee", category = "Tailoring", description = "Tailored suits, dresses, and school uniforms. Measurements taken in person.", location = "Lilongwe", isActive = true, imageUrl = "tailor.jpeg"),
                    Service(id = 2, title = "Wedding Photography", providerId = "chimwemwe_uid", providerName = "Chimwemwe Phiri", category = "Photography", description = "Full-day coverage with edited photos delivered within two weeks.", location = "Blantyre", isActive = true, imageUrl = "photography.jpeg"),
                    Service(id = 3, title = "Furniture Making", providerId = "john_uid", providerName = "John Mwale", category = "Carpentry", description = "Beds, wardrobes, tables, and custom orders built to your size.", location = "Lilongwe", isActive = true, imageUrl = "carpently.jpeg"),
                    Service(id = 4, title = "Home Painting", providerId = "mercy_uid", providerName = "Mercy Kamanga", category = "Painting", description = "Interior and exterior painting with a one-year touch-up guarantee.", location = "Mzuzu", isActive = true, imageUrl = "painting.jpeg"),
                    Service(id = 5, title = "Event Catering", providerId = "tee_uid", providerName = "Tee", category = "Catering", description = "Food for weddings, funerals, and parties of any size.", location = "Zomba", isActive = false, imageUrl = "tailor.jpeg")
                )
                seedServices.forEach { service: Service ->
                    db.collection("services").document(service.id.toString()).set(
                        mapOf(
                            "id" to service.id,
                            "title" to service.title,
                            "providerId" to service.providerId,
                            "providerName" to service.providerName,
                            "category" to service.category,
                            "description" to service.description,
                            "location" to service.location,
                            "isActive" to service.isActive,
                            "imageUrl" to service.imageUrl
                        )
                    )
                }

                val seedSubmissions = listOf(
                    Submission(id = 1, serviceId = 4, requesterId = "tee_uid", requesterName = "Tee", providerId = "mercy_uid", submittedOn = "28 Sep 2026", status = SubmissionStatus.COMPLETED, answers = listOf("Full name" to "Tee", "Phone number" to "0999 123 456", "Describe what you need" to "Repaint two bedrooms")),
                    Submission(id = 2, serviceId = 3, requesterId = "tee_uid", requesterName = "Tee", providerId = "john_uid", submittedOn = "01 Oct 2026", status = SubmissionStatus.COMPLETED, answers = listOf("Your name" to "Tee", "Item to build" to "Wardrobe", "Width (cm)" to "120", "Height (cm)" to "200")),
                    Submission(id = 3, serviceId = 2, requesterId = "tee_uid", requesterName = "Tee", providerId = "chimwemwe_uid", submittedOn = "03 Oct 2026", status = SubmissionStatus.PENDING, answers = listOf("Your name" to "Tee", "Event date" to "20 Dec 2026", "Number of guests" to "150", "Venue" to "Kamuzu Palace Hall")),
                    Submission(id = 4, serviceId = 1, requesterId = "alice_uid", requesterName = "Alice Moyo", providerId = "tee_uid", submittedOn = "30 Sep 2026", status = SubmissionStatus.PENDING, answers = listOf("Full name" to "Alice Moyo", "Chest (cm)" to "90", "Waist (cm)" to "76", "Needed by" to "15 Oct 2026")),
                    Submission(id = 5, serviceId = 1, requesterId = "peter_uid", requesterName = "Peter Zulu", providerId = "tee_uid", submittedOn = "02 Oct 2026", status = SubmissionStatus.IN_PROGRESS, answers = listOf("Full name" to "Peter Zulu", "Chest (cm)" to "104", "Waist (cm)" to "92", "Needed by" to "25 Oct 2026"))
                )
                seedSubmissions.forEach { sub: Submission ->
                    db.collection("submissions").document(sub.id.toString()).set(
                        mapOf(
                            "id" to sub.id,
                            "serviceId" to sub.serviceId,
                            "requesterId" to sub.requesterId,
                            "requesterName" to sub.requesterName,
                            "providerId" to sub.providerId,
                            "submittedOn" to sub.submittedOn,
                            "status" to sub.status.name,
                            "answers" to sub.answers.map { pair: Pair<String, String> -> mapOf("label" to pair.first, "value" to pair.second) }
                        )
                    )
                }

                val seedCatalogue = listOf(
                    CatalogueItem(id = 1, providerId = "tee_uid", providerName = "Tee", serviceId = 1, title = "Bespoke Royal Suit", description = "3-piece tailored wool suit crafted for a high-profile wedding.", date = "Aug 2026", imageUrl = "tailor.jpeg"),
                    CatalogueItem(id = 2, providerId = "tee_uid", providerName = "Tee", serviceId = 1, title = "Custom Evening Dress", description = "Hand-stitched evening dress with traditional chitenje accents.", date = "Jul 2026", imageUrl = "tailor.jpeg"),
                    CatalogueItem(id = 3, providerId = "tee_uid", providerName = "Tee", serviceId = 5, title = "Corporate Buffet Catering", description = "Full course meal catering for 150 guests at Lilongwe Hotel.", date = "Sep 2026", imageUrl = "tailor.jpeg"),
                    CatalogueItem(id = 4, providerId = "chimwemwe_uid", providerName = "Chimwemwe Phiri", serviceId = 2, title = "Lake Malawi Sunset Wedding", description = "Romantic outdoor wedding shoot at Sunbird Livingstonia Beach.", date = "Sep 2026", imageUrl = "photography.jpeg"),
                    CatalogueItem(id = 5, providerId = "chimwemwe_uid", providerName = "Chimwemwe Phiri", serviceId = 2, title = "Portrait Session", description = "Studio portrait session for corporate branding.", date = "Aug 2026", imageUrl = "photography.jpeg"),
                    CatalogueItem(id = 6, providerId = "john_uid", providerName = "John Mwale", serviceId = 3, title = "Mahogany Dining Table", description = "Solid 8-seater dining set made from seasoned Malawian mahogany.", date = "May 2026", imageUrl = "carpently.jpeg"),
                    CatalogueItem(id = 7, providerId = "john_uid", providerName = "John Mwale", serviceId = 3, title = "Master Closet Wardrobe", description = "Spacious 4-door wardrobe with sliding mirror panels.", date = "Jun 2026", imageUrl = "carpently.jpeg"),
                    CatalogueItem(id = 8, providerId = "mercy_uid", providerName = "Mercy Kamanga", serviceId = 4, title = "Modern Villa Exterior", description = "Weather-resistant dual coat exterior painting in Mzuzu.", date = "Jul 2026", imageUrl = "painting.jpeg")
                )
                seedCatalogue.forEach { item: CatalogueItem ->
                    db.collection("catalogue").document(item.id.toString()).set(
                        mapOf(
                            "id" to item.id,
                            "providerId" to item.providerId,
                            "providerName" to item.providerName,
                            "serviceId" to item.serviceId,
                            "title" to item.title,
                            "description" to item.description,
                            "date" to item.date,
                            "imageUrl" to item.imageUrl
                        )
                    )
                }

                val seedRatings = listOf(
                    ServiceRating(id = 1, serviceId = 1, submissionId = 4, requesterId = "alice_uid", clientName = "Alice Moyo", providerId = "tee_uid", providerName = "Tee", stars = 5, comment = "Perfect fit for my suit! Excellent craftsmanship.", date = "15 Sep 2026"),
                    ServiceRating(id = 2, serviceId = 1, submissionId = 5, requesterId = "peter_uid", clientName = "Peter Zulu", providerId = "tee_uid", providerName = "Tee", stars = 4, comment = "Great fabric selection and friendly service.", date = "20 Sep 2026")
                )
                seedRatings.forEach { r: ServiceRating ->
                    db.collection("ratings").document(r.id.toString()).set(
                        mapOf(
                            "id" to r.id,
                            "serviceId" to r.serviceId,
                            "submissionId" to r.submissionId,
                            "requesterId" to r.requesterId,
                            "clientName" to r.clientName,
                            "providerId" to r.providerId,
                            "providerName" to r.providerName,
                            "stars" to r.stars,
                            "comment" to r.comment,
                            "date" to r.date
                        )
                    )
                }

                val seedProviders = listOf(
                    ProviderProfile(providerId = "tee_uid", name = "Tee", bio = "Professional fashion designer & tailor with 8+ years experience in custom suit fittings, ceremonial wear, and event catering.", location = "Lilongwe", phone = "0999 000 000", email = "tee@example.com"),
                    ProviderProfile(providerId = "chimwemwe_uid", name = "Chimwemwe Phiri", bio = "Award-winning documentary and wedding photographer capturing timeless moments across Malawi.", location = "Blantyre", phone = "0888 111 222", email = "chimwemwe@example.com"),
                    ProviderProfile(providerId = "john_uid", name = "John Mwale", bio = "Master carpenter specializing in solid hardwood furniture, bespoke wardrobes, and modern dining sets.", location = "Lilongwe", phone = "0995 333 444", email = "john@example.com"),
                    ProviderProfile(providerId = "mercy_uid", name = "Mercy Kamanga", bio = "Expert interior & exterior painter offering premium finishes, color consulting, and wall restoration.", location = "Mzuzu", phone = "0884 555 666", email = "mercy@example.com")
                )
                seedProviders.forEach { p: ProviderProfile ->
                    db.collection("providers").document(p.name).set(
                        mapOf(
                            "providerId" to p.providerId,
                            "name" to p.name,
                            "bio" to p.bio,
                            "location" to p.location,
                            "phone" to p.phone,
                            "email" to p.email,
                            "avatarUrl" to p.avatarUrl
                        )
                    )
                }

                val seedUsers = listOf(
                    mapOf("uid" to "tee_uid", "fullName" to "Tee", "email" to "tee@example.com", "phone" to "0999 000 000"),
                    mapOf("uid" to "chimwemwe_uid", "fullName" to "Chimwemwe Phiri", "email" to "chimwemwe@example.com", "phone" to "0888 111 222"),
                    mapOf("uid" to "john_uid", "fullName" to "John Mwale", "email" to "john@example.com", "phone" to "0995 333 444"),
                    mapOf("uid" to "mercy_uid", "fullName" to "Mercy Kamanga", "email" to "mercy@example.com", "phone" to "0884 555 666"),
                    mapOf("uid" to "alice_uid", "fullName" to "Alice Moyo", "email" to "alice@example.com", "phone" to "0998 777 888")
                )
                seedUsers.forEach { u: Map<String, String> ->
                    db.collection("users").document(u["uid"]!!).set(u)
                }
            }
        }
    }

    // --- WRITE OPERATIONS TO FIRESTORE ---

    fun saveService(service: Service) {
        db.collection("services").document(service.id.toString()).set(
            mapOf(
                "id" to service.id,
                "title" to service.title,
                "providerId" to service.providerId,
                "providerName" to service.providerName,
                "category" to service.category,
                "description" to service.description,
                "location" to service.location,
                "isActive" to service.isActive,
                "imageUrl" to service.imageUrl
            ),
            SetOptions.merge()
        )
    }

    fun submitRequest(submission: Submission) {
        db.collection("submissions").document(submission.id.toString()).set(
            mapOf(
                "id" to submission.id,
                "serviceId" to submission.serviceId,
                "requesterId" to submission.requesterId,
                "requesterName" to submission.requesterName,
                "providerId" to submission.providerId,
                "submittedOn" to submission.submittedOn,
                "status" to submission.status.name,
                "answers" to submission.answers.map { pair: Pair<String, String> -> mapOf("label" to pair.first, "value" to pair.second) }
            )
        )
    }

    fun updateSubmissionStatus(submissionId: Int, status: SubmissionStatus) {
        db.collection("submissions").document(submissionId.toString()).update("status", status.name)
    }

    fun addCatalogueItem(item: CatalogueItem) {
        db.collection("catalogue").document(item.id.toString()).set(
            mapOf(
                "id" to item.id,
                "providerId" to item.providerId,
                "providerName" to item.providerName,
                "serviceId" to item.serviceId,
                "title" to item.title,
                "description" to item.description,
                "date" to item.date,
                "imageUrl" to item.imageUrl
            )
        )
    }

    fun addRating(rating: ServiceRating) {
        db.collection("ratings").document(rating.id.toString()).set(
            mapOf(
                "id" to rating.id,
                "serviceId" to rating.serviceId,
                "submissionId" to rating.submissionId,
                "requesterId" to rating.requesterId,
                "clientName" to rating.clientName,
                "providerId" to rating.providerId,
                "providerName" to rating.providerName,
                "stars" to rating.stars,
                "comment" to rating.comment,
                "date" to rating.date
            )
        )
    }

    fun saveFormFields(serviceId: Int, fields: List<FormField>) {
        val mappedFields = fields.map { f: FormField ->
            mapOf(
                "id" to f.id,
                "label" to f.label,
                "type" to f.type.name,
                "required" to f.required
            )
        }
        db.collection("forms").document(serviceId.toString()).set(
            mapOf("serviceId" to serviceId, "fields" to mappedFields)
        )
    }
}
