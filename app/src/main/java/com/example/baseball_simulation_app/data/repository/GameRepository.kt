package com.example.baseball_simulation_app.data.repository

import android.util.Log
import com.example.baseball_simulation_app.client.RetrofitClient
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getGames(date: Date): NetworkResult<List<GameModel>> {
        return withContext(Dispatchers.IO) {
            try {
                // 연도는 2024년으로 고정하고, 월일은 현재 디바이스 시간에서 가져옴
                val monthDayFormat = SimpleDateFormat("MMdd", Locale.getDefault())
                val year = "2024" // 고정 연도
                val monthDay = monthDayFormat.format(date)
                val apiDate = year + monthDay

                Log.d("GameRepository", "요청 날짜: $apiDate, 원본 날짜: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)}")

                val response = apiService.getGames(apiDate)
                if (response.isSuccessful) {
                    val games = response.body()?.mapNotNull { it.toGameModel() } ?: emptyList()
                    NetworkResult.Success(games)
                } else {
                    NetworkResult.Error("서버 오류: ${response.code()}")
                }
            } catch (e: IOException) {
                NetworkResult.Error("네트워크 연결 오류")
            } catch (e: Exception) {
                NetworkResult.Error("알 수 없는 오류: ${e.message}")
            }
        }
    }
}