//AdDetailScreen.kt
package com.example.phon_krisha.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.User
import com.example.phon_krisha.ui.components.MapPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdDetailScreen(
    adId: Int,
    currentUserId: Int,
    onStartChat: (Int) -> Unit,
    navController: NavController
) {
    var ad by remember { mutableStateOf<Ad?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adId) {
        try {
            ad = ApiClient.api.getAd(adId)
            ad?.let {
                user = ApiClient.api.getUser(it.user_id)
            }
        } catch (e: Exception) {
            error = "Ошибка загрузки объявления: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали объявления") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "Неизвестная ошибка",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            ad == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Объявление не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> {
                val a = ad!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    if (!a.photos.isNullOrEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(a.photos!!) { url ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Фото объявления",
                                        modifier = Modifier
                                            .width(320.dp)
                                            .height(280.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = a.title.ifBlank { "Без названия" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${a.price} ₸",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CharacteristicItem(Icons.Default.LocationOn, "Город", a.city.ifBlank { "Не указан" })
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CharacteristicItem(Icons.Default.Home, "Адрес", a.address ?: "Не указан")
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CharacteristicItem(Icons.Default.BedroomParent, "Комнат", a.rooms.toString())
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CharacteristicItem(Icons.Default.Stairs, "Этаж", "${a.floor} / ${a.floors_in_house}")
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CharacteristicItem(Icons.Default.CalendarToday, "Год постройки", a.year_built.toString())
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CharacteristicItem(Icons.Default.SquareFoot, "Площадь", "${a.area} м²")

                            if (!a.complex.isNullOrBlank()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                CharacteristicItem(Icons.Default.Apartment, "ЖК / Комплекс", a.complex!!)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Описание
                    if (!a.description.isNullOrBlank()) {
                        Text(
                            text = "Описание",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = a.description!!,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (a.lat != null && a.lon != null) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Расположение",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                MapPicker(
                                    modifier = Modifier.fillMaxSize(),
                                    initialLat = a.lat!!,
                                    initialLon = a.lon!!,
                                    readOnly = true,
                                    onLocationSelected = { _, _ -> }
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = a.address?.takeIf { it.isNotBlank() } ?: "${a.city}, точное местоположение",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Продавец",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user?.fio ?: "Неизвестно",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = user?.phone ?: "—",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!AuthState.isGuest && a.user_id != currentUserId) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onStartChat(a.user_id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Message,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Написать продавцу")
                                }
                            } else if (AuthState.isGuest) {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { navController.navigate("profile") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Войдите, чтобы писать сообщения")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacteristicItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}