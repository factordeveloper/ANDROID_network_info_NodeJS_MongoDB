package com.factor.dev.retrofitcyberalerts

import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkService {
    @POST("api/networkinfo")
    suspend fun sendNetworkInfo(@Body networkInfo: NetworkInfo)
}