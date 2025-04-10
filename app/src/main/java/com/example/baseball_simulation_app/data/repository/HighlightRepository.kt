package com.example.baseball_simulation_app.data.repository

import android.util.Log
import com.example.baseball_simulation_app.API.HighlightResponse
import com.example.baseball_simulation_app.client.RetrofitClient
import com.example.baseball_simulation_app.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class HighlightRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getHighlights(code: String): NetworkResult<HighlightResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("HighlightRepository", "요청 코드: $code")

                val response = apiService.getHighlights(code)
                if (response.isSuccessful) {
                    val highlightResponse = response.body()
                    if (highlightResponse != null) {
                        NetworkResult.Success(highlightResponse)
                    } else {
                        NetworkResult.Error("하이라이트 데이터가 없습니다")
                    }
                } else {
                    NetworkResult.Error("서버 오류: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("HighlightRepository", "네트워크 오류", e)
                NetworkResult.Error("네트워크 연결 오류")
            } catch (e: Exception) {
                Log.e("HighlightRepository", "알 수 없는 오류", e)
                NetworkResult.Error("알 수 없는 오류: ${e.message}")
            }
        }
    }
}