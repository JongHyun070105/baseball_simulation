package com.example.baseball_simulation_app.ui.main

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ActivityMypickBinding
import android.view.View  // 추가해줘야 함 (View.GONE 쓰려고)

class MyPickActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypickBinding
    private var batterPlayer: Player? = null
    private var pitcherPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        getIntentData()
        displayPlayers()
        setupStartButton()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun getIntentData() {
        // Get players from intent
        batterPlayer = intent.getSerializableExtra("selected_batter") as? Player
        pitcherPlayer = intent.getSerializableExtra("selected_pitcher") as? Player

        // If no players were passed, create sample data
        if (batterPlayer == null) {
            batterPlayer = Player(
                name = "이재현",
                position = "외야수",
                playerImageUrl = "",
                isHomeTeam = true,
                battingAverage = 0.329,
                hits = 121,
                homeRuns = 21,
                rbi = 75,
                onBasePercentage = 0.420,
                sluggingPercentage = 0.580
            )
        }

        if (pitcherPlayer == null) {
            pitcherPlayer = Player(
                name = "류현진",
                position = "투수",
                playerImageUrl = "",
                isHomeTeam = false,
                era = 3.02,
                wins = 3,
                losses = 2,
                holds = 47,
                saves = 1,
                inningsPitched = 150.2
            )
        }
    }

    private fun displayPlayers() {
        // Display batter information
        batterPlayer?.let { batter ->
            with(binding) {
                tvBatterName.text = batter.name

                // Safely set batting stats
                tvBattingAvg.text = batter.battingAverage?.let { String.format("%.3f", it) } ?: "-"
                tvHits.text = batter.hits?.toString() ?: "-"
                tvHomeRuns.text = batter.homeRuns?.toString() ?: "-"
                tvRBIs.text = batter.rbi?.toString() ?: "-"

                // Load batter image
                ivBatterImage.loadImage(batter.playerImageUrl)
            }
        }

        // Display pitcher information
        pitcherPlayer?.let { pitcher ->
            with(binding) {
                tvPitcherName.text = pitcher.name

                // Safely set pitching stats
                tvERA.text = pitcher.era?.let { String.format("%.2f", it) } ?: "-"
                tvWinLoss.text = "${pitcher.wins ?: 0}승 ${pitcher.losses ?: 0}패"
                tvHolds.text = pitcher.holds?.toString() ?: "-"
                tvSaves.text = pitcher.saves?.toString() ?: "-"

                // Load pitcher image
                ivPitcherImage.loadImage(pitcher.playerImageUrl)
            }
        }

        // ✨ 하이라이트 섹션의 Play 버튼 숨기기 추가 ✨
        binding.highlightSection.btnPlay.visibility = View.GONE
    }


    private fun setupStartButton() {
        binding.btnStart.setOnClickListener {
            // TODO: Start simulation logic
        }
    }

    private fun ImageView.loadImage(url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .into(this)
        } else {
            setImageResource(R.drawable.placeholder_logo)
        }
    }
}