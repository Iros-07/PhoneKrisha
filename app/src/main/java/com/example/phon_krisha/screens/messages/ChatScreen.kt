
package com.example.phon_krisha.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.ArrowBack
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(currentUserId: Int, toUserId: Int, onBack: () -> Unit) {
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var newMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(toUserId) {
        withContext(Dispatchers.IO) {
            messages = ApiClient.instance.getMessages(currentUserId, toUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (toUserId == 1) "Поддержка" else "Чат") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f)) {
                items(messages) { msg ->
                    Text(
                        text = msg.message,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(if (msg.from_user_id == currentUserId) Alignment.End else Alignment.Start)
                    )
                }
            }

            Row(Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newMessage.isNotBlank()) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                ApiClient.instance.sendMessage(
                                    mapOf(
                                        "from_user_id" to currentUserId,
                                        "to_user_id" to toUserId,
                                        "message" to newMessage
                                    )
                                )
                                messages = ApiClient.instance.getMessages(currentUserId, toUserId)
                            }
                        }
                        newMessage = ""
                    }
                }) {
                    Icon(Icons.Default.Send, null)
                }
            }
        }
    }
}