package com.example.baseball_simulation_app.data.model

import android.health.connect.datatypes.units.Percentage
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameModel(
    val id: String,
    val homeTeam: TeamModel,
    val awayTeam: TeamModel,
    val homeScore: Int,
    val awayScore: Int,
    val stadium: String,
    val date: String,
    val status: String, // 경기 상태 추가 (END, LIVE, Cancle 등)
) : Parcelable

@Parcelize
data class TeamModel(
    val id: String,
    val displayName: String,
    val logoUrl: String // 리소스 ID 대신 URL로 변경
) : Parcelable