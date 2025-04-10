package com.example.baseball_simulation_app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ActivityResultBinding


class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var resultAdapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val batterPlayer = intent.getSerializableExtra("BATTER_PLAYER") as? Player
        val pitcherPlayer = intent.getSerializableExtra("PITCHER_PLAYER") as? Player
        val homeTeamName = intent.getStringExtra("HOME_TEAM_NAME") ?: "홈팀"
        val awayTeamName = intent.getStringExtra("AWAY_TEAM_NAME") ?: "원정팀"
        val homeScore = intent.getIntExtra("HOME_SCORE", 0)
        val awayScore = intent.getIntExtra("AWAY_SCORE", 0)
        val stadium = intent.getStringExtra("STADIUM") ?: "야구장"

        // Setup RecyclerView
        resultAdapter = ResultAdapter()
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@ResultActivity)
            adapter = resultAdapter
        }

        // Create and add result item
        val resultItem = createResultItem(
            homeTeamName, awayTeamName, homeScore, awayScore, stadium,
            batterPlayer?.name ?: "타자", pitcherPlayer?.name ?: "투수"
        )

        resultAdapter.submitList(listOf(resultItem))
    }

    private fun createResultItem(
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        stadium: String,
        batterName: String,
        pitcherName: String
    ): ResultItem {
        return ResultItem(
            homeTeamName = homeTeam,
            awayTeamName = awayTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            stadium = stadium,
            batterName = batterName,
            pitcherName = pitcherName
        )
    }

    // Data class for results
    data class ResultItem(
        val homeTeamName: String,
        val awayTeamName: String,
        val homeScore: Int,
        val awayScore: Int,
        val stadium: String,
        val batterName: String,
        val pitcherName: String
    )
}