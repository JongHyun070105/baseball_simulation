package com.example.baseball_simulation_app.data.model

data class MatchResult(
    val homeTeamName: String,
    val homeTeamLogo: Int,  // Resource ID
    val homeScore: Int,
    val awayTeamName: String,
    val awayTeamLogo: Int,  // Resource ID
    val awayScore: Int,
    val stadium: String,
    val beforeWinRate: Float,  // 교체 전 승률
    val afterWinRate: Float,   // 교체 후 승률
)