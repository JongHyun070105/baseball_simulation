package com.example.baseball_simulation_app.ui.main

    import android.os.Parcelable
    import com.example.baseball_simulation_app.data.model.TeamModel
    import kotlinx.parcelize.Parcelize

    @Parcelize
    data class HighlightModel(
        val id: String,
        val gameId: String,
        val inning: Int,
        val isTop: Boolean, // true = 초(top), false = 말(bottom)
        val homeTeam: TeamModel,
        val awayTeam: TeamModel,
        val homeScore: Int,
        val awayScore: Int,
        val situation: String, // 상황 설명 (예: "2사 1,3루")
        val description: String // 하이라이트 설명
    ) : Parcelable