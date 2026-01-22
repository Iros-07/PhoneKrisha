// ApiService.kt

package com.example.phon_krisha.network

import android.content.Context
import android.util.Log
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

import android.net.wifi.WifiManager
import android.text.format.Formatter


data class UserRegisterRequest(val fio: String, val phone: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val id: Int?, val fio: String? = null, val phone: String? = null, val email: String? = null, val error: String? = null)

data class User(val id: Int, val fio: String, val phone: String, val email: String)

data class Ad(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val user_id: Int = 0,
    val rooms: Int = 0,
    val city: String = "",
    val address: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val photos: List<String>? = null,
    val price: Long = 0,
    val ad_type: String = "",
    val house_type: String = "",
    val floor: Int = 0,
    val floors_in_house: Int = 0,
    val year_built: Int = 0,
    val area: Double = 0.0,
    val complex: String? = null,
    val user_fio: String? = null,
    val user_phone: String? = null
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

data class UpdateUserRequest(
    val fio: String,
    val phone: String,
    val email: String,
    val password: String? = null
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
        @Query("title") title: String? = null,
        @Query("city") city: String? = null,
        @Query("rooms") rooms: Int? = null,
        @Query("price_min") price_min: Long? = null,
        @Query("price_max") price_max: Long? = null,
        @Query("ad_type") ad_type: String? = null,
        @Query("house_type") house_type: String? = null,
        @Query("floor_min") floor_min: Int? = null,
        @Query("floor_max") floor_max: Int? = null,
        @Query("year_built_min") year_built_min: Int? = null,
        @Query("year_built_max") year_built_max: Int? = null,
        @Query("area_min") area_min: Double? = null,
        @Query("area_max") area_max: Double? = null,
        @Query("complex") complex: String? = null
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
    suspend fun updateUser(@Path("user_id") userId: Int, @Body request: UpdateUserRequest): Any

    @GET("chats/{user_id}")
    suspend fun getChats(@Path("user_id") userId: Int): List<ChatPartner>

    @Multipart
    @POST("upload_photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): Map<String, String>
}


object ApiClient {
    private const val FALLBACK = "http://10.184.78.41:5000/"
    var baseUrl: String = FALLBACK

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }



    private fun getGatewayIp(context: Context): String? {
        return try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return null

            if (!wifi.isWifiEnabled) return null

            val dhcp = wifi.dhcpInfo ?: return null

            Formatter.formatIpAddress(dhcp.gateway)
        } catch (e: Exception) {
            Log.w("ApiClient", "Не удалось получить gateway IP", e)
            null
        }
    }

    private fun getCommonRouterGuesses(): String {
        return listOf(
            "192.168.31.132",
            "10.246.12.41"
        ).first()

    }
}