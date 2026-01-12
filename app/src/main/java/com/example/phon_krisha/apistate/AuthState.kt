package com.example.phon_krisha.apistate

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val USER_ID_KEY = intPreferencesKey("user_id")

object AuthState {

    // Единственный источник состояния для Compose
    var currentUserId = mutableStateOf<Int?>(null)
        private set

    suspend fun saveUserId(context: Context, userId: Int) {
        context.userDataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
        currentUserId.value = userId
    }

    fun restore(context: Context) = runBlocking {
        val prefs = context.userDataStore.data.first()
        currentUserId.value = prefs[USER_ID_KEY]
    }

    suspend fun clearUserId(context: Context) {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
        }
        currentUserId.value = null
    }
}
