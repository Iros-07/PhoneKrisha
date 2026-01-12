package com.example.phon_krisha.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.ChatPartner
import kotlinx.coroutines.launch

@Composable
fun MessagesScreen(
    currentUserId: Int,
    onChatClick: (Int) -> Unit
) {
    var partners by remember { mutableStateOf<List<ChatPartner>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            partners = ApiClient.api.getChats(currentUserId) ?: emptyList()
        } catch (_: Exception) {
            partners = emptyList()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (partners.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет сообщений")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(partners) { partner ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { onChatClick(partner.partner_id) }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(partner.partner_name?.ifBlank { "Пользователь" } ?: "Пользователь")
                    Text(partner.message?.ifBlank { "Сообщений нет" } ?: "Сообщений нет")
                }
            }
        }
    }
}
