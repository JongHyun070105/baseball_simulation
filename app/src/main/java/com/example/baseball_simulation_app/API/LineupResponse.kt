package com.example.baseball_simulation_app.API

import com.google.gson.annotations.SerializedName

data class LineupResponse(
    val away: TeamLineup,
    val home: TeamLineup
)

data class TeamLineup(
    val team: Team,
    @SerializedName("startingLineups") val startingLineups: List<Player>,
    @SerializedName("benchLineups") val benchLineups: List<Player>,
    @SerializedName("bullpenLineups") val bullpenLineups: List<Player>
)

data class Team(
    val name: String,
    val code: String,
    @SerializedName("logoUrl") val logoUrl: String,
    val color: String
)

data class Player(
    val code: String,
    val name: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    val position: List<String>? = null,
    @SerializedName("battingOrder") val battingOrder: Int? = null,
    val stats: Stats? = null
)

data class Stats(
    val name: String? = null,
    // 타자 스탯
    @SerializedName("타율") val battingAverage: String? = null,
    val OPS: String? = null,
    @SerializedName("wRC+") val wRCPlus: String? = null,
    val WAR: String? = null,
    @SerializedName("경기") val games: String? = null,
    @SerializedName("타수") val atBats: String? = null,
    @SerializedName("안타") val hits: String? = null,
    @SerializedName("2루타") val doubles: String? = null,
    @SerializedName("3루타") val triples: String? = null,
    @SerializedName("홈런") val homeRuns: String? = null,
    @SerializedName("타점") val rbi: String? = null,
    @SerializedName("득점") val runs: String? = null,
    @SerializedName("도루") val stolenBases: String? = null,
    @SerializedName("타자_볼넷") val walks: String? = null,
    @SerializedName("삼진") val strikeouts: String? = null,
    @SerializedName("병살") val doublePlay: String? = null,
    @SerializedName("출루율") val onBasePercentage: String? = null,
    @SerializedName("장타율") val sluggingPercentage: String? = null,
    val IsoP: String? = null,
    val BABIP: String? = null,
    val wOBA: String? = null,
    val WPA: String? = null,
    val team: String? = null,

    // 투수 스탯
    @SerializedName("평균자책") val era: String? = null,
    val WHIP: String? = null,
    val QS: String? = null,
    @SerializedName("승") val wins: String? = null,
    @SerializedName("패") val losses: String? = null,
    @SerializedName("세이브") val saves: String? = null,
    @SerializedName("홀드") val holds: String? = null,
    @SerializedName("이닝") val innings: String? = null,
    @SerializedName("탈삼진") val pitcherStrikeouts: String? = null,
    @SerializedName("피안타") val hitsAllowed: String? = null,
    @SerializedName("피홈런") val homeRunsAllowed: String? = null,
    @SerializedName("투수_볼넷") val pitcherWalks: String? = null,
    @SerializedName("사구") val hitByPitch: String? = null,
    @SerializedName("폭투") val wildPitches: String? = null,
    @SerializedName("실점") val runsAllowed: String? = null,
    @SerializedName("승률") val winningPercentage: String? = null,
    @SerializedName("K/9") val strikeoutsPer9: String? = null,
    @SerializedName("BB/9") val walksPer9: String? = null,
    @SerializedName("K/BB") val strikeoutToWalkRatio: String? = null,
    @SerializedName("K %") val strikeoutPercentage: String? = null,
    @SerializedName("BB %") val walkPercentage: String? = null
)