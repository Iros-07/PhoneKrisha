// File: app/src/main/kotlin/com/example/phon_krisha/screens/add/AddOrEditAdScreen.kt
package com.example.phon_krisha.screens.add

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun AddOrEditAdScreen(
    userId: Int,
    adId: Int? = null,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(adId != null) }
    var ad by remember { mutableStateOf<Ad?>(null) }

    // Поля формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var adType by remember { mutableStateOf("") }
    var houseType by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var floorsInHouse by remember { mutableStateOf("") }
    var yearBuilt by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var complex by remember { mutableStateOf("") }

    // Загрузка существующего объявления
    LaunchedEffect(adId) {
        if (adId != null) {
            try {
                val loaded = ApiClient.api.getAd(adId)
                ad = loaded
                title = loaded.title.ifBlank { "" }
                description = loaded.description.ifBlank { "" }
                rooms = loaded.rooms?.toString() ?: ""
                city = loaded.city.ifBlank { "" }
                photo = loaded.photo ?: ""
                price = loaded.price?.toString() ?: ""
                adType = loaded.ad_type.ifBlank { "" }
                houseType = loaded.house_type.ifBlank { "" }
                floor = loaded.floor?.toString() ?: ""
                floorsInHouse = loaded.floors_in_house?.toString() ?: ""
                yearBuilt = loaded.year_built?.toString() ?: ""
                area = loaded.area?.toString() ?: ""
                complex = loaded.complex ?: ""
            } catch (e: Exception) {
                Log.e("AddEditAd", "Ошибка загрузки объявления", e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // UI формы
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = rooms,
            onValueChange = { rooms = it },
            label = { Text("Комнат") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = photo,
            onValueChange = { photo = it },
            label = { Text("Ссылка на фото") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Цена") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = adType,
            onValueChange = { adType = it },
            label = { Text("Тип (продажа/аренда)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = houseType,
            onValueChange = { houseType = it },
            label = { Text("Тип дома") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = floor,
            onValueChange = { floor = it },
            label = { Text("Этаж") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = floorsInHouse,
            onValueChange = { floorsInHouse = it },
            label = { Text("Этажность") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = yearBuilt,
            onValueChange = { yearBuilt = it },
            label = { Text("Год постройки") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Площадь") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = complex,
            onValueChange = { complex = it },
            label = { Text("ЖК / Комплекс") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val newAd = Ad(
                            id = adId ?: 0,
                            user_id = userId,
                            title = title.ifBlank { "Без названия" },
                            description = description.ifBlank { "Описание отсутствует" },
                            rooms = rooms.toIntOrNull() ?: 0,
                            city = city.ifBlank { "Не указан" },
                            photo = photo.ifBlank { null },
                            price = price.toLongOrNull() ?: 0L,
                            ad_type = adType.ifBlank { "продажа" },
                            house_type = houseType.ifBlank { "Не указан" },
                            floor = floor.toIntOrNull() ?: 0,
                            floors_in_house = floorsInHouse.toIntOrNull() ?: 0,
                            year_built = yearBuilt.toIntOrNull() ?: 0,
                            area = area.toDoubleOrNull() ?: 0.0,
                            complex = complex.ifBlank { null }
                        )

                        if (adId == null) {
                            ApiClient.api.addAd(newAd)
                        } else {
                            ApiClient.api.updateAd(adId, newAd)
                        }
                        onFinish()
                    } catch (e: Exception) {
                        Log.e("AddAd", "Ошибка публикации", e)
                        // TODO: Можно показать Snackbar с ошибкой
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (adId == null) "Опубликовать" else "Сохранить изменения")
        }
    }
}
