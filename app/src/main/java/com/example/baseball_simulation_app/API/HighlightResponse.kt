package com.example.baseball_simulation_app.API

import com.google.gson.annotations.SerializedName

data class HighlightResponse(
    @SerializedName("substitutions")
    val substitutions: List<SubstitutionItem>,
    @SerializedName("team_filter")
    val team_filter: String
)

data class SubstitutionItem(
    @SerializedName("inning")
    val inning: String,
    @SerializedName("substituted_player")
    val substitutedPlayer: String,
    @SerializedName("new_player")
    val newPlayer: String,
    @SerializedName("out_count")
    val outCount: Int,
    @SerializedName("position")
    val position: String,
    @SerializedName("team")
    val team: String,
    @SerializedName("current_pitcher")
    val currentPitcher: Player,
    @SerializedName("current_hitter")
    val currentHitter: Player,
    @SerializedName("score")
    val score: Score
)

data class Player(
    @SerializedName("name")
    val name: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("id")
    val id: String
)

data class Score(
    @SerializedName("away")
    val away: Int,
    @SerializedName("home")
    val home: Int
)