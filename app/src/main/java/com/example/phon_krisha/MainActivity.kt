package com.example.phon_krisha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.phon_krisha.messages.ChatScreen
import com.example.phon_krisha.messages.MessagesScreen
import com.example.phon_krisha.screens.add.AddOrEditAdScreen
import com.example.phon_krisha.screens.favorites.FavoritesScreen
import com.example.phon_krisha.screens.home.AdDetailScreen
import com.example.phon_krisha.screens.home.HomeScreen
import com.example.phon_krisha.screens.profile.ProfileScreen
import com.example.phon_krisha.apistate.AuthState

// Константы маршрутов
const val HOME = "home"
const val MESSAGES = "messages"
const val ADD = "add"
const val FAVORITES = "favorites"
const val PROFILE = "profile"
const val AD_DETAIL = "ad/{adId}"
const val EDIT_AD = "edit_ad/{adId}"
const val CHAT = "chat/{toUserId}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthState.restore(this)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val loggedInUserId by AuthState.currentUserId

                // Если пользователь не авторизован — показываем экран профиля/входа
                if (loggedInUserId == null) {
                    ProfileScreen(navController = navController)
                } else {
                    // Пользователь авторизован → сразу главная страница + нижняя панель
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                val items = listOf(
                                    Triple(HOME, Icons.Default.Home, "Главная"),
                                    Triple(MESSAGES, Icons.Default.Email, "Сообщения"),
                                    Triple(ADD, Icons.Default.AddCircle, "Добавить"),
                                    Triple(FAVORITES, Icons.Default.Favorite, "Избранное"),
                                    Triple(PROFILE, Icons.Default.Person, "Профиль")
                                )

                                items.forEach { (route, icon, label) ->
                                    NavigationBarItem(
                                        selected = currentRoute(navController) == route,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(HOME) {
                                                    inclusive = false
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = HOME,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(HOME) {
                                HomeScreen(
                                    loggedInUserId = loggedInUserId!!,
                                    onAdClick = { adId -> navController.navigate("ad/$adId") }
                                )
                            }

                            composable(FAVORITES) {
                                FavoritesScreen(
                                    userId = loggedInUserId!!,
                                    onAdClick = { adId -> navController.navigate("ad/$adId") }
                                )
                            }

                            composable(MESSAGES) {
                                MessagesScreen(
                                    currentUserId = loggedInUserId!!,
                                    onChatClick = { toUserId ->
                                        navController.navigate("chat/$toUserId")
                                    }
                                )
                            }

                            composable(ADD) {
                                AddOrEditAdScreen(
                                    userId = loggedInUserId!!,           // ← добавили
                                    adId = null,
                                    onFinish = {
                                        navController.navigate(HOME) {
                                            popUpTo(HOME) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(PROFILE) {
                                ProfileScreen(navController = navController)
                            }

                            composable(
                                route = AD_DETAIL,
                                arguments = listOf(navArgument("adId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val adId = backStackEntry.arguments?.getInt("adId") ?: 0
                                AdDetailScreen(
                                    adId = adId,
                                    currentUserId = loggedInUserId!!,
                                    onStartChat = { sellerId ->
                                        navController.navigate("chat/$sellerId")
                                    }
                                )
                            }

                            composable(
                                route = EDIT_AD,
                                arguments = listOf(navArgument("adId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val adId = backStackEntry.arguments?.getInt("adId") ?: 0
                                AddOrEditAdScreen(
                                    userId = loggedInUserId!!,           // ← добавили здесь тоже
                                    adId = adId,
                                    onFinish = {
                                        navController.navigate(HOME) {
                                            popUpTo(HOME) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(
                                route = CHAT,
                                arguments = listOf(navArgument("toUserId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val toUserId = backStackEntry.arguments?.getInt("toUserId") ?: 0
                                ChatScreen(
                                    currentUserId = loggedInUserId!!,    // ← добавили
                                    toUserId = toUserId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}