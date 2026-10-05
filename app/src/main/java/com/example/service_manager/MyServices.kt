package com.example.service_manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

@Composable
fun MyServicesScreen(
    onCreateService: () -> Unit = {},
    onOpenRequests: () -> Unit = {},
    onEditService: (Int) -> Unit = {}
) {
    val myServices = dummyServices.filter { it.providerName == currentUserName }
    val myServiceIds = myServices.map { it.id }
    val incoming = dummySubmissions.filter { it.serviceId in myServiceIds }
    val pendingCount = incoming.count { it.status == SubmissionStatus.PENDING }

    fun setActive(service: Service, active: Boolean) {
        val index = dummyServices.indexOfFirst { it.id == service.id }
        if (index >= 0) dummyServices[index] = service.copy(isActive = active)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "My services",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage what you offer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            RequestsSummaryCard(
                total = incoming.size,
                pending = pendingCount,
                onClick = onOpenRequests
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Your services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (myServices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't created a service yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {
                    items(myServices, key = { it.id }) { service ->
                        MyServiceCard(
                            service = service,
                            requestCount = incoming.count { it.serviceId == service.id },
                            onToggle = { setActive(service, it) },
                            onClick = { onEditService(service.id) }
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCreateService,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("New service") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun RequestsSummaryCard(total: Int, pending: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Received requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$total total • $pending pending",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MyServiceCard(
    service: Service,
    requestCount: Int,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = service.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$requestCount request${if (requestCount == 1) "" else "s"} • " +
                            if (service.isActive) "Published" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (service.isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = service.isActive, onCheckedChange = onToggle)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyServicesPreview() {
    ServicemanagerTheme {
        MyServicesScreen()
    }
}