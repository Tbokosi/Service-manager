package com.example.service_manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailsScreen(
    submissionId: Int,
    asProvider: Boolean,
    onBack: () -> Unit = {},
    onProviderClick: (String) -> Unit = {}
) {
    val index = dummySubmissions.indexOfFirst { it.id == submissionId }
    val submission = dummySubmissions.getOrNull(index)
    val service = submission?.let { s -> dummyServices.find { it.id == s.serviceId } }

    var selectedStars by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var ratingSubmitted by remember { mutableStateOf(false) }

    val existingRating = dummyRatings.find { it.submissionId == submissionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (submission == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Request not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Service image & Header
            if (service != null) {
                AssetImage(
                    assetPath = service.imageUrl,
                    contentDescription = service.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = service?.title ?: "Unknown service",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(submission.status)
                }
                Spacer(Modifier.height(4.dp))
                if (asProvider) {
                    Text(
                        text = "From ${submission.requesterName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (service != null) {
                    Text(
                        text = "Provider: ${service.providerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onProviderClick(service.providerName) }
                    )
                }
                Text(
                    text = "Submitted ${submission.submittedOn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status switcher (provider only)
            if (asProvider) {
                Column {
                    Text(
                        text = "Update status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    val options = SubmissionStatus.values()
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        options.forEachIndexed { i, status ->
                            SegmentedButton(
                                selected = submission.status == status,
                                onClick = {
                                    dummySubmissions[index] = submission.copy(status = status)
                                },
                                shape = SegmentedButtonDefaults.itemShape(i, options.size)
                            ) {
                                Text(
                                    text = status.label,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // Rating Section (When service is COMPLETED and user is client)
            if (submission.status == SubmissionStatus.COMPLETED) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Service Rating & Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))

                        val activeRating = existingRating ?: if (ratingSubmitted) dummyRatings.find { it.submissionId == submissionId } else null

                        if (activeRating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { starIndex ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (starIndex < activeRating.stars) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${activeRating.stars}/5 stars",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (activeRating.comment.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "\"${activeRating.comment}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Reviewed by ${activeRating.clientName} on ${activeRating.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else if (!asProvider) {
                            Text(
                                text = "How was your experience? Rate this completed service:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))

                            // Interactive 5-star picker
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { selectedStars = star },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Outlined.Star,
                                            contentDescription = "$star stars",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                label = { Text("Write a comment (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (service != null) {
                                        dummyRatings.add(
                                            ServiceRating(
                                                id = (dummyRatings.maxOfOrNull { it.id } ?: 0) + 1,
                                                serviceId = service.id,
                                                submissionId = submission.id,
                                                clientName = currentUserName,
                                                providerName = service.providerName,
                                                stars = selectedStars,
                                                comment = reviewComment.trim(),
                                                date = "Today"
                                            )
                                        )
                                        ratingSubmitted = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Rating")
                            }
                        } else {
                            Text(
                                text = "No rating submitted by client yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Submitted Answers
            Column {
                Text(
                    text = "Submitted information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        submission.answers.forEachIndexed { i, (label, value) ->
                            AnswerRow(label, value)
                            if (i < submission.answers.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionDetailsPreview() {
    ServicemanagerTheme {
        SubmissionDetailsScreen(submissionId = 4, asProvider = true)
    }
}
