package com.example.baseball_simulation_app.data.model

data class Game(
    val id: Int,
    val homeTeamName: String,
    val homeTeamLogoUrl: String,
    val awayTeamName: String,
    val awayTeamLogoUrl: String,
    val score: String,
    val stadium: String,
    val date: String,
)
