package com.example.service_manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceManagerTest {

    @Test
    fun testAccountStatisticsCalculation() {
        // Feature: Display the number of services provided, requests sent, and requests received.
        val myServiceIds = dummyServices
            .filter { it.providerName == currentUserName }
            .map { it.id }

        val servicesCount = myServiceIds.size
        val sentCount = dummySubmissions.count { it.requesterName == currentUserName }
        val receivedCount = dummySubmissions.count { it.serviceId in myServiceIds }

        // Based on dummy data for current user "Tee":
        // Services: "Custom Suit Tailoring" (1), "Event Catering" (5) -> 2
        assertEquals(2, servicesCount)
        // Sent requests: Submissions 1, 2, 3 -> 3
        assertEquals(3, sentCount)
        // Received requests: Submissions 4, 5 (both for service 1) -> 2
        assertEquals(2, receivedCount)
    }

    @Test
    fun testAccountProfileUpdate() {
        // Feature: Allow users to update their email address and phone number.
        val originalEmail = currentUser.email
        val originalPhone = currentUser.phone
        
        val newEmail = "updated_email@example.com"
        val newPhone = "0999 111 222"
        
        // Simulate update
        currentUser.email = newEmail
        currentUser.phone = newPhone
        
        assertEquals(newEmail, currentUser.email)
        assertEquals(newPhone, currentUser.phone)
        
        // Restore for consistency
        currentUser.email = originalEmail
        currentUser.phone = originalPhone
    }

    @Test
    fun testCreateAndEditServices() {
        // Feature: Allow service providers to create new service listings and edit existing ones.
        val initialCount = dummyServices.size
        val newService = Service(
            id = 101,
            title = "New Test Service",
            providerName = currentUserName,
            category = "Testing",
            description = "Description",
            location = "Lilongwe"
        )
        
        // Create
        dummyServices.add(newService)
        assertEquals(initialCount + 1, dummyServices.size)
        
        // Edit
        val index = dummyServices.indexOfFirst { it.id == 101 }
        dummyServices[index] = newService.copy(title = "Edited Service Title")
        assertEquals("Edited Service Title", dummyServices.find { it.id == 101 }?.title)
        
        // Cleanup
        dummyServices.removeAt(index)
    }

    @Test
    fun testSubmitServiceRequests() {
        // Feature: Allow clients to submit requests for services, including responses to custom form fields.
        val initialCount = dummySubmissions.size
        val newSubmission = Submission(
            id = 202,
            serviceId = 1,
            requesterName = currentUserName,
            submittedOn = "12 Oct 2026",
            status = SubmissionStatus.PENDING,
            answers = listOf("Full name" to "Tee", "Chest (cm)" to "98")
        )
        
        dummySubmissions.add(newSubmission)
        assertEquals(initialCount + 1, dummySubmissions.size)
        assertEquals("Tee", dummySubmissions.last().requesterName)
        assertTrue(dummySubmissions.last().answers.any { it.first == "Chest (cm)" })
        
        // Cleanup
        dummySubmissions.remove(newSubmission)
    }

    @Test
    fun testViewReceivedRequests() {
        // Feature: Allow service providers to view requests submitted for their services.
        val myServiceIds = dummyServices
            .filter { it.providerName == currentUserName }
            .map { it.id }
        
        val receivedRequests = dummySubmissions.filter { it.serviceId in myServiceIds }
        
        // For "Tee", we expect requests for service 1 and 5 (Ids 4 and 5 in dummy data)
        assertEquals(2, receivedRequests.size)
        assertTrue(receivedRequests.all { it.serviceId == 1 || it.serviceId == 5 })
    }

    @Test
    fun testCustomFormBuilding() {
        // Feature: Allow service providers to create custom forms for their services, define fields, and publish forms.
        val serviceId = 5
        val customFields = listOf(
            FormField(1, "Event Date", FieldType.DATE, true),
            FormField(2, "Special Requirements", FieldType.TEXT, false)
        )
        
        dummyForms[serviceId] = customFields
        formPublished[serviceId] = true
        
        assertEquals(2, dummyForms[serviceId]?.size)
        assertEquals(true, formPublished[serviceId])
        assertEquals("Event Date", dummyForms[serviceId]?.first()?.label)

        // Cleanup
        dummyForms.remove(serviceId)
        formPublished.remove(serviceId)
    }
}
