package com.example.service_manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ServiceManagerTest {

    @Before
    fun setup() {
        resetData()
    }

    // --- Data and Rating Logic ---

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

    // --- Validation and Submission Logic ---

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

        val result = addServiceRating(
            serviceId = 1,
            submissionId = 1, // Exists in dummySubmissions
            clientName = "Test User",
            providerName = "Tee",
            stars = 5,
            comment = "Excellent"
        )
        
        assertTrue(result)
        assertEquals(initialSize + 1, dummyRatings.size)
        assertEquals("Excellent", dummyRatings.last().comment)
    }

    @Test
    fun testAddRatingLogic_Duplicate() {
        addServiceRating(1, 1, "User", "Tee", 5, "First")
        val result = addServiceRating(1, 1, "User", "Tee", 4, "Second")
        assertFalse(result)
    }

    @Test
    fun testSubmissionFilteringByUser() {
        val myRequests = getSentRequests(currentUserName)
        // Based on dummySubmissions, "Tee" has 3 requests (ids 1, 2, 3)
        assertEquals(3, myRequests.size)
        myRequests.forEach {
            assertEquals("Tee", it.requesterName)
        }
    }

    @Test
    fun testSubmissionFilteringByStatus() {
        val myRequests = getSentRequests(currentUserName)
        val pendingRequests = myRequests.filter { it.status == SubmissionStatus.PENDING }
        // "Tee" has 1 pending request (id 3)
        assertEquals(1, pendingRequests.size)
        assertEquals(SubmissionStatus.PENDING, pendingRequests[0].status)
    }

    // --- Functional Feature Tests ---

    @Test
    fun testFeature_ExploreSearchAndFiltering() {
        val photoQuery = "Photography"
        val photoFiltered = searchServices(photoQuery, currentUserName)
        assertEquals(1, photoFiltered.size)
        assertEquals("Wedding Photography", photoFiltered[0].title)
    }

    @Test
    fun testFeature_ExploreExcludeOwnServices() {
        val exploreFiltered = searchServices("", currentUserName)
        assertFalse(exploreFiltered.any { it.providerName == currentUserName })
    }

    @Test
    fun testAccountStatisticsCalculation() {
        val (servicesCount, sentCount, receivedCount) = getAccountStats(currentUserName)

        assertEquals(2, servicesCount)
        assertEquals(3, sentCount)
        assertEquals(2, receivedCount)
    }

    @Test
    fun testFeature_AccountProfileUpdate() {
        val originalEmail = currentUser.email
        val originalPhone = currentUser.phone
        val newEmail = "updated_tee@example.com"
        val newPhone = "0999 111 222"
        
        val result = updateUserProfile(newEmail, newPhone)
        
        assertTrue(result)
        assertEquals(newEmail, currentUser.email)
        assertEquals(newPhone, currentUser.phone)
        
        // Restore
        updateUserProfile(originalEmail, originalPhone)
    }

    @Test
    fun testFeature_CreateAndEditService() {
        val initialCount = dummyServices.size
        
        // Create
        val createResult = saveService(
            id = -1, // New service
            title = "New Test Service",
            category = "Testing",
            location = "Lilongwe",
            description = "Description",
            isActive = true
        )
        
        assertTrue(createResult)
        assertEquals(initialCount + 1, dummyServices.size)
        val newService = dummyServices.last()
        assertEquals("New Test Service", newService.title)
        
        // Edit
        val editResult = saveService(
            id = newService.id,
            title = "Edited Title",
            category = newService.category,
            location = newService.location,
            description = newService.description,
            isActive = newService.isActive
        )
        
        assertTrue(editResult)
        assertEquals("Edited Title", dummyServices.find { it.id == newService.id }?.title)
    }

    @Test
    fun testFeature_SubmitRequest() {
        val initialCount = dummySubmissions.size
        val answers = mapOf(
            1 to "Tee",
            2 to "90",
            3 to "80",
            4 to "2026-10-10"
        ) // For service 1, satisfying required fields
        
        val result = submitRequest(1, answers)
        
        assertTrue(result)
        assertEquals(initialCount + 1, dummySubmissions.size)
    }

    @Test
    fun testFeature_ProviderReceivedRequests() {
        val receivedRequests = getReceivedRequests(currentUserName)
        assertEquals(2, receivedRequests.size)
    }

    @Test
    fun testFeature_FormBuilding() {
        val serviceId = 5
        val customFields = listOf(
            FormField(1, "Event Date", FieldType.DATE, true),
            FormField(2, "Notes", FieldType.TEXT, false)
        )
        
        val result = updateServiceForm(serviceId, customFields, true)
        
        assertTrue(result)
        assertEquals(2, getServiceForm(serviceId).size)
        assertEquals(true, isFormPublished(serviceId))
    }

    // --- New Negative Tests ---

    @Test
    fun testFeature_AccountStatsCalculation_Negative() {
        val (servicesCount, sentCount, receivedCount) = getAccountStats("NonExistentUser")
        
        assertEquals(0, servicesCount)
        assertEquals(0, sentCount)
        assertEquals(0, receivedCount)
    }

    @Test
    fun testFeature_AccountProfileUpdate_Negative() {
        val originalEmail = currentUser.email
        val result = updateUserProfile("invalid-email", "0999")
        
        assertFalse(result)
        assertEquals(originalEmail, currentUser.email) // Email should not have changed
    }

    @Test
    fun testFeature_CreateAndEditService_Negative() {
        // Validation fails if fields are blank
        val result = saveService(-1, "", "", "", "", true)
        assertFalse(result)
    }

    @Test
    fun testFeature_SubmitRequest_Negative() {
        // Submission fails for non-existent service
        val result = submitRequest(-999, emptyMap())
        assertFalse(result)
        
        // Submission fails if required fields are missing (Service 1 has required fields)
        val result2 = submitRequest(1, emptyMap())
        assertFalse(result2)
    }

    @Test
    fun testFeature_ProviderReceivedRequests_Negative() {
        val received = getReceivedRequests("NewRequesterOnly")
        assertEquals(0, received.size)
    }

    @Test
    fun testFeature_FormBuilding_Negative() {
        // Updating form for non-existent service fails
        val result = updateServiceForm(-999, emptyList(), true)
        assertFalse(result)
    }

    @Test
    fun testFeature_UpdateSubmissionStatus() {
        val submissionId = 3 // Exists in dummySubmissions
        val result = updateSubmissionStatus(submissionId, SubmissionStatus.IN_PROGRESS)
        assertTrue(result)
        assertEquals(SubmissionStatus.IN_PROGRESS, dummySubmissions.find { it.id == submissionId }?.status)

        // Restore
        updateSubmissionStatus(submissionId, SubmissionStatus.PENDING)
    }

    @Test
    fun testFeature_UpdateSubmissionStatus_Negative() {
        val result = updateSubmissionStatus(-999, SubmissionStatus.COMPLETED)
        assertFalse(result)
    }
}
