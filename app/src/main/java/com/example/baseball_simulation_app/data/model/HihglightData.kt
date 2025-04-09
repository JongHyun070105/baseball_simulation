package com.example.baseball_simulation_app.data.model

data class HighlightData(
    val inning: String,
    val homeTeamName: String,
    val homeTeamScore: Int,
    val homeTeamLogoRes: Int,
    val awayTeamName: String,
    val awayTeamScore: Int,
    val awayTeamLogoRes: Int,
    val batterName: String,
    val pitcherName: String,
    val baseStatus: BaseStatus,
    val outCount: Int
)

data class BaseStatus(
    val first: Boolean = false,
    val second: Boolean = false,
    val third: Boolean = false
)