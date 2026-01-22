// FavoritesScreen.kt
package com.example.phon_krisha.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.RemoveFavoriteRequest
import com.example.phon_krisha.screens.home.AdCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    userId: Int,
    onAdClick: (Int) -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (!AuthState.isGuest) {
            try {
                favorites = ApiClient.api.getFavorites(userId) ?: emptyList()
            } catch (e: Exception) {
                favorites = emptyList()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (AuthState.isGuest) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                TextButton(onClick = { navController.navigate("profile") }) {
                    Text("Гости не могут использовать избранное. Войдите в аккаунт.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(favorites) { ad ->
                AdCard(
                    ad = ad,
                    isFavorite = true,
                    onClick = { onAdClick(ad.id) },
                    onFavoriteClick = {
                        scope.launch {
                            try {
                                ApiClient.api.removeFavorite(
                                    RemoveFavoriteRequest(userId, ad.id)
                                )
                                favorites = favorites.filter { it.id != ad.id }
                            } catch (e: Exception) {
                            }
                        }
                    },
                    showFavoriteButton = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}