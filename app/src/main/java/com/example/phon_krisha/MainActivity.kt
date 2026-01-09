package com.example.phon_krisha

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.phon_krisha.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ТЕСТ ПОДКЛЮЧЕНИЯ К СЕРВЕРУ
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.apiService.ping()
                Log.e("API_TEST", "SUCCESS: $res")
            } catch (e: Exception) {
                Log.e("API_TEST", "ERROR", e)
            }
        }

        setContent {
            Text("TEST")
        }

    }
}
