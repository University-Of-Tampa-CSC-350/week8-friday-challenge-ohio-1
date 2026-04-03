package com.example.fc_006.network

import com.example.fc_006.model.AsteroidBrowseResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaNeoApiService {

    @GET("neo/rest/v1/neo/browse")
    suspend fun getAsteroids(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): AsteroidBrowseResponse
}

object NasaNeoApi {
    private const val BASE_URL = "https://api.nasa.gov/"

    val service: NasaNeoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NasaNeoApiService::class.java)
    }
}
