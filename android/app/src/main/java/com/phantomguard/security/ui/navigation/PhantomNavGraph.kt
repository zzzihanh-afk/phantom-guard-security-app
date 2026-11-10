package com.phantomguard.security.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phantomguard.security.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Devices : Screen("devices")
    object RemoteControl : Screen("remote_control/{deviceId}") {
        fun createRoute(deviceId: String) = "remote_control/$deviceId"
    }
    object Location : Screen("location")
    object ProtectionSettings : Screen("protection_settings")
    object Alerts : Screen("alerts")
    object RecoveryCenter : Screen("recovery_center")
    object GlobalAdmin : Screen("global_admin")
    object Compatibility : Screen("compatibility")
    object Settings : Screen("settings")
    object AddDevice : Screen("add_device")
    object DatabaseConfig : Screen("database_config")
}

@Composable
fun PhantomNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Dashboard.route)
                },
                onNavigateToForgotPassword = {}
            )
        }

        composable(Screen.Dashboard.route) {
            MainDashboardScreen(
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToRemoteControl = { deviceId ->
                    navController.navigate(Screen.RemoteControl.createRoute(deviceId))
                },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToGlobalAdmin = { navController.navigate(Screen.GlobalAdmin.route) },
                onNavigateToCompatibility = { navController.navigate(Screen.Compatibility.route) }
            )
        }

        composable(Screen.Devices.route) {
            DevicesListScreen(
                onBackClick = { navController.popBackStack() },
                onDeviceClick = { deviceId ->
                    navController.navigate(Screen.RemoteControl.createRoute(deviceId))
                },
                onAddNewDevice = { navController.navigate(Screen.AddDevice.route) }
            )
        }

        composable(
            route = Screen.RemoteControl.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            RemoteControlScreen(
                deviceId = deviceId,
                onBackClick = { navController.popBackStack() },
                onNavigateToLocation = { navController.navigate(Screen.Location.route) },
                onNavigateToRecovery = { navController.navigate(Screen.RecoveryCenter.route) }
            )
        }

        composable(Screen.Location.route) {
            LiveLocationScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHistory = {}
            )
        }

        composable(Screen.ProtectionSettings.route) {
            ProtectionSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onManageSettings = { navController.navigate(Screen.Compatibility.route) }
            )
        }

        composable(Screen.Alerts.route) {
            SecurityAlertsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.RecoveryCenter.route) {
            RecoveryCenterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.GlobalAdmin.route) {
            GlobalAdminScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCompatibility = { navController.navigate(Screen.Compatibility.route) }
            )
        }

        composable(Screen.Compatibility.route) {
            CompatibilityReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToProtectionSettings = { navController.navigate(Screen.ProtectionSettings.route) },
                onNavigateToCompatibility = { navController.navigate(Screen.Compatibility.route) },
                onNavigateToGlobalAdmin = { navController.navigate(Screen.GlobalAdmin.route) },
                onNavigateToDatabaseConfig = { navController.navigate(Screen.DatabaseConfig.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DatabaseConfig.route) {
            DatabaseConfigScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddDevice.route) {
            AddDeviceScreen(
                onBackClick = { navController.popBackStack() },
                onDeviceEnrolled = { navController.popBackStack() }
            )
        }
    }
}
