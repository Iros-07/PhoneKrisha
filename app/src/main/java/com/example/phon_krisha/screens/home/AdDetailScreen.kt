// Updated: app/src/main/kotlin/com/example/phon_krisha/screens/home/AdDetailScreen.kt
package com.example.phon_krisha.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.User
import com.example.phon_krisha.apistate.AuthState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdDetailScreen(
    adId: Int,
    currentUserId: Int,
    onStartChat: (Int) -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var ad by remember { mutableStateOf<Ad?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adId) {
        try {
            ad = ApiClient.api.getAd(adId)
            user = ApiClient.api.getUser(ad!!.user_id)
        } catch (e: Exception) {
            error = "Ошибка загрузки объявления"
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(error ?: "", color = Color.Red, textAlign = TextAlign.Center)
            }

            ad == null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Объявление не найдено", textAlign = TextAlign.Center)
            }

            else -> {
                ad?.let { a ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Карусель фото (листаем горизонтально)
                        if (!a.photos.isNullOrEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(a.photos) { photoUrl ->
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Фото",
                                        modifier = Modifier
                                            .width(300.dp)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            a.title.ifBlank { "Без названия" },
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "${a.price} ₸",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Город: ${a.city.ifBlank { "Не указан" }} • Комнат: ${a.rooms}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Этаж: ${a.floor} • Этажность: ${a.floors_in_house}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            a.description.ifBlank { "Описание отсутствует" },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Год постройки: ${a.year_built}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Площадь: ${a.area} м²",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        a.complex?.let { Text("Комплекс: $it", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start) }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Продавец: ${user?.fio ?: "Неизвестно"} (${user?.phone ?: "Не указан"})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        if (!AuthState.isGuest && a.user_id != currentUserId) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { onStartChat(a.user_id) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Написать продавцу")
                            }
                        } else if (AuthState.isGuest) {
                            Spacer(Modifier.height(16.dp))
                            TextButton(onClick = { navController.navigate("profile") }) {
                                Text("Войдите, чтобы писать сообщения", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}