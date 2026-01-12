package com.example.phon_krisha.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun AdDetailScreen(
    adId: Int,
    currentUserId: Int,
    onStartChat: (Int) -> Unit
) {
    var ad by remember { mutableStateOf<Ad?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adId) {
        try {
            ad = ApiClient.api.getAd(adId)
        } catch (e: Exception) {
            error = "Ошибка загрузки объявления"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Color.Red)
        }

        ad == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Объявление не найдено")
        }

        else -> {
            ad?.let { a ->
                Column(modifier = Modifier.padding(16.dp)) {
                    a.photo?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Фото",
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(a.title?.ifBlank { "Без названия" } ?: "Без названия",
                        style = MaterialTheme.typography.headlineMedium)
                    Text("${a.price ?: 0} ₸", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Город: ${a.city?.ifBlank { "Не указан" } ?: "Не указан"} • Комнат: ${a.rooms ?: 0}")
                    Text("Этаж: ${a.floor ?: 0} • Этажность: ${a.floors_in_house ?: 0}")
                    Spacer(Modifier.height(8.dp))
                    Text(a.description?.ifBlank { "Описание отсутствует" } ?: "Описание отсутствует")
                    Spacer(Modifier.height(8.dp))
                    Text("Год постройки: ${a.year_built ?: 0}")
                    Text("Площадь: ${a.area ?: 0.0} м²")
                    a.complex?.let { Text("Комплекс: $it") }

                    if (a.user_id != currentUserId) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onStartChat(a.user_id) }) {
                            Text("Написать продавцу")
                        }
                    }
                }
            }
        }
    }
}
