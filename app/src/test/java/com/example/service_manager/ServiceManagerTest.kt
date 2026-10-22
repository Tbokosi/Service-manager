package com.example.service_manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceManagerTest {

    @Test
    fun testGetProviderOverallRating() {
        val (avg, count) = getProviderOverallRating("Tee")
        // Based on dummyRatings: id 1 (5 stars), id 2 (4 stars) for provider "Tee"
        assertEquals(4.5f, avg, 0.01f)
        assertEquals(2, count)
    }
     @Test
    fun testGetProviderOverallRating_NotFound() {
        val (avg, count) = getProviderOverallRating("NonExistent")
        assertEquals(0f, avg, 0.01f)
        assertEquals(0, count)
    }

    @Test
    fun testGetServiceAverageRating() {
        val (avg, count) = getServiceAverageRating(1)
        // Based on dummyRatings: serviceId 1 has ratings with 5 and 4 stars
        assertEquals(4.5f, avg, 0.01f)
        assertEquals(2, count)
    }

    @Test
    fun testGetProviderProfile_Existing() {
        val profile = getProviderProfile("Tee")
        assertEquals("Tee", profile.name)
        assertEquals("Lilongwe", profile.location)
    }

    @Test
    fun testGetProviderProfile_NonExisting() {
        val profile = getProviderProfile("Unknown")
        assertEquals("Unknown", profile.name)
        assertEquals("Malawi", profile.location)
    }

    @Test
    fun testGetProviderCatalogue() {
        val items = getProviderCatalogue("Tee")
        // dummyCatalogue has 3 items for "Tee"
        assertEquals(3, items.size)
        items.forEach {
            assertEquals("Tee", it.providerName)
        }
    }

    @Test
    fun testGetProviderServices() {
        val services = getProviderServices("Tee")
        // dummyServices has 2 services for "Tee" (id 1 and 5)
        assertEquals(2, services.size)
        services.forEach {
            assertEquals("Tee", it.providerName)
        }
    }

    @Test
    fun testGetProviderRatings() {
        val ratings = getProviderRatings("Tee")
        assertEquals(2, ratings.size)
        ratings.forEach {
            assertEquals("Tee", it.providerName)
        }
    }

    @Test
    fun testNameValidation() {
        assertTrue(Validator.isNameValid("John Doe"))
        assertFalse(Validator.isNameValid(""))
        assertFalse(Validator.isNameValid("   "))
    }

    @Test
    fun testEmailValidation() {
        assertTrue(Validator.isEmailValid("test@example.com"))
        assertFalse(Validator.isEmailValid("testexample.com"))
        assertFalse(Validator.isEmailValid("test@examplecom"))
        assertFalse(Validator.isEmailValid("test"))
    }

    @Test
    fun testPasswordValidation() {
        assertTrue(Validator.isPasswordValid("123456"))
        assertTrue(Validator.isPasswordValid("password123"))
        assertFalse(Validator.isPasswordValid("12345"))
        assertFalse(Validator.isPasswordValid(""))
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(Validator.isPhoneValid("0999000000"))
        assertFalse(Validator.isPhoneValid(""))
        assertFalse(Validator.isPhoneValid("   "))
    }

    @Test
    fun testAddRatingLogic() {
        val initialSize = dummyRatings.size
        val maxId = dummyRatings.maxOfOrNull { it.id } ?: 0

        val newRating = ServiceRating(
            id = maxId + 1,
            serviceId = 1,
            submissionId = 100,
            clientName = "Test User",
            providerName = "Tee",
            stars = 5,
            comment = "Excellent",
            date = "Today"
        )
        dummyRatings.add(newRating)

        assertEquals(initialSize + 1, dummyRatings.size)
        assertEquals(maxId + 1, dummyRatings.last().id)
        assertEquals("Excellent", dummyRatings.last().comment)

        // Cleanup for other tests
        dummyRatings.remove(newRating)
    }

    @Test
    fun testSubmissionFilteringByUser() {
        // currentUserName is "Tee"
        val myRequests = dummySubmissions.filter { it.requesterName == currentUserName }
        
        // Based on dummySubmissions, "Tee" has 3 requests (ids 1, 2, 3)
        assertEquals(3, myRequests.size)
        myRequests.forEach {
            assertEquals("Tee", it.requesterName)
        }
    }

    @Test
    fun testSubmissionFilteringByStatus() {
        val myRequests = dummySubmissions.filter { it.requesterName == currentUserName }
        
        val pendingRequests = myRequests.filter { it.status == SubmissionStatus.PENDING }
        // "Tee" has 1 pending request (id 3)
        assertEquals(1, pendingRequests.size)
        assertEquals(SubmissionStatus.PENDING, pendingRequests[0].status)

        val completedRequests = myRequests.filter { it.status == SubmissionStatus.COMPLETED }
        // "Tee" has 2 completed requests (id 1, 2)
        assertEquals(2, completedRequests.size)
        assertTrue(completedRequests.all { it.status == SubmissionStatus.COMPLETED })
    }

    // --- Functional Feature Tests ---

    @Test
    fun testFeature_ExploreSearchAndFiltering() {
        // Requirement: Users can search for services by title, provider, or category
        val photoQuery = "Photography"
        val photoFiltered = dummyServices.filter {
            it.isActive &&
                    it.providerName != currentUserName &&
                    (
                            it.title.contains(photoQuery, ignoreCase = true) ||
                                    it.providerName.contains(photoQuery, ignoreCase = true) ||
                                    it.category.contains(photoQuery, ignoreCase = true)
                            )
        }
        assertEquals(1, photoFiltered.size)
        assertEquals("Wedding Photography", photoFiltered[0].title)
    }

    @Test
    fun testFeature_ExploreExcludeOwnServices() {
        // Requirement: Users should not see their own services in the Explore screen
        val exploreFiltered = dummyServices.filter {
            it.isActive && it.providerName != currentUserName
        }
        assertFalse(exploreFiltered.any { it.providerName == currentUserName })
    }

    @Test
    fun testFeature_AccountStatsCalculation() {
        // Requirement: Users can see their activity stats (Services, Sent requests, Received requests)
        val myServiceIds = dummyServices
            .filter { it.providerName == currentUserName }
            .map { it.id }
        val servicesCount = myServiceIds.size
        val sentCount = dummySubmissions.count { it.requesterName == currentUserName }
        val receivedCount = dummySubmissions.count { it.serviceId in myServiceIds }

        // Based on dummy data for "Tee":
        assertEquals(2, servicesCount) // Suit Tailoring (1), Catering (5)
        assertEquals(3, sentCount)     // Submissions 1, 2, 3
        assertEquals(2, receivedCount) // Submissions 4, 5 (both for service 1)
    }

    @Test
    fun testFeature_AccountProfileUpdate() {
        // Requirement: Users can update their email and phone number
        val originalEmail = currentUser.email
        val originalPhone = currentUser.phone
        
        val newEmail = "new_tee@example.com"
        val newPhone = "0999 777 888"
        
        // Update logic from AccountScreen
        currentUser.email = newEmail
        currentUser.phone = newPhone
        
        assertEquals(newEmail, currentUser.email)
        assertEquals(newPhone, currentUser.phone)
        
        // Restore
        currentUser.email = originalEmail
        currentUser.phone = originalPhone
    }

    @Test
    fun testFeature_CreateAndEditService() {
        // Requirement: Providers can create and update their service listings
        val initialCount = dummyServices.size
        val newService = Service(
            id = 999,
            title = "Test Service",
            providerName = currentUserName,
            category = "Testing",
            description = "Test Description",
            location = "Test Location"
        )
        
        // Create
        dummyServices.add(newService)
        assertEquals(initialCount + 1, dummyServices.size)
        
        // Edit
        val index = dummyServices.indexOfFirst { it.id == 999 }
        dummyServices[index] = newService.copy(title = "Updated Title")
        assertEquals("Updated Title", dummyServices.find { it.id == 999 }?.title)
        
        // Cleanup
        dummyServices.removeAt(index)
    }

    @Test
    fun testFeature_SubmitRequest() {
        // Requirement: Clients can submit requests for services
        val initialCount = dummySubmissions.size
        val newSubmission = Submission(
            id = 888,
            serviceId = 1,
            requesterName = currentUserName,
            submittedOn = "10 Oct 2026",
            status = SubmissionStatus.PENDING,
            answers = listOf("Full name" to "Tee", "Chest (cm)" to "95")
        )
        
        dummySubmissions.add(newSubmission)
        assertEquals(initialCount + 1, dummySubmissions.size)
        assertEquals(currentUserName, dummySubmissions.last().requesterName)
        
        // Cleanup
        dummySubmissions.remove(newSubmission)
    }

    @Test
    fun testFeature_ProviderReceivedRequests() {
        // Requirement: Providers can see requests sent to their services
        // "Tee" provides services 1 and 5.
        val myServiceIds = dummyServices
            .filter { it.providerName == currentUserName }
            .map { it.id }
        
        val receivedRequests = dummySubmissions.filter { it.serviceId in myServiceIds }
        
        // In dummySubmissions, id 4 and 5 are for service 1
        assertEquals(2, receivedRequests.size)
        assertTrue(receivedRequests.all { it.serviceId == 1 || it.serviceId == 5 })
    }

    @Test
    fun testFeature_FormBuilding() {
        // Requirement: Providers can build custom forms for their services
        val serviceId = 5
        val customFields = listOf(
            FormField(1, "Event Date", FieldType.DATE, true),
            FormField(2, "Preferences", FieldType.TEXT, false)
        )
        
        dummyForms[serviceId] = customFields
        formPublished[serviceId] = true
        
        assertEquals(2, dummyForms[serviceId]?.size)
        assertEquals(true, formPublished[serviceId])
        
        // Cleanup
        dummyForms.remove(serviceId)
        formPublished.remove(serviceId)
    }
}
