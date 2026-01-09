package com.example.phon_krisha.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdScreen(currentUserId: Int) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var rooms by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }  // Можно потом заменить на загрузку фото
    var price by remember { mutableStateOf("") }
    var adType by remember { mutableStateOf("apartment") }  // apartment или house
    var houseType by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var floorsInHouse by remember { mutableStateOf("") }
    var yearBuilt by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var complex by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Новое объявление", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = rooms,
            onValueChange = { rooms = it },
            label = { Text("Количество комнат") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = photo,
            onValueChange = { photo = it },
            label = { Text("Ссылка на фото (URL)") },
            placeholder = { Text("https://example.com/photo.jpg") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
            label = { Text("Цена (в тенге)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (adType == "apartment") "Квартира" else "Дом",
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип недвижимости") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Квартира") }, onClick = {
                    adType = "apartment"
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Дом") }, onClick = {
                    adType = "house"
                    expanded = false
                })
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = houseType,
            onValueChange = { houseType = it },
            label = { Text("Тип дома (панельный, кирпичный и т.д.)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = floor,
            onValueChange = { if (it.all { char -> char.isDigit() }) floor = it },
            label = { Text("Этаж") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = floorsInHouse,
            onValueChange = { if (it.all { char -> char.isDigit() }) floorsInHouse = it },
            label = { Text("Этажей в доме") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = yearBuilt,
            onValueChange = { if (it.all { char -> char.isDigit() }) yearBuilt = it },
            label = { Text("Год постройки") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Общая площадь (м²)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = complex,
            onValueChange = { complex = it },
            label = { Text("Жилой комплекс") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (success) {
            Text("Объявление успешно добавлено!", color = Color.Green)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    val newAd = Ad(
                        user_id = currentUserId,
                        rooms = rooms.toIntOrNull(),
                        city = city.ifBlank { null },
                        photo = photo.ifBlank { null },
                        price = price.toLongOrNull(),
                        ad_type = adType,
                        house_type = houseType.ifBlank { null },
                        floor = floor.toIntOrNull(),
                        floors_in_house = floorsInHouse.toIntOrNull(),
                        year_built = yearBuilt.toIntOrNull(),
                        area = area.toFloatOrNull(),
                        complex = complex.ifBlank { null }
                    )

                    try {
                        ApiClient.instance.addAd(newAd)
                        success = true
                        error = ""
                        // Очистить поля после успеха
                        rooms = ""
                        city = ""
                        photo = ""
                        price = ""
                        houseType = ""
                        floor = ""
                        floorsInHouse = ""
                        yearBuilt = ""
                        area = ""
                        complex = ""
                    } catch (e: Exception) {
                        error = "Ошибка: ${e.message}"
                        success = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Опубликовать объявление")
        }
    }
}