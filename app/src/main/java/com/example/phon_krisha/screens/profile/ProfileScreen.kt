package com.example.phon_krisha.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController) {

    // ✅ Правильно получаем ID
    val loggedInUserId by AuthState.currentUserId
    val userId = loggedInUserId  // ← локальная копия для smart cast

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var user by remember { mutableStateOf<User?>(null) }
    var myAds by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var isEditing by remember { mutableStateOf(false) }

    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoginMode by remember { mutableStateOf(true) }

    // 🔄 Загружаем профиль при логине
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                user = ApiClient.api.getUser(userId)
                fio = user?.fio ?: ""
                phone = user?.phone ?: ""
                email = user?.email ?: ""
                myAds = ApiClient.api.getAds().filter { it.user_id == userId }
            } catch (_: Exception) {}
        }
    }

    if (userId == null) {
        // ================== АВТОРИЗАЦИЯ ==================
        Column(modifier = Modifier.padding(24.dp)) {
            if (isLoginMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = ApiClient.api.login(LoginRequest(email, password))
                                if (response.id != null) {
                                    AuthState.saveUserId(context, response.id)
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Войти")
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { isLoginMode = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Нет аккаунта? Зарегистрироваться")
                }
            } else {
                // ================== РЕГИСТРАЦИЯ ==================
                OutlinedTextField(value = fio, onValueChange = { fio = it }, label = { Text("ФИО") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = ApiClient.api.register(UserRegisterRequest(fio, phone, email, password))
                                if (response.id != null) {
                                    AuthState.saveUserId(context, response.id)
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Зарегистрироваться")
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { isLoginMode = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Уже есть аккаунт? Войти")
                }
            }
        }
    } else {
        // ================== ПРОФИЛЬ ==================
        Column(modifier = Modifier.padding(24.dp)) {
            if (isEditing) {
                OutlinedTextField(value = fio, onValueChange = { fio = it }, label = { Text("ФИО") })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Телефон") })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Новый пароль (если нужно)") })
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ApiClient.api.updateUser(
                                    userId,
                                    UpdateUserRequest(
                                        fio = fio,
                                        phone = phone,
                                        email = email,
                                        password = password.takeIf { it.isNotBlank() }
                                    )
                                )
                                isEditing = false
                                user = ApiClient.api.getUser(userId)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            } else {
                user?.let {
                    Text("ФИО: ${it.fio}", style = MaterialTheme.typography.titleMedium)
                    Text("Телефон: ${it.phone}")
                    Text("Email: ${it.email}")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { isEditing = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Редактировать профиль")
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                AuthState.clearUserId(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Выйти")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Мои объявления", style = MaterialTheme.typography.titleLarge)

            LazyColumn {
                items(myAds) { ad ->
                    Card(modifier = Modifier.padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ad.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Row {
                                Button(onClick = { navController.navigate("edit_ad/${ad.id}") }) {
                                    Text("Изменить")
                                }
                                Spacer(Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                ApiClient.api.deleteAd(ad.id)
                                                myAds = myAds.filter { it.id != ad.id }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                ) {
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
