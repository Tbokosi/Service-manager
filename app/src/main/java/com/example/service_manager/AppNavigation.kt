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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument


object Routes {
    const val AUTH = "auth"
    const val EXPLORE = "explore"
    const val MY_REQUESTS = "my_requests"
    const val MY_SERVICES = "my_services"
    const val ACCOUNT = "account"
    const val SERVICE_DETAILS = "service_details/{serviceId}"
    fun serviceDetails(id: Int) = "service_details/$id"
    const val FILL_FORM = "fill_form/{serviceId}"
    fun fillForm(id: Int) = "fill_form/$id"
    const val SUBMISSION_DETAILS = "submission_details/{submissionId}?asProvider={asProvider}"
    fun submissionDetails(id: Int, asProvider: Boolean = false) =
        "submission_details/$id?asProvider=$asProvider"
    const val SERVICE_EDITOR = "service_editor?serviceId={serviceId}"
    fun serviceEditor(id: Int = -1) = "service_editor?serviceId=$id"
    const val FORM_BUILDER = "form_builder/{serviceId}"
    fun formBuilder(id: Int) = "form_builder/$id"
    const val RECEIVED_REQUESTS = "received_requests"
}

data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.EXPLORE, "Explore", Icons.Default.Search),
    TabItem(Routes.MY_REQUESTS, "My requests", Icons.AutoMirrored.Filled.List),
    TabItem(Routes.MY_SERVICES, "My services", Icons.Default.Build),
    TabItem(Routes.ACCOUNT, "Account", Icons.Default.Person),
)

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }
    val goBack: () -> Unit = { navController.popBackStack() }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
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
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {

            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Routes.EXPLORE) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.EXPLORE) {
                ExploreScreen(
                    onServiceClick = { service ->
                        navController.navigate(Routes.serviceDetails(service.id))
                    }
                )
            }
            // ---------- Placeholders (replace one by one) ----------
            composable(
                route = Routes.SERVICE_DETAILS,
                arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
            ) { entry ->
                val serviceId = entry.arguments?.getInt("serviceId") ?: -1
                ServiceDetailsScreen(
                    serviceId = serviceId,
                    onBack = goBack,
                    onRequest = { navController.navigate(Routes.fillForm(serviceId)) }
                )
            }
            composable(route = Routes.FILL_FORM,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
            ) {
                entry ->
            val serviceId = entry.arguments?.getInt("serviceId") ?: -1
            FillFormScreen(
                serviceId = serviceId,
                onBack = goBack,
                onDone = { navController.popBackStack(Routes.EXPLORE, false) }
            )
        }
            composable(Routes.MY_REQUESTS) {
                MyRequestsScreen(
                    onRequestClick = { id -> navController.navigate(Routes.submissionDetails(id)) }
                )
            }
            composable(Routes.MY_SERVICES) {
                MyServicesScreen(
                    onCreateService = { navController.navigate(Routes.serviceEditor()) },
                    onOpenRequests = { navController.navigate(Routes.RECEIVED_REQUESTS) },
                    onEditService = { id -> navController.navigate(Routes.serviceEditor(id)) }
                )
            }
            composable(
                route = Routes.SERVICE_EDITOR,
                arguments = listOf(
                    navArgument("serviceId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { entry ->
                val serviceId = entry.arguments?.getInt("serviceId") ?: -1
                ServiceEditorScreen(
                    serviceId = serviceId,
                    onBack = goBack,
                    onSaved = goBack,
                    onEditForm = { navController.navigate(Routes.formBuilder(serviceId)) }
                )
            }
            composable(
                route = Routes.FORM_BUILDER,
                arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
            ) { entry ->
                FormBuilderScreen(
                    serviceId = entry.arguments?.getInt("serviceId") ?: -1,
                    onBack = goBack
                )
            }
            composable(Routes.RECEIVED_REQUESTS) {
                ReceivedRequestsScreen(
                    onBack = goBack,
                    onRequestClick = { id ->
                        navController.navigate(Routes.submissionDetails(id, asProvider = true))
                    }
                )
            }
            composable(
                route = Routes.SUBMISSION_DETAILS,
                arguments = listOf(
                    navArgument("submissionId") { type = NavType.IntType },
                    navArgument("asProvider") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { entry ->
                SubmissionDetailsScreen(
                    submissionId = entry.arguments?.getInt("submissionId") ?: -1,
                    asProvider = entry.arguments?.getBoolean("asProvider") ?: false,
                    onBack = goBack
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    onLogout = {
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
fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList()
) {
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
        if (onBack != null) {
            TextButton(onClick = onBack) { Text("Back") }
        }
    }
}