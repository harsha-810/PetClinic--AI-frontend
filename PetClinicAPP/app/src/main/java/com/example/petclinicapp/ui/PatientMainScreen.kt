package com.example.petclinicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.petclinicapp.ui.theme.OceanBlue
import com.example.petclinicapp.ui.theme.ClinicTeal
import com.example.petclinicapp.ui.theme.BackgroundWhite
import com.example.petclinicapp.navigation.*

sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Appointments : BottomNavItem("appointments", Icons.Default.DateRange, "Schedule")
    object Records : BottomNavItem("records", Icons.AutoMirrored.Filled.List, "Records")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Account")
}

@Composable
fun PatientMainScreen(
    rootNavController: NavController,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val innerNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Appointments,
        BottomNavItem.Records,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute != "notifications") {
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.97f),
                    tonalElevation = 0.dp
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp)) },
                            label = { Text(item.title, style = MaterialTheme.typography.labelMedium) },
                            selected = currentRoute == item.route,
                            onClick = {
                                innerNavController.navigate(item.route) {
                                    innerNavController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ClinicTeal,
                                selectedTextColor = ClinicTeal,
                                unselectedIconColor = Color.LightGray,
                                unselectedTextColor = Color.LightGray,
                                indicatorColor = ClinicTeal.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                PatientDashboardScreen(
                    onLogout = onLogout,
                    onChangePassword = { rootNavController.navigate(ChangePassword) },
                    onProfileClick = { innerNavController.navigate(BottomNavItem.Profile.route) },
                    onAddPetClick = { rootNavController.navigate(AddPet) },
                    onBookAppointmentClick = { petId -> rootNavController.navigate(BookAppointment(petId = petId)) },
                    onPetHistoryClick = { petId -> rootNavController.navigate(PetHistory(petId = petId, isDoctor = false)) },
                    onNotificationsClick = { innerNavController.navigate("notifications") },
                    onSettingsClick = onSettingsClick,
                    onUpcomingAppointmentClick = { petId ->
                        rootNavController.navigate(FilteredAppointments(petId = petId, onlyUpcoming = true))
                    },
                    onAiAnalysisClick = {
                        rootNavController.navigate(AiAnalysis)
                    }
                )
            }
            composable(BottomNavItem.Appointments.route) {
                PatientAppointmentsScreen(
                    onBack = { innerNavController.popBackStack() }
                )
            }

            composable(BottomNavItem.Records.route) {
                PatientRecordsScreen()
            }
            composable(BottomNavItem.Profile.route) {
                PatientProfileScreen(
                    onSettingsClick = onSettingsClick
                )
            }
            composable("notifications") {
                PatientNotificationsScreen(onBack = { innerNavController.popBackStack() })
            }
        }
    }
}
