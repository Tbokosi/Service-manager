package com.example.service_manager

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

object Routes {
    const val AUTH = "auth"
    const val EXPLORE = "explore"
    const val MY_REQUESTS = "my_requests"
    const val MY_SERVICES = "my_services"
    const val ACCOUNT = "account"
    const val SERVICE_DETAILS = "service_details"
    const val FILL_FORM = "fill_form"
    const val SUBMISSION_DETAILS = "submission_details"
    const val SERVICE_EDITOR = "service_editor"
    const val FORM_BUILDER = "form_builder"
    const val RECEIVED_REQUESTS = "received_requests"
}

data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.EXPLORE, "Explore", Icons.Default.Search),
    Tab(Routes.MY_REQUESTS, "My requests", Icons.AutoMirrored.Filled.List),
    Tab(Routes.MY_SERVICES, "My services", Icons.Default.Build),
    Tab(Routes.ACCOUNT, "Account", Icons.Default.Person),
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.AUTH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.AUTH) {
                PlaceholderScreen(
                    "Auth (log in / sign up)",
                    "Continue" to {
                        navController.navigate(Routes.EXPLORE) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.EXPLORE) {
                PlaceholderScreen(
                    "Browse services",
                    "Open a service" to { navController.navigate(Routes.SERVICE_DETAILS) }
                )
            }
            composable(Routes.SERVICE_DETAILS) {
                PlaceholderScreen(
                    "Service details",
                    "Request this service" to { navController.navigate(Routes.FILL_FORM) }
                )
            }
            composable(Routes.FILL_FORM) {
                PlaceholderScreen(
                    "Fill form",
                    "Submit" to { navController.popBackStack(Routes.EXPLORE, false) }
                )
            }
            composable(Routes.MY_REQUESTS) {
                PlaceholderScreen(
                    "My requests",
                    "Open a request" to { navController.navigate(Routes.SUBMISSION_DETAILS) }
                )
            }
            composable(Routes.MY_SERVICES) {
                PlaceholderScreen(
                    "My services",
                    "Create service" to { navController.navigate(Routes.SERVICE_EDITOR) },
                    "Received requests" to { navController.navigate(Routes.RECEIVED_REQUESTS) }
                )
            }
            composable(Routes.SERVICE_EDITOR) {
                PlaceholderScreen(
                    "Create / edit service",
                    "Edit form" to { navController.navigate(Routes.FORM_BUILDER) }
                )
            }
            composable(Routes.FORM_BUILDER) { PlaceholderScreen("Form builder") }
            composable(Routes.RECEIVED_REQUESTS) {
                PlaceholderScreen(
                    "Received requests",
                    "Open a request" to { navController.navigate(Routes.SUBMISSION_DETAILS) }
                )
            }
            composable(Routes.SUBMISSION_DETAILS) { PlaceholderScreen("Submission details") }
            composable(Routes.ACCOUNT) {
                PlaceholderScreen(
                    "Account",
                    "Log out" to {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, vararg actions: Pair<String, () -> Unit>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        actions.forEach { (label, onClick) ->
            Button(onClick = onClick) { Text(label) }
            Spacer(Modifier.height(8.dp))
        }
    }
}