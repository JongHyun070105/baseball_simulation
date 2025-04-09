package com.example.baseball_simulation_app.API

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("api/get_schedule/{date}")
    suspend fun getGames(@Path("date") date: String): Response<List<GameResponse>>
}
