package com.example.service_manager


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

val serviceCategories = listOf(
    "Tailoring", "Photography", "Carpentry", "Painting", "Catering", "Other"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceEditorScreen(
    serviceId: Int = -1,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onEditForm: () -> Unit = {}
) {
    val existing = dummyServices.find { it.id == serviceId }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "") }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }
    var showErrors by remember { mutableStateOf(false) }

    fun save() {
        showErrors = true
        val valid = title.isNotBlank() && category.isNotBlank() &&
                location.isNotBlank() && description.isNotBlank()
        if (!valid) return

        if (existing != null) {
            val index = dummyServices.indexOfFirst { it.id == existing.id }
            dummyServices[index] = existing.copy(
                title = title.trim(),
                category = category,
                location = location.trim(),
                description = description.trim(),
                isActive = isActive
            )
        } else {
            val newId = (dummyServices.maxOfOrNull { it.id } ?: 0) + 1
            dummyServices.add(
                Service(
                    id = newId,
                    title = title.trim(),
                    providerName = currentUserName,
                    category = category,
                    description = description.trim(),
                    location = location.trim(),
                    isActive = isActive
                )
            )
        }
        onSaved()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(if (existing != null) "Edit service" else "New service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { save() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Text(if (existing != null) "Save changes" else "Create service")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Service title *") },
                singleLine = true,
                isError = showErrors && title.isBlank(),
                supportingText = { if (showErrors && title.isBlank()) Text("Title is required") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    serviceCategories.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option) }
                        )
                    }
                }
                if (showErrors && category.isBlank()) {
                    Text(
                        text = "Choose a category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                singleLine = true,
                isError = showErrors && location.isBlank(),
                supportingText = { if (showErrors && location.isBlank()) Text("Location is required") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                minLines = 4,
                isError = showErrors && description.isBlank(),
                supportingText = { if (showErrors && description.isBlank()) Text("Description is required") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Publish / deactivate
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isActive) "Published" else "Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isActive) "Clients can find and request this service"
                            else "Hidden from clients in Explore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }

            // Request form section
            if (existing != null) {
                val fieldCount = (dummyForms[existing.id] ?: defaultForm).size
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Request form",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$fieldCount fields clients fill in when requesting this service",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onEditForm,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit request form")
                        }
                    }
                }
            } else {
                Text(
                    text = "Save the service first, then you can build its request form.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceEditorPreview() {
    ServicemanagerTheme {
        ServiceEditorScreen(serviceId = 1)
    }
}