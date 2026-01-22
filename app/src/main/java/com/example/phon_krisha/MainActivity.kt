// MainActivity.kt
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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        AuthState.restore(this)


        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val loggedInUserId by AuthState.currentUserId

                Scaffold(
                    bottomBar = {
                        if (loggedInUserId != null || AuthState.isGuest) {
                            NavigationBar {
                                val items = listOf(
                                    Triple("home", Icons.Default.Home, "Главная"),
                                    Triple("messages", Icons.Default.Email, "Сообщения"),
                                    Triple("add", Icons.Default.AddCircle, "Добавить"),
                                    Triple("favorites", Icons.Default.Favorite, "Избранное"),
                                    Triple("profile", Icons.Default.Person, "Профиль")
                                )

                                items.forEach { (route, icon, label) ->
                                    NavigationBarItem(
                                        selected = currentRoute(navController) == route,
                                        onClick = {
                                            if (route == "messages" && AuthState.isGuest) {
                                                return@NavigationBarItem
                                            }
                                            navController.navigate(route) {
                                                popUpTo("home") { saveState = true }
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
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = if (loggedInUserId != null || AuthState.isGuest) "home" else "profile", modifier = Modifier.padding(innerPadding)) {
                        composable("home") { HomeScreen(loggedInUserId, { id -> navController.navigate("ad/$id") }, navController) }
                        composable("favorites") { FavoritesScreen(loggedInUserId ?: 0, { id -> navController.navigate("ad/$id") }, navController) }
                        composable("messages") { MessagesScreen(loggedInUserId ?: 0, { id -> navController.navigate("chat/$id") }, navController) }
                        composable("add") { AddOrEditAdScreen(loggedInUserId ?: 0, null, onBack = { navController.popBackStack() }, onFinish = { navController.navigate("home") }) }
                        composable("profile") { ProfileScreen(navController) }
                        composable("ad/{adId}", arguments = listOf(navArgument("adId") { type = NavType.IntType })) { entry ->
                            AdDetailScreen(entry.arguments?.getInt("adId") ?: 0, loggedInUserId ?: 0, { id -> navController.navigate("chat/$id") }, navController)
                        }
                        composable("edit_ad/{adId}", arguments = listOf(navArgument("adId") { type = NavType.IntType })) { entry ->
                            AddOrEditAdScreen(loggedInUserId ?: 0, entry.arguments?.getInt("adId"), onBack = { navController.popBackStack() }, onFinish = { navController.navigate("home") })
                        }
                        composable("chat/{toUserId}", arguments = listOf(navArgument("toUserId") { type = NavType.IntType })) { entry ->
                            ChatScreen(loggedInUserId ?: 0, entry.arguments?.getInt("toUserId") ?: 0, navController)
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