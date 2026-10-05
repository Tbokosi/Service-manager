package com.example.service_manager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillFormScreen(
    serviceId: Int,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    val service = dummyServices.find { it.id == serviceId }
    val fields = dummyForms[serviceId] ?: defaultForm
    val formOpen = formPublished[serviceId] ?: true

    val answers = remember { mutableStateMapOf<Int, String>() }
    val errors = remember { mutableStateMapOf<Int, String>() }
    var showConfirmation by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        errors.clear()
        fields.forEach { field ->
            val value = answers[field.id].orEmpty().trim()
            if (field.required && value.isEmpty()) {
                errors[field.id] = if (field.type == FieldType.IMAGE) "Please add an image"
                else "This field is required"
            } else if (field.type == FieldType.NUMBER && value.isNotEmpty() &&
                value.toDoubleOrNull() == null
            ) {
                errors[field.id] = "Enter a valid number"
            }
        }
        return errors.isEmpty()
    }
    fun submit() {
        val newId = (dummySubmissions.maxOfOrNull { it.id } ?: 0) + 1
        dummySubmissions.add(
            0,
            Submission(
                id = newId,
                serviceId = serviceId,
                requesterName = currentUserName,
                submittedOn = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                status = SubmissionStatus.PENDING,
                answers = fields.map { field ->
                    val value = answers[field.id].orEmpty().trim()
                    field.label to when {
                        value.isEmpty() -> "-"
                        field.type == FieldType.IMAGE -> "Image attached"
                        else -> value
                    }
                }
            )
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Request form") },
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
                    onClick = {
                        if (validate()) {
                            submit()
                            showConfirmation = true
                        }
                    },
                    enabled = formOpen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Text("Submit request")
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
            Column {
                Text(
                    text = service?.title ?: "Service",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (service != null) {
                    Text(
                        text = "by ${service.providerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fields marked * are required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!formOpen) {
                Text(
                    text = "This form isn't accepting requests right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            fields.forEach { field ->
                FormFieldInput(
                    field = field,
                    value = answers[field.id].orEmpty(),
                    error = errors[field.id],
                    onValueChange = {
                        answers[field.id] = it
                        errors.remove(field.id)
                    }
                )
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Request sent") },
            text = {
                Text("Your request for ${service?.title ?: "this service"} was submitted. You can track it under My requests.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    onDone()
                }) { Text("OK") }
            }
        )
    }
}

// One composable that picks the right input for each field type
@Composable
private fun FormFieldInput(
    field: FormField,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    val label = if (field.required) "${field.label} *" else field.label

    when (field.type) {
        FieldType.TEXT -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        FieldType.NUMBER -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        FieldType.DATE -> DateField(label, value, error, onValueChange)

        FieldType.IMAGE -> ImageField(label, value, error, onValueChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick date") },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Invisible layer on top that catches taps
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onValueChange(formatDate(it)) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun ImageField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onValueChange(uri.toString())
    }

    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (value.isEmpty()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Choose image")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Image selected (tap to change)")
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@Preview(showBackground = true)
@Composable
fun FillFormPreview() {
    ServicemanagerTheme {
        FillFormScreen(serviceId = 1)
    }
}