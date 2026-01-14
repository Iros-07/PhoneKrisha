// Updated: app/src/main/kotlin/com/example/phon_krisha/screens/favorites/FavoritesScreen.kt
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

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(favorites) { ad ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    onClick = { onAdClick(ad.id) }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            ad.title.ifBlank { "Без названия" },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Цена: ${ad.price ?: 0}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Город: ${ad.city.ifBlank { "Не указан" }}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}