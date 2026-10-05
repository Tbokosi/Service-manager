package com.example.service_manager


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme

@Composable
fun AccountScreen(onLogout: () -> Unit = {}) {
    var email by remember { mutableStateOf(currentUser.email) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var showErrors by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val myServiceIds = dummyServices
        .filter { it.providerName == currentUserName }
        .map { it.id }
    val servicesCount = myServiceIds.size
    val sentCount = dummySubmissions.count { it.requesterName == currentUserName }
    val receivedCount = dummySubmissions.count { it.serviceId in myServiceIds }

    val emailValid = email.contains("@") && email.contains(".")
    val phoneValid = phone.isNotBlank()
    val changed = email != currentUser.email || phone != currentUser.phone

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(0.dp))
        Text(
            text = "Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Profile header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUserName.first().toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = currentUserName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = currentUser.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Services", servicesCount, Modifier.weight(1f))
            StatCard("Sent", sentCount, Modifier.weight(1f))
            StatCard("Received", receivedCount, Modifier.weight(1f))
        }

        // Edit info
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Basic information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = currentUserName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Full name") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    saved = false
                },
                label = { Text("Email") },
                singleLine = true,
                isError = showErrors && !emailValid,
                supportingText = { if (showErrors && !emailValid) Text("Enter a valid email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    saved = false
                },
                label = { Text("Phone number") },
                singleLine = true,
                isError = showErrors && !phoneValid,
                supportingText = { if (showErrors && !phoneValid) Text("Phone number is required") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    showErrors = true
                    if (emailValid && phoneValid) {
                        email = email.trim()
                        phone = phone.trim()
                        currentUser.email = email
                        currentUser.phone = phone
                        saved = true
                    }
                },
                enabled = changed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save changes")
            }
            if (saved && !changed) {
                Text(
                    text = "Changes saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider()

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Log out")
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to log in again to use the app.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountPreview() {
    ServicemanagerTheme {
        AccountScreen()
    }
}