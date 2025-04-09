package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.databinding.ActivityHighlightBinding
import com.example.baseball_simulation_app.databinding.ItemHighlightBinding
import com.example.baseball_simulation_app.data.model.HighlightData
import com.example.baseball_simulation_app.data.model.BaseStatus

class HighlightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighlightBinding
    private var teamId: String = ""
    private var isHomeTeam: Boolean = true
    private var gameId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighlightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 값 받아오기
        teamId = intent.getStringExtra(EXTRA_TEAM_ID) ?: ""
        isHomeTeam = intent.getBooleanExtra(EXTRA_IS_HOME_TEAM, true)
        gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: ""

        setupRecyclerView()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        binding.rvHighlights.apply {
            layoutManager = LinearLayoutManager(this@HighlightActivity)
            adapter = HighlightAdapter(
                getDummyHighlightData(gameId, teamId, isHomeTeam),
                teamId,
                isHomeTeam,
                gameId
            )
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
    }

    private fun getDummyHighlightData(gameId: String, teamId: String, isHomeTeam: Boolean): List<HighlightData> {
        return if (isHomeTeam) {
            listOf(
                HighlightData(
                    inning = "5회초",
                    homeTeamName = "KT",
                    homeTeamLogoRes = getLogoResIdForTeam(teamId),
                    awayTeamName = "LG",
                    awayTeamLogoRes = getLogoResIdForOpponent(teamId),
                    batterName = "강백호",
                    pitcherName = "고우석",
                    baseStatus = BaseStatus(first = true, second = false, third = true),
                    outCount = 1
                ),
                HighlightData(
                    inning = "7회초",
                    homeTeamName = "KT",
                    homeTeamLogoRes = getLogoResIdForTeam(teamId),
                    awayTeamName = "LG",
                    awayTeamLogoRes = getLogoResIdForOpponent(teamId),
                    batterName = "알포드",
                    pitcherName = "정우영",
                    baseStatus = BaseStatus(first = false, second = false, third = true),
                    outCount = 2
                )
            )
        } else {
            listOf(
                HighlightData(
                    inning = "5회말",
                    homeTeamName = "KT",
                    homeTeamLogoRes = getLogoResIdForOpponent(teamId),
                    awayTeamName = "LG",
                    awayTeamLogoRes = getLogoResIdForTeam(teamId),
                    batterName = "김현수",
                    pitcherName = "벤자민",
                    baseStatus = BaseStatus(first = false, second = true, third = false),
                    outCount = 0
                )
            )
        }
    }

    private fun getLogoResIdForTeam(teamId: String): Int {
        return R.drawable.placeholder_logo
    }

    private fun getLogoResIdForOpponent(teamId: String): Int {
        return R.drawable.placeholder_logo
    }

    inner class HighlightAdapter(
        private val highlights: List<HighlightData>,
        private val teamId: String,
        private val isHomeTeam: Boolean,
        private val gameId: String
    ) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

        inner class HighlightViewHolder(private val binding: ItemHighlightBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(highlight: HighlightData) {
                binding.apply {
                    tvInning.text = highlight.inning
                    tvHomeTeamName.text = highlight.homeTeamName
                    ivHomeTeamLogo.setImageResource(highlight.homeTeamLogoRes)

                    tvAwayTeamName.text = highlight.awayTeamName
                    ivAwayTeamLogo.setImageResource(highlight.awayTeamLogoRes)

                    tvHomePlayerInfo.text = highlight.batterName
                    tvAwayPlayerInfo.text = highlight.pitcherName

                    ivFirstBase.setBackgroundResource(
                        if (highlight.baseStatus.first) R.drawable.base_fill
                        else R.drawable.base_empty
                    )
                    ivSecondBase.setBackgroundResource(
                        if (highlight.baseStatus.second) R.drawable.base_fill
                        else R.drawable.base_empty
                    )
                    ivThirdBase.setBackgroundResource(
                        if (highlight.baseStatus.third) R.drawable.base_fill
                        else R.drawable.base_empty
                    )

                    when (highlight.outCount) {
                        0 -> {
                            ivOut1.setBackgroundResource(R.drawable.out_empty)
                            ivOut2.setBackgroundResource(R.drawable.out_empty)
                        }
                        1 -> {
                            ivOut1.setBackgroundResource(R.drawable.out_fill)
                            ivOut2.setBackgroundResource(R.drawable.out_empty)
                        }
                        2 -> {
                            ivOut1.setBackgroundResource(R.drawable.out_fill)
                            ivOut2.setBackgroundResource(R.drawable.out_fill)
                        }
                    }

                    btnPlay.setOnClickListener {
                        val context = itemView.context
                        val intent = Intent(context, ChangeMemberActivity::class.java).apply {
                            putExtra(ChangeMemberActivity.EXTRA_TEAM_ID, teamId)
                            putExtra(ChangeMemberActivity.EXTRA_IS_HOME_TEAM, isHomeTeam)
                            putExtra(ChangeMemberActivity.EXTRA_GAME_ID, gameId)
                        }
                        context.startActivity(intent)

                        if (context is AppCompatActivity) {
                            context.overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
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

        override fun getItemCount() = highlights.size
    }

    companion object {
        const val EXTRA_TEAM_ID = "extra_team_id"
        const val EXTRA_IS_HOME_TEAM = "extra_is_home_team"
        const val EXTRA_GAME_ID = "extra_game_id"
    }
}
