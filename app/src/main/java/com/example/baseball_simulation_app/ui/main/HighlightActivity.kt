package com.example.baseball_simulation_app.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.baseball_simulation_app.databinding.ItemHighlightBinding
import com.example.baseball_simulation_app.data.model.HighlightData
import com.example.baseball_simulation_app.data.model.BaseStatus
import com.example.baseball_simulation_app.databinding.ActivityHighlightBinding

class HighlightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighlightBinding
    private var teamId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighlightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get teamId from intent
        teamId = intent.getStringExtra(EXTRA_TEAM_ID) ?: ""

        setupRecyclerView()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        binding.rvHighlights.apply {
            layoutManager = LinearLayoutManager(this@HighlightActivity)
            adapter = HighlightAdapter(getHighlightDataForTeam(teamId))
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()  // Close the activity instead of popping back stack
        }
    }

    private fun getHighlightDataForTeam(teamId: String): List<HighlightData> {
        // In a real implementation, this would fetch highlight data based on teamId
        // For now, return example data
        return listOf(
            HighlightData(
                inning = "5회초",
                homeTeamName = "엘지",
                homeTeamScore = 3,
                homeTeamLogoRes = getLogoResIdForTeam(teamId),
                awayTeamName = "KT",
                awayTeamScore = 1,
                awayTeamLogoRes = getLogoResIdForOpponent(teamId),
                batterName = "김현수",
                pitcherName = "고영표",
                baseStatus = BaseStatus(first = true, second = false, third = false),
                outCount = 1
            )
        )
    }

    // Get logo resource based on teamId (placeholder implementation)
    private fun getLogoResIdForTeam(teamId: String): Int {
        // Replace with actual logic
        return com.example.baseball_simulation_app.R.drawable.placeholder_logo
    }

    private fun getLogoResIdForOpponent(teamId: String): Int {
        // Replace with actual logic
        return com.example.baseball_simulation_app.R.drawable.placeholder_logo
    }

    // Adapter implementation
    inner class HighlightAdapter(private val highlights: List<HighlightData>) :
        RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

        inner class HighlightViewHolder(private val binding: ItemHighlightBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(highlight: HighlightData) {
                binding.apply {
                    tvInning.text = highlight.inning
                    tvHomeTeamName.text = highlight.homeTeamName
                    tvHomeScore.text = highlight.homeTeamScore.toString()
                    ivHomeTeamLogo.setImageResource(highlight.homeTeamLogoRes)

                    tvAwayTeamName.text = highlight.awayTeamName
                    tvAwayScore.text = highlight.awayTeamScore.toString()
                    ivAwayTeamLogo.setImageResource(highlight.awayTeamLogoRes)

                    tvHomePlayerInfo.text = highlight.batterName
                    tvAwayPlayerInfo.text = highlight.pitcherName

                    // Set base status
                    ivFirstBase.setBackgroundResource(
                        if (highlight.baseStatus.first) com.example.baseball_simulation_app.R.drawable.base_fill
                        else com.example.baseball_simulation_app.R.drawable.base_empty
                    )
                    ivSecondBase.setBackgroundResource(
                        if (highlight.baseStatus.second) com.example.baseball_simulation_app.R.drawable.base_fill
                        else com.example.baseball_simulation_app.R.drawable.base_empty
                    )
                    ivThirdBase.setBackgroundResource(
                        if (highlight.baseStatus.third) com.example.baseball_simulation_app.R.drawable.base_fill
                        else com.example.baseball_simulation_app.R.drawable.base_empty
                    )

                    // Set out count
                    when (highlight.outCount) {
                        0 -> {
                            ivOut1.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_empty)
                            ivOut2.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_empty)
                        }
                        1 -> {
                            ivOut1.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_fill)
                            ivOut2.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_empty)
                        }
                        2 -> {
                            ivOut1.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_fill)
                            ivOut2.setBackgroundResource(com.example.baseball_simulation_app.R.drawable.out_fill)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
            val binding = ItemHighlightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return HighlightViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
            holder.bind(highlights[position])
        }

        override fun getItemCount(): Int = highlights.size
    }

    companion object {
        const val EXTRA_TEAM_ID = "extra_team_id"
    }
}