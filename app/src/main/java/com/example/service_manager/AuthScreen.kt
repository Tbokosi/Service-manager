package com.example.service_manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.service_manager.ui.theme.ServicemanagerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit = {}) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val nameValid = Validator.isNameValid(name)
    val phoneValid = Validator.isPhoneValid(phone)
    val emailValid = Validator.isEmailValid(email)
    val passwordValid = Validator.isPasswordValid(password)

    fun parseError(exc: Exception): String {
        val msg = exc.localizedMessage ?: "Authentication failed"
        return if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
            msg.contains("Configuration not found", ignoreCase = true)
        ) {
            "Email/Password sign-in is disabled in your Firebase Console.\n\nPlease enable it in Firebase Console > Authentication > Sign-in method > Email/Password."
        } else {
            msg
        }
    }

    fun handleAuth() {
        showErrors = true
        val ok = emailValid && passwordValid && (isLogin || (nameValid && phoneValid))
        if (!ok) return

        isLoading = true
        authError = null

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        if (isLogin) {
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        currentUserId = user.uid
                        currentUserEmail = user.email ?: email.trim()
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val fullName = doc.getString("fullName")
                                    if (!fullName.isNullOrBlank()) {
                                        currentUserName = fullName
                                    }
                                    val ph = doc.getString("phone")
                                    if (!ph.isNullOrBlank()) {
                                        currentUserPhone = ph
                                    }
                                }
                                FirestoreRepository.loadCurrentUserProfile()
                                isLoading = false
                                onAuthSuccess()
                            }
                            .addOnFailureListener {
                                FirestoreRepository.loadCurrentUserProfile()
                                isLoading = false
                                onAuthSuccess()
                            }
                    } else {
                        isLoading = false
                        onAuthSuccess()
                    }
                }
                .addOnFailureListener { exc ->
                    isLoading = false
                    authError = parseError(exc)
                }
        } else {
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        currentUserId = user.uid
                        currentUserName = name.trim()
                        currentUserEmail = email.trim()
                        currentUserPhone = phone.trim()

                        val userMap = mapOf(
                            "uid" to user.uid,
                            "fullName" to name.trim(),
                            "email" to email.trim(),
                            "phone" to phone.trim()
                        )
                        db.collection("users").document(user.uid).set(userMap)
                        FirestoreRepository.loadCurrentUserProfile()
                    }
                    isLoading = false
                    onAuthSuccess()
                }
                .addOnFailureListener { exc ->
                    isLoading = false
                    authError = parseError(exc)
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Service Manager",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isLogin) "Welcome back" else "Create your account",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(32.dp))

        if (authError != null) {
            Text(
                text = authError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (!isLogin) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && !nameValid,
                supportingText = { if (showErrors && !nameValid) Text("Name is required") },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && !phoneValid,
                supportingText = { if (showErrors && !phoneValid) Text("Phone number is required") },
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = showErrors && !emailValid,
            supportingText = { if (showErrors && !emailValid) Text("Enter a valid email") },
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = showErrors && !passwordValid,
            supportingText = { if (showErrors && !passwordValid) Text("At least 6 characters") },
        )
        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { handleAuth() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isLogin) "Log in" else "Sign up")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            isLogin = !isLogin
            showErrors = false
            authError = null
        }) {
            Text(
                if (isLogin) "Don't have an account? Sign up"
                else "Already have an account? Log in"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    ServicemanagerTheme {
        AuthScreen()
    }
}
