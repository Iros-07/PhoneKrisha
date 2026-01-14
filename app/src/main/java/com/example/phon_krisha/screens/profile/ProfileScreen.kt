// Updated: app/src/main/kotlin/com/example/phon_krisha/screens/profile/ProfileScreen.kt
package com.example.phon_krisha.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val loggedInUserId by AuthState.currentUserId
    val userId = loggedInUserId

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var user by remember { mutableStateOf<User?>(null) }
    var myAds by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var isEditing by remember { mutableStateOf(false) }

    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var fioError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

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
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    if (loggedInUserId != null || AuthState.isGuest) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            if (userId == null && !AuthState.isGuest) {
                // ================== АВТОРИЗАЦИЯ ==================
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
                                        navController.navigate("home")
                                    }
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Войти")
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { isLoginMode = false }) { Text("Нет аккаунта? Зарегистрироваться") }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                AuthState.saveAsGuest(context)
                                navController.navigate("home")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Войти как гость")
                    }
                } else {
                    OutlinedTextField(
                        value = fio,
                        onValueChange = { fio = it },
                        label = { Text("ФИО") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
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
                                    val response = ApiClient.api.register(UserRegisterRequest(fio, phone, email, password))
                                    if (response.id != null) {
                                        AuthState.saveUserId(context, response.id)
                                        navController.navigate("home")
                                    }
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зарегистрироваться")
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { isLoginMode = true }) { Text("Уже есть аккаунт? Войти") }
                }
            } else {
                if (AuthState.isGuest) {
                    Text("Вы вошли как гость. Функции ограничены.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                AuthState.clearUserId(context)
                                navController.navigate("profile") { popUpTo(0) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Выйти из гостевого режима")
                    }
                } else {
                    user?.let {
                        Text("ФИО: ${it.fio}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        Text("Телефон: ${it.phone}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        Text("Email: ${it.email}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
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

                    if (isEditing) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value = fio, onValueChange = { fio = it }, label = { Text("ФИО") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Новый пароль (опционально") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        ApiClient.api.updateUser(
                                            userId!!,
                                            UpdateUserRequest(
                                                fio = fio,
                                                phone = phone,
                                                email = email,
                                                password = password.takeIf { it.isNotBlank() }
                                            )
                                        )
                                        isEditing = false
                                        user = ApiClient.api.getUser(userId)
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить")
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Text("Мои объявления", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                    LazyColumn {
                        items(myAds) { ad ->
                            Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(ad.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
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
                                                    } catch (e: Exception) {}
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
    }
}