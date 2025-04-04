package com.example.baseball_simulation_app.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.Game
import com.example.baseball_simulation_app.data.repository.GameRepository

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameRepository = GameRepository()
    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> get() = _games

    private val _teams = MutableLiveData<List<String>>()
    val teams: LiveData<List<String>> get() = _teams

    fun loadGames(year: Int, month: Int) {
        val gamesFromRepository = gameRepository.getGamesByMonth(year, month)
        _games.value = gamesFromRepository
    }

    fun loadTeamNames(context: Context) {
        val resources: Resources = context.resources
        val teamNames = resources.getStringArray(R.array.teamNames).toList()
        _teams.value = teamNames
    }
}
