package com.example.phon_krisha.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(currentUserId: Int) {
    var ads by remember { mutableStateOf(listOf<Ad>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAd by remember { mutableStateOf<Ad?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                ads = ApiClient.instance.getAds()
            } catch (e: Exception) {
                ads = emptyList()
            }
        }
    }

    if (selectedAd != null) {
        AdDetailScreen(ad = selectedAd!!, currentUserId = currentUserId, onClose = { selectedAd = null })
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск по недвижимости") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            LazyColumn {
                items(ads.filter {
                    it.city?.contains(searchQuery, ignoreCase = true) == true ||
                            it.complex?.contains(searchQuery, ignoreCase = true) == true
                }) { ad ->
                    AdCard(ad = ad, currentUserId = currentUserId, onClick = { selectedAd = ad })
                }
            }
        }
    }
}

@Composable
fun AdCard(ad: Ad, currentUserId: Int, onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray),  // ← Правильный вызов background
                contentAlignment = Alignment.Center
            ) {
                Text("Фото", color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${ad.rooms}-комн. квартира", style = MaterialTheme.typography.titleMedium)
                Text("${ad.price} ₸", color = MaterialTheme.colorScheme.primary)
                Text(ad.city ?: "")
                Text(ad.complex ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = {
                isFavorite = !isFavorite
                // Вызов API toggleFavorite
            }) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Избранное",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}