// Updated: app/src/main/kotlin/com/example/phon_krisha/apistate/AuthState.kt
package com.example.phon_krisha.apistate

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Расширение DataStore (обязательно!)
private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val USER_ID_KEY = intPreferencesKey("user_id")
private val IS_GUEST_KEY = booleanPreferencesKey("is_guest")

object AuthState {

    var currentUserId = mutableStateOf<Int?>(null)
        private set

    var isGuest = false

    // Сохраняем ID
    suspend fun saveUserId(context: Context, userId: Int) {
        context.userDataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[IS_GUEST_KEY] = false
        }
        currentUserId.value = userId
        isGuest = false
    }

    // Сохраняем как гость
    suspend fun saveAsGuest(context: Context) {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
            prefs[IS_GUEST_KEY] = true
        }
        currentUserId.value = null
        isGuest = true
    }

    // Загружаем при старте приложения
    fun restore(context: Context) = runBlocking {
        val prefs = context.userDataStore.data.first()
        currentUserId.value = prefs[USER_ID_KEY]
        isGuest = prefs[IS_GUEST_KEY] ?: false
    }

    // Очищаем при выходе
    suspend fun clearUserId(context: Context) {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
            prefs.remove(IS_GUEST_KEY)
        }
        currentUserId.value = null
        isGuest = false
    }
}