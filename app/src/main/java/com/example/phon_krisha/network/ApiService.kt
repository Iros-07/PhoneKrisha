package com.example.phon_krisha.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class UserRegisterRequest(val fio: String, val phone: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

data class AuthResponse(val id: Int?, val error: String?)

data class Ad(
    val id: Int = 0,
    val user_id: Int = 0,
    val rooms: Int? = null,
    val city: String? = null,
    val photo: String? = null,
    val price: Long? = null,
    val ad_type: String? = null,
    val house_type: String? = null,
    val floor: Int? = null,
    val floors_in_house: Int? = null,
    val year_built: Int? = null,
    val area: Float? = null,
    val complex: String? = null
)

data class Message(
    val id: Int,
    val from_user_id: Int,
    val to_user_id: Int,
    val message: String,
    val timestamp: String
)

interface ApiService {
    @GET("ping")
    suspend fun ping(): Map<String, String>

    @POST("register")
    suspend fun register(@Body request: UserRegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("ads")
    suspend fun getAds(): List<Ad>

    @POST("add_ad")
    suspend fun addAd(@Body ad: Ad): Map<String, Any>

    @GET("favorites")
    suspend fun getFavorites(@Query("user_id") userId: Int): List<Ad>

    @POST("favorite")
    suspend fun toggleFavorite(@Body body: Map<String, Int>): Map<String, Boolean>

    @GET("messages")
    suspend fun getMessages(
        @Query("user_id") userId: Int,
        @Query("to_user_id") toUserId: Int
    ): List<Message>

    @POST("send_message")
    suspend fun sendMessage(@Body body: Map<String, Any>): Map<String, Boolean>
}

object ApiClient {
    private const val BASE_URL = "http://192.168.31.132:5000/"  // Твой текущий IP ноутбука

    val instance: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}