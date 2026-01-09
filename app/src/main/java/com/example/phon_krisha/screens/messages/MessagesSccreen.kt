package com.example.phon_krisha.messages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MessagesScreen(currentUserId: Int) {
    var chats by remember { mutableStateOf(listOf<Int>()) }  // Список собеседников

    // Здесь можно получить список чатов из БД или mock
    val mockChats = listOf(1, 2, 3)  // 1 — поддержка

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(mockChats) { toUserId ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable { /* Открыть ChatScreen(currentUserId, toUserId) */ }
            ) {
                Text(
                    if (toUserId == 1) "Поддержка" else "Пользователь $toUserId",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}