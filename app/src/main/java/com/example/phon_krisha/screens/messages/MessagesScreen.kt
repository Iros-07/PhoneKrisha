// MessagesScreen.kt
package com.example.phon_krisha.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.phon_krisha.network.ChatPartner
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    currentUserId: Int,
    onChatClick: (Int) -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var partners by remember { mutableStateOf<List<ChatPartner>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!AuthState.isGuest) {
                try {
                    partners = ApiClient.api.getChats(currentUserId) ?: emptyList()
                } catch (e: Exception) {
                    // silent fail
                } finally {
                    isLoading = false
                }
            }
            delay(8000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сообщения") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (AuthState.isGuest) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                TextButton(onClick = { navController.navigate("profile") }) {
                    Text(
                        "Гости не могут использовать сообщения.\nВойдите в аккаунт.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(partners) { partner ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = { onChatClick(partner.partner_id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = partner.partner_name?.ifBlank { "Пользователь" } ?: "Пользователь",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = partner.message?.ifBlank { "Нет сообщений" } ?: "Нет сообщений",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}