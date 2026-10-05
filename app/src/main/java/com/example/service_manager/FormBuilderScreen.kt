package com.example.service_manager


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

fun FieldType.displayName() = when (this) {
    FieldType.TEXT -> "Text"
    FieldType.NUMBER -> "Number"
    FieldType.DATE -> "Date"
    FieldType.IMAGE -> "Image"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBuilderScreen(
    serviceId: Int,
    onBack: () -> Unit = {}
) {
    val service = dummyServices.find { it.id == serviceId }
    val fields = dummyForms[serviceId] ?: defaultForm
    val published = formPublished[serviceId] ?: true

    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FormField?>(null) }
    var deleting by remember { mutableStateOf<FormField?>(null) }

    fun update(newList: List<FormField>) {
        dummyForms[serviceId] = newList
    }

    fun move(index: Int, delta: Int) {
        val target = index + delta
        if (target !in fields.indices) return
        val list = fields.toMutableList()
        val item = list.removeAt(index)
        list.add(target, item)
        update(list)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Form builder")
                        Text(
                            text = service?.title ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = null
                    showSheet = true
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add field") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            item {
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
                                text = if (published) "Form published" else "Form inactive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (published) "Clients can submit requests"
                                else "Clients can't submit requests right now",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = published,
                            onCheckedChange = { formPublished[serviceId] = it }
                        )
                    }
                }
            }

            if (fields.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No fields yet. Tap Add field to start.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(fields, key = { _, field -> field.id }) { index, field ->
                FieldCard(
                    field = field,
                    canMoveUp = index > 0,
                    canMoveDown = index < fields.lastIndex,
                    onUp = { move(index, -1) },
                    onDown = { move(index, 1) },
                    onEdit = {
                        editing = field
                        showSheet = true
                    },
                    onDelete = { deleting = field }
                )
            }
        }
    }

    if (showSheet) {
        FieldSheet(
            initial = editing,
            onDismiss = {
                showSheet = false
                editing = null
            },
            onSave = { label, type, required ->
                val current = editing
                if (current == null) {
                    val newId = (fields.maxOfOrNull { it.id } ?: 0) + 1
                    update(fields + FormField(newId, label, type, required))
                } else {
                    update(
                        fields.map {
                            if (it.id == current.id) it.copy(label = label, type = type, required = required)
                            else it
                        }
                    )
                }
                showSheet = false
                editing = null
            }
        )
    }

    deleting?.let { target ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete field?") },
            text = { Text("\"${target.label}\" will be removed from the form.") },
            confirmButton = {
                TextButton(onClick = {
                    update(fields.filter { it.id != target.id })
                    deleting = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FieldCard(
    field: FormField,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)
        ) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = field.type.displayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = if (field.required) "Required" else "Optional",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (field.required) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onUp, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onDown, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit field")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete field")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FieldSheet(
    initial: FormField?,
    onDismiss: () -> Unit,
    onSave: (String, FieldType, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: FieldType.TEXT) }
    var required by remember { mutableStateOf(initial?.required ?: false) }
    var showError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initial == null) "Add field" else "Edit field",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Field label *") },
                placeholder = { Text("e.g. Chest (cm)") },
                singleLine = true,
                isError = showError && label.isBlank(),
                supportingText = { if (showError && label.isBlank()) Text("Label is required") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text("Field type", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldType.values().forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.displayName()) }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = required, onCheckedChange = { required = it })
            }

            Button(
                onClick = {
                    showError = true
                    if (label.isNotBlank()) onSave(label.trim(), type, required)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (initial == null) "Add field" else "Save field")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormBuilderPreview() {
    ServicemanagerTheme {
        FormBuilderScreen(serviceId = 1)
    }
}