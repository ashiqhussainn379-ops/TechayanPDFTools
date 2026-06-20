package com.techayan.pdftools.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.techayan.pdftools.ui.about.AboutScreen
import com.techayan.pdftools.ui.dashboard.DashboardScreen
import com.techayan.pdftools.ui.dashboard.DashboardToolAction
import com.techayan.pdftools.ui.dashboard.DashboardViewModel
import com.techayan.pdftools.ui.imagetopdf.ImageToPdfScreen
import com.techayan.pdftools.ui.imagetopdf.ImageToPdfViewModel
import com.techayan.pdftools.ui.legal.PrivacyPolicyScreen
import com.techayan.pdftools.ui.legal.TermsConditionsScreen
import com.techayan.pdftools.ui.recent.RecentFilesScreen
import com.techayan.pdftools.ui.settings.SettingsScreen
import com.techayan.pdftools.ui.settings.SettingsViewModel
import com.techayan.pdftools.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechayanNavHost(
    isDarkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Splash.route
    val currentDestination = AppDestination.fromRoute(currentRoute)
    val showTopBar = currentDestination != AppDestination.Splash
    val showBottomBar = AppDestination.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = { Text(text = currentDestination.title) },
                    navigationIcon = {
                        if (!showBottomBar) {
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text(text = "Back")
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    AppDestination.bottomNavItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AppDestination.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                destination.iconResId?.let { iconResId ->
                                    Icon(
                                        painter = painterResource(id = iconResId),
                                        contentDescription = destination.title
                                    )
                                }
                            },
                            label = { Text(text = destination.title) }
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(AppDestination.Splash.route) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Splash.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(AppDestination.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onToolSelected = { tool ->
                        when (tool.action) {
                            DashboardToolAction.ImageToPdf -> {
                                navController.navigate(AppDestination.ImageToPdf.route)
                            }

                            DashboardToolAction.RecentFiles -> {
                                navController.navigate(AppDestination.RecentFiles.route)
                            }

                            DashboardToolAction.Settings -> {
                                navController.navigate(AppDestination.Settings.route)
                            }

                            DashboardToolAction.About -> {
                                navController.navigate(AppDestination.About.route)
                            }

                            else -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${tool.title} UI is ready; processing logic is not part of this step."
                                    )
                                }
                            }
                        }
                    }
                )
            }

            composable(AppDestination.ImageToPdf.route) {
                val viewModel: ImageToPdfViewModel = viewModel()
                ImageToPdfScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }

            composable(AppDestination.RecentFiles.route) {
                RecentFilesScreen()
            }

            composable(AppDestination.Settings.route) {
                val viewModel: SettingsViewModel = viewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onDarkModeChanged = onDarkModeChanged,
                    onPrivacyPolicyClick = {
                        navController.navigate(AppDestination.PrivacyPolicy.route)
                    },
                    onTermsConditionsClick = {
                        navController.navigate(AppDestination.TermsConditions.route)
                    }
                )
            }

            composable(AppDestination.About.route) {
                AboutScreen(
                    onPrivacyPolicyClick = {
                        navController.navigate(AppDestination.PrivacyPolicy.route)
                    },
                    onTermsConditionsClick = {
                        navController.navigate(AppDestination.TermsConditions.route)
                    }
                )
            }

            composable(AppDestination.PrivacyPolicy.route) {
                PrivacyPolicyScreen()
            }

            composable(AppDestination.TermsConditions.route) {
                TermsConditionsScreen()
            }
        }
    }
}
