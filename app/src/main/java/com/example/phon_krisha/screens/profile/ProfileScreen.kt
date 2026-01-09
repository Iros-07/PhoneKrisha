package com.example.phon_krisha.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.currentUserId
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.UserRegisterRequest
import com.example.phon_krisha.network.LoginRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isRegister by remember { mutableStateOf(true) }  // true = регистрация, false = вход
    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegister) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        if (isRegister) {
            OutlinedTextField(
                value = fio,
                onValueChange = { fio = it },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '+' || char == ' ' || char == '(' || char == ')' || char == '-' }) phone = it },
                label = { Text("Телефон") },
                placeholder = { Text("+7 (XXX) XXX-XX-XX") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (success.isNotEmpty()) {
            Text(success, color = Color.Green)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            if (isRegister) {
                                if (fio.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                                    error = "Заполните все поля"
                                    success = ""
                                    return@withContext
                                }
                                val response = ApiClient.instance.register(
                                    UserRegisterRequest(fio, phone, email, password)
                                )
                                if (response.id != null) {
                                    currentUserId.value = response.id
                                    success = "Регистрация успешна! ID: ${response.id}"
                                    error = ""
                                } else {
                                    error = response.error ?: "Ошибка регистрации"
                                    success = ""
                                }
                            } else {
                                if (email.isBlank() || password.isBlank()) {
                                    error = "Введите email и пароль"
                                    success = ""
                                    return@withContext
                                }
                                val response = ApiClient.instance.login(
                                    LoginRequest(email, password)
                                )
                                if (response.id != null) {
                                    currentUserId.value = response.id
                                    success = "Вход успешный! ID: ${response.id}"
                                    error = ""
                                } else {
                                    error = response.error ?: "Неверный email или пароль"
                                    success = ""
                                }
                            }
                        } catch (e: Exception) {
                            error = "Нет соединения с сервером"
                            success = ""
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegister) "Зарегистрироваться" else "Войти")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            isRegister = !isRegister
            error = ""
            success = ""
        }) {
            Text(if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться")
        }

        if (currentUserId.value != null) {
            Spacer(Modifier.height(32.dp))
            Text("Вы вошли как пользователь ID: ${currentUserId.value}")
            Button(onClick = { currentUserId.value = null }) {
                Text("Выйти")
            }
        }
    }
}