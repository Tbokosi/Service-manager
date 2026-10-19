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
}
