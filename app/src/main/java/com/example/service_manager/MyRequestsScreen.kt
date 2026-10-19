package com.example.service_manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun MyRequestsScreen(
    onRequestClick: (Int) -> Unit = {},
    onProviderClick: (String) -> Unit = {}
) {
    var selectedStatus by remember { mutableStateOf<SubmissionStatus?>(null) }
    var ratingSubmissionId by remember { mutableStateOf<Int?>(null) }

    val myRequests = dummySubmissions.filter { it.requesterName == currentUserName }
    val visible = myRequests.filter { selectedStatus == null || it.status == selectedStatus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "My requests",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track the services you've requested",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { selectedStatus = null },
                label = { Text("All") }
            )
            SubmissionStatus.values().forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = { Text(status.label) }
                )
            }
        }

        if (visible.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No requests here yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(visible, key = { it.id }) { submission ->
                    RequestCard(
                        submission = submission,
                        onClick = { onRequestClick(submission.id) },
                        onProviderClick = onProviderClick,
                        onRateClick = { ratingSubmissionId = submission.id }
                    )
                }
            }
        }
    }

    val currentSubId = ratingSubmissionId
    if (currentSubId != null) {
        val sub = dummySubmissions.find { s: Submission -> s.id == currentSubId }
        val service = if (sub != null) dummyServices.find { s: Service -> s.id == sub.serviceId } else null
        if (sub != null && service != null) {
            RatingDialog(
                serviceTitle = service.title,
                providerName = service.providerName,
                onDismiss = { ratingSubmissionId = null },
                onSubmit = { stars, comment ->
                    val maxId = dummyRatings.maxOfOrNull { r: ServiceRating -> r.id } ?: 0
                    dummyRatings.add(
                        ServiceRating(
                            id = maxId + 1,
                            serviceId = service.id,
                            submissionId = sub.id,
                            clientName = currentUserName,
                            providerName = service.providerName,
                            stars = stars,
                            comment = comment,
                            date = "Today"
                        )
                    )
                    ratingSubmissionId = null
                }
            )
        }
    }
}

@Composable
private fun RequestCard(
    submission: Submission,
    onClick: () -> Unit,
    onProviderClick: (String) -> Unit,
    onRateClick: () -> Unit
) {
    val service = dummyServices.find { it.id == submission.serviceId }
    val rating = dummyRatings.find { it.submissionId == submission.id }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (service != null) {
                    AssetImage(
                        assetPath = service.imageUrl,
                        contentDescription = service.title,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = service?.title ?: "Unknown service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusChip(submission.status)
                    }
                    Spacer(Modifier.height(4.dp))
                    if (service != null) {
                        Text(
                            text = "by ${service.providerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onProviderClick(service.providerName) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Submitted ${submission.submittedOn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (submission.status == SubmissionStatus.COMPLETED) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                if (rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < rating.stars) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Your rating: ${rating.stars}/5",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    Button(
                        onClick = onRateClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Rate completed service")
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDialog(
    serviceTitle: String,
    providerName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var selectedStars by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Service") },
        text = {
            Column {
                Text(
                    text = "$serviceTitle by $providerName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Text("Tap stars to rate:", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))

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
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Leave feedback (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(selectedStars, comment.trim()) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatusChip(status: SubmissionStatus) {
    val (container, content) = when (status) {
        SubmissionStatus.PENDING ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        SubmissionStatus.IN_PROGRESS ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        SubmissionStatus.COMPLETED ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(color = container, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyRequestsPreview() {
    ServicemanagerTheme {
        MyRequestsScreen()
    }
}
