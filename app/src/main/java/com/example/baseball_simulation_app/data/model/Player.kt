package com.example.baseball_simulation_app.data.model

import java.io.Serializable

data class Player(
    val name: String = "",
    val position: String = "",
    val playerImageUrl: String = "",
    val isHomeTeam: Boolean = true,

    // 타자 스탯
    val battingAverage: Double? = null,
    val hits: Int? = null,
    val homeRuns: Int? = null,
    val rbi: Int? = null,
    val onBasePercentage: Double? = null,
    val sluggingPercentage: Double? = null,

    // 투수 스탯
    val era: Double? = null,
    val inningsPitched: Double? = null,
    val wins: Int? = null,
    val losses: Int? = null,
    val holds: Int? = null,
    val saves: Int? = null
) : Serializable
