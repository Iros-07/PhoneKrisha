//ProfileScreen.kt
package com.example.phon_krisha.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavHostController
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val loggedInUserId by AuthState.currentUserId
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var user by remember { mutableStateOf<User?>(null) }
    var myAds by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var isEditing by remember { mutableStateOf(false) }

    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(loggedInUserId) {
        loggedInUserId?.let { id ->
            try {
                user = ApiClient.api.getUser(id)
                user?.let {
                    fio = it.fio
                    phone = it.phone
                    email = it.email
                }
                myAds = ApiClient.api.getAds().filter { it.user_id == id }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if (loggedInUserId == null && !AuthState.isGuest) {
            AuthForm(navController)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(32.dp))

                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(user?.fio ?: "Пользователь", style = MaterialTheme.typography.headlineMedium)
                Text(user?.phone ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(32.dp))

                if (!isEditing) {
                    Button(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Редактировать")
                    }
                }

                if (isEditing) {
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(fio, { fio = it }, label = { Text("ФИО") })
                    OutlinedTextField(phone, { phone = it }, label = { Text("Телефон") })
                    OutlinedTextField(email, { email = it }, label = { Text("Email") })
                    OutlinedTextField(
                        password,
                        { password = it },
                        label = { Text("Новый пароль") },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = {
                        scope.launch {
                            try {
                                ApiClient.api.updateUser(
                                    loggedInUserId!!,
                                    UpdateUserRequest(
                                        fio, phone, email,
                                        password.takeIf { it.isNotBlank() }
                                    )
                                )
                                isEditing = false
                                user = ApiClient.api.getUser(loggedInUserId!!)
                            } catch (_: Exception) {}
                        }
                    }) {
                        Text("Сохранить")
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text("Мои объявления", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(16.dp))

                if (myAds.isEmpty()) {
                    Text("Объявлений нет")
                } else {
                    myAds.forEach { ad ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(ad.title)
                                Text("${ad.price} ₸")

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate("edit_ad/${ad.id}")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Изменить")
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                ApiClient.api.deleteAd(ad.id)
                                                myAds = myAds.filter { it.id != ad.id }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Удалить")
                                    }
                                }
                            }
                        }
                    }

                }

                Spacer(Modifier.height(32.dp))

                OutlinedButton(onClick = {
                    scope.launch {
                        AuthState.clearUserId(context)
                        navController.navigate("profile") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти")
                }
            }
        }
    }
}

@Composable
private fun AuthForm(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLoginMode by remember { mutableStateOf(true) }
    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(email, { email = it }, label = { Text("Email") })
        OutlinedTextField(
            password, { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation()
        )

        if (!isLoginMode) {
            OutlinedTextField(fio, { fio = it }, label = { Text("ФИО") })
            OutlinedTextField(phone, { phone = it }, label = { Text("Телефон") })
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                try {
                    if (isLoginMode) {
                        val r = ApiClient.api.login(LoginRequest(email, password))
                        if (r.id != null) {
                            AuthState.saveUserId(context, r.id)
                            navController.navigate("home") { popUpTo(0) { inclusive = true } }
                        }
                    } else {
                        val r = ApiClient.api.register(UserRegisterRequest(fio, phone, email, password))
                        if (r.id != null) {
                            AuthState.saveUserId(context, r.id)
                            navController.navigate("home") { popUpTo(0) { inclusive = true } }
                        }
                    }
                } catch (_: Exception) {}
            }
        }) {
            Text(if (isLoginMode) "Войти" else "Регистрация")
        }

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(if (isLoginMode) "Нет аккаунта?" else "Уже есть аккаунт?")
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    AuthState.saveAsGuest(context)
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти как гость")
        }

    }
}
