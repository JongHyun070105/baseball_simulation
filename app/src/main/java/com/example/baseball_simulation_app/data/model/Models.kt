package com.example.baseball_simulation_app.data.model

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
    val highlights: List<String>
) : Parcelable

@Parcelize
data class TeamModel(
    val id: String,
    val displayName: String,
    val logoResId: Int
) : Parcelable

@Parcelize
data class PlayerModel(
    val id: String,
    val name: String,
    val teamId: String,
    val position: String,
    val battingAverage: Float,
    val homeRuns: Int,
    val rbi: Int,
    val era: Float? = null,  // 투수용
    val wins: Int? = null,   // 투수용
    val losses: Int? = null, // 투수용
    val imageResId: Int? = null
) : Parcelable