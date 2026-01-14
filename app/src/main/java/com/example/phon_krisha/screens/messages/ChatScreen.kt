// Updated: app/src/main/kotlin/com/example/phon_krisha/messages/ChatScreen.kt
package com.example.phon_krisha.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.Message
import com.example.phon_krisha.network.SendMessageRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarToday

@OptIn(ExperimentalMaterial3Api::class)
// Основные улучшения в ChatScreen.kt

// 1. Убираем выравнивание по центру для пустого состояния
// 2. Делаем более естественное поведение при отсутствии сообщений
// 3. Добавляем небольшой отступ снизу у списка сообщений
// 4. Улучшаем стили баблов и общий вид

@Composable
fun ChatScreen(
    currentUserId: Int,
    toUserId: Int,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // ... (проверка гостя и LaunchedEffect остаются без изменений)

    Scaffold(
        topBar = { /* без изменений */ },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Область сообщений
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    messages.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Сообщений пока нет",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Напишите первое сообщение собеседнику",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 8.dp),          // ← небольшой отступ снизу
                            verticalArrangement = Arrangement.Bottom,
                            contentPadding = PaddingValues(
                                bottom = 8.dp,
                                top = 8.dp
                            )
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isOwnMessage = message.from_user_id == currentUserId
                                )
                            }
                        }
                    }
                }
            }

            // Поле ввода — делаем чуть красивее и современнее
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        placeholder = { Text("Сообщение...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                    )

                    IconButton(
                        onClick = {
                            if (newMessageText.isNotBlank()) {
                                scope.launch {
                                    try {
                                        ApiClient.api.sendMessage(
                                            SendMessageRequest(
                                                from_user_id = currentUserId,
                                                to_user_id = toUserId,
                                                message = newMessageText.trim()
                                            )
                                        )
                                        messages = ApiClient.api.getMessages(currentUserId, toUserId)
                                        newMessageText = ""
                                    } catch (e: Exception) {
                                        // можно добавить snackbar с ошибкой
                                    }
                                }
                            }
                        },
                        enabled = newMessageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = if (newMessageText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    val backgroundColor = if (isOwnMessage)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isOwnMessage)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.timestamp?.take(16) ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}