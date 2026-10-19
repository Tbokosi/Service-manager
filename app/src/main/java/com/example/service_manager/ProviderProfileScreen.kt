package com.example.service_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerName: String,
    onBack: () -> Unit = {},
    onServiceClick: (Int) -> Unit = {}
) {
    val provider = getProviderProfile(providerName)
    val (overallRating, totalReviews) = getProviderOverallRating(providerName)
    val catalogue = getProviderCatalogue(providerName)
    val services = getProviderServices(providerName)
    val reviews = getProviderRatings(providerName)

    var selectedTab by remember { mutableStateOf(0) }
    var showAddWorkDialog by remember { mutableStateOf(false) }
    var selectedCatalogueItem by remember { mutableStateOf<CatalogueItem?>(null) }

    val tabs = listOf("Catalogue (${catalogue.size})", "Services (${services.size})", "Reviews (${reviews.size})")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(provider.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = provider.name.first().toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = provider.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Rating badge & Contact info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (totalReviews > 0)
                                        "${"%.1f".format(overallRating)} ($totalReviews review${if (totalReviews == 1) "" else "s"})"
                                    else "No ratings yet",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = provider.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Navigation Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> CatalogueTab(
                    catalogue = catalogue,
                    isOwner = provider.name == currentUserName,
                    onAddWorkClick = { showAddWorkDialog = true },
                    onItemClick = { item -> selectedCatalogueItem = item }
                )
                1 -> ServicesTab(services, onServiceClick)
                2 -> ReviewsTab(reviews)
            }
        }
    }

    // Dialog for viewing a catalogue item in detail
    val activeItem = selectedCatalogueItem
    if (activeItem != null) {
        AlertDialog(
            onDismissRequest = { selectedCatalogueItem = null },
            title = { Text(activeItem.title) },
            text = {
                Column {
                    AssetImage(
                        assetPath = activeItem.imageUrl,
                        contentDescription = activeItem.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Completed in ${activeItem.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = activeItem.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                val serviceId: Int? = activeItem.serviceId
                if (serviceId != null) {
                    Button(onClick = {
                        selectedCatalogueItem = null
                        onServiceClick(serviceId)
                    }) {
                        Text("Book this Service")
                    }
                } else {
                    TextButton(onClick = { selectedCatalogueItem = null }) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (activeItem.serviceId != null) {
                    TextButton(onClick = { selectedCatalogueItem = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Dialog for adding completed work to catalogue
    if (showAddWorkDialog) {
        AddWorkDialog(
            providerName = providerName,
            providerServices = services,
            onDismiss = { showAddWorkDialog = false },
            onAdd = { title, serviceId, desc, img ->
                val maxId = dummyCatalogue.maxOfOrNull { c: CatalogueItem -> c.id } ?: 0
                dummyCatalogue.add(
                    CatalogueItem(
                        id = maxId + 1,
                        providerName = providerName,
                        serviceId = serviceId,
                        title = title,
                        description = desc,
                        date = "Recent",
                        imageUrl = img
                    )
                )
                showAddWorkDialog = false
            }
        )
    }
}

@Composable
private fun CatalogueTab(
    catalogue: List<CatalogueItem>,
    isOwner: Boolean,
    onAddWorkClick: () -> Unit,
    onItemClick: (CatalogueItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isOwner) {
            Button(
                onClick = onAddWorkClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Photo of Completed Work")
            }
        }

        if (catalogue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No previous work added to catalogue yet")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(catalogue, key = { it.id }) { item ->
                    Card(
                        onClick = { onItemClick(item) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            AssetImage(
                                assetPath = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWorkDialog(
    providerName: String,
    providerServices: List<Service>,
    onDismiss: () -> Unit,
    onAdd: (String, Int?, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedServiceId by remember { mutableStateOf(providerServices.firstOrNull()?.id) }
    var selectedImage by remember { mutableStateOf("tailor.jpeg") }

    val sampleImages = listOf(
        "tailor.jpeg" to "Tailoring",
        "photography.jpeg" to "Photo",
        "carpently.jpeg" to "Carpentry",
        "painting.jpeg" to "Painting"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Completed Work") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { input: String -> title = input },
                    label = { Text("Title of finished work") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { input: String -> description = input },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Sample Photo:", style = MaterialTheme.typography.bodySmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleImages.forEach { (img, label) ->
                        FilterChip(
                            selected = selectedImage == img,
                            onClick = { selectedImage = img },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), selectedServiceId, description.trim(), selectedImage)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add to Portfolio")
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
private fun ServicesTab(services: List<Service>, onServiceClick: (Int) -> Unit) {
    if (services.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No active services listed")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services, key = { it.id }) { service ->
                Card(
                    onClick = { onServiceClick(service.id) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssetImage(
                            assetPath = service.imageUrl,
                            contentDescription = service.title,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = service.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = service.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = service.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsTab(reviews: List<ServiceRating>) {
    if (reviews.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No reviews yet")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews, key = { it.id }) { review ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = review.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < review.stars) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${review.stars}/5",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = review.comment,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
