// Updated: app/src/main/kotlin/com/example/phon_krisha/screens/add/AddOrEditAdScreen.kt

package com.example.phon_krisha.screens.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditAdScreen(
    userId: Int,
    adId: Int? = null,
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(adId != null) }
    var ad by remember { mutableStateOf<Ad?>(null) }

    // Поля формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<MutableList<Uri>>(mutableListOf()) } // Список выбранных Uri
    var photoUrls by remember { mutableStateOf<MutableList<String>>(mutableListOf()) } // Список загруженных URLs
    var price by remember { mutableStateOf("") }
    var adType by remember { mutableStateOf("продажа") }
    var houseType by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var floorsInHouse by remember { mutableStateOf("") }
    var yearBuilt by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var complex by remember { mutableStateOf("") }

    // Ошибки валидации
    var titleError by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    // Загрузка существующего объявления при редактировании
    LaunchedEffect(adId) {
        if (adId != null) {
            try {
                val loadedAd = ApiClient.api.getAd(adId)
                ad = loadedAd
                title = loadedAd.title
                description = loadedAd.description ?: ""
                rooms = loadedAd.rooms.toString()
                city = loadedAd.city
                photoUrls = loadedAd.photos?.toMutableList() ?: mutableListOf() // Загружаем существующие фото
                price = loadedAd.price.toString()
                adType = loadedAd.ad_type
                houseType = loadedAd.house_type
                floor = loadedAd.floor.toString()
                floorsInHouse = loadedAd.floors_in_house.toString()
                yearBuilt = loadedAd.year_built.toString()
                area = loadedAd.area.toString()
                complex = loadedAd.complex ?: ""
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Ошибка загрузки объявления: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Выбор нескольких фото из галереи
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        photoUris.addAll(uris)
        scope.launch {
            uris.forEach { uri ->
                try {
                    val file = File(context.cacheDir, "ad_photo_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                    val response = ApiClient.api.uploadPhoto(body)
                    val url = response["url"]
                    if (url != null) {
                        photoUrls.add(url)
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Ошибка загрузки фото: ${e.message}")
                }
            }
            photoUris.clear() // Очищаем после загрузки
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (adId != null) "Редактировать объявление" else "Добавить объявление") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        titleError = title.isBlank()
                        cityError = city.isBlank()
                        priceError = price.isBlank()

                        if (titleError || cityError || priceError) return@IconButton

                        scope.launch {
                            val newAd = Ad(
                                id = adId ?: 0,
                                title = title,
                                description = description,
                                user_id = userId,
                                rooms = rooms.toIntOrNull() ?: 0,
                                city = city,
                                photos = photoUrls, // Список URLs
                                price = price.toLongOrNull() ?: 0,
                                ad_type = adType,
                                house_type = houseType,
                                floor = floor.toIntOrNull() ?: 0,
                                floors_in_house = floorsInHouse.toIntOrNull() ?: 0,
                                year_built = yearBuilt.toIntOrNull() ?: 0,
                                area = area.toDoubleOrNull() ?: 0.0,
                                complex = complex
                            )
                            try {
                                if (adId != null) {
                                    ApiClient.api.updateAd(adId, newAd)
                                    snackbarHostState.showSnackbar("Объявление обновлено")
                                } else {
                                    ApiClient.api.addAd(newAd)
                                    snackbarHostState.showSnackbar("Объявление добавлено")
                                }
                                onFinish()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Показ существующих фото с возможностью удаления
            if (photoUrls.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photoUrls) { url ->
                        Box {
                            AsyncImage(
                                model = url,
                                contentDescription = "Фото",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { photoUrls.remove(url) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить фото")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Основные поля
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок *") },
                isError = titleError,
                supportingText = { if (titleError) Text("Обязательное поле") else null },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() } },
                label = { Text("Цена *") },
                isError = priceError,
                supportingText = { if (priceError) Text("Обязательное поле") else null },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₸ ") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Город *") },
                isError = cityError,
                supportingText = { if (cityError) Text("Обязательное поле") else null },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rooms,
                onValueChange = { rooms = it.filter { char -> char.isDigit() } },
                label = { Text("Количество комнат") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Дополнительные поля
            OutlinedTextField(
                value = adType,
                onValueChange = { adType = it },
                label = { Text("Тип объявления (продажа/аренда)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = houseType,
                onValueChange = { houseType = it },
                label = { Text("Тип жилья") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it.filter { char -> char.isDigit() } },
                    label = { Text("Этаж") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = floorsInHouse,
                    onValueChange = { floorsInHouse = it.filter { char -> char.isDigit() } },
                    label = { Text("Этажность") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yearBuilt,
                    onValueChange = { yearBuilt = it.filter { char -> char.isDigit() } },
                    label = { Text("Год постройки") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Площадь, м²") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = complex,
                onValueChange = { complex = it },
                label = { Text("Название ЖК / Комплекс") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}