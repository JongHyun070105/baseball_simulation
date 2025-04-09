package com.example.baseball_simulation_app.API

import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.data.model.TeamModel
import com.google.gson.annotations.SerializedName

data class GameResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("dateTime")
    val dateTime: Long,
    @SerializedName("stadium")
    val stadium: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("away")
    val away: TeamResponse,
    @SerializedName("home")
    val home: TeamResponse
) {
    fun toGameModel(): GameModel? {
        if (status == "CANCEL") return null
        return GameModel(
            id = code,
            homeTeam = home.toTeamModel(),
            awayTeam = away.toTeamModel(),
            homeScore = home.score,
            awayScore = away.score,
            stadium = stadium,
            date = dateTime.toString(),
            status = status
        )
    }
}

data class TeamResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("logoUrl")
    val logoUrl: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("isWin")
    val isWin: Boolean
) {
    fun toTeamModel(): TeamModel {
        return TeamModel(
            id = code,
            displayName = name,
            logoUrl = logoUrl
        )
    }
}