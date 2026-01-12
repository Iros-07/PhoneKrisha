// File: app/src/main/kotlin/com/example/phon_krisha/network/
package com.example.phon_krisha.network

import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Data classes
data class UserRegisterRequest(val fio: String, val phone: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(
    val id: Int?,
    val fio: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val error: String? = null
)

data class User(val id: Int, val fio: String, val phone: String, val email: String)

data class Ad(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val user_id: Int = 0,
    val rooms: Int = 0,
    val city: String = "",
    val photo: String? = null,
    val price: Long = 0,
    val ad_type: String = "",
    val house_type: String = "",
    val floor: Int = 0,
    val floors_in_house: Int = 0,
    val year_built: Int = 0,
    val area: Double = 0.0,
    val complex: String? = null
)

data class Message(
    val id: Int = 0,
    val from_user_id: Int,
    val to_user_id: Int,
    val message: String,
    val timestamp: String? = null
)

data class SendMessageRequest(val from_user_id: Int, val to_user_id: Int, val message: String)
data class AddFavoriteRequest(val user_id: Int, val ad_id: Int)
data class RemoveFavoriteRequest(val user_id: Int, val ad_id: Int)

data class ChatPartner(
    val partner_id: Int,
    val partner_name: String? = null,
    val message: String? = null,
    val timestamp: String? = null
)

// В файле network (например  или прямо в ApiService.kt)

data class UpdateUserRequest(
    val fio: String,
    val phone: String,
    val email: String,
    val password: String? = null // null = не менять пароль
)
interface ApiService {

    @POST("register")
    suspend fun register(@Body request: UserRegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("user/{user_id}")
    suspend fun getUser(@Path("user_id") userId: Int): User

    @GET("ads")
    suspend fun getAds(
        @Query("city") city: String? = null,
        @Query("rooms") rooms: Int? = null
    ): List<Ad>

    @GET("ads/{ad_id}")
    suspend fun getAd(@Path("ad_id") adId: Int): Ad

    @POST("ads/add")
    suspend fun addAd(@Body ad: Ad): Map<String, Any>

    @PUT("ads/update/{ad_id}")
    suspend fun updateAd(@Path("ad_id") adId: Int, @Body ad: Ad): Map<String, Any>

    @DELETE("ads/delete/{ad_id}")
    suspend fun deleteAd(@Path("ad_id") adId: Int): Map<String, Any>

    @GET("favorites/{user_id}")
    suspend fun getFavorites(@Path("user_id") userId: Int): List<Ad>

    @POST("favorites/add")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Map<String, Any>

    @POST("favorites/remove")
    suspend fun removeFavorite(@Body request: RemoveFavoriteRequest): Map<String, Any>

    @GET("messages/{from_id}/{to_id}")
    suspend fun getMessages(@Path("from_id") from: Int, @Path("to_id") to: Int): List<Message>

    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Map<String, Any>

    @PUT("user/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body request: UpdateUserRequest
    ): Any // или Map<String, Any>, или ваш тип ответа (status, error и т.д.)

    @GET("chats/{user_id}")
    suspend fun getChats(@Path("user_id") userId: Int): List<ChatPartner>

}



object ApiClient {
    private const val BASE_URL = "http://10.54.4.41:5000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}