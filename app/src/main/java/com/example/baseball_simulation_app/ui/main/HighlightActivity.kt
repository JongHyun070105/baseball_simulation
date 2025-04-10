package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.API.SubstitutionItem
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.repository.HighlightRepository
import com.example.baseball_simulation_app.databinding.ActivityHighlightBinding
import com.example.baseball_simulation_app.databinding.ItemHighlightBinding
import com.example.baseball_simulation_app.network.NetworkResult
import com.example.baseball_simulation_app.svgformat.GlideApp
import kotlinx.coroutines.launch

class HighlightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighlightBinding
    private var isHomeTeam: Boolean = true
    private var gameId: String = ""
    private var homeTeamId: String = ""
    private var awayTeamId: String = ""
    private var homeTeamName: String = ""
    private var awayTeamName: String = ""
    private val highlightRepository = HighlightRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighlightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 값 받아오기
        isHomeTeam = intent.getBooleanExtra(EXTRA_IS_HOME_TEAM, true)
        gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: ""
        homeTeamId = intent.getStringExtra(EXTRA_HOME_TEAM_ID) ?: ""
        awayTeamId = intent.getStringExtra(EXTRA_AWAY_TEAM_ID) ?: ""
        homeTeamName = intent.getStringExtra(EXTRA_HOME_TEAM_NAME) ?: homeTeamId
        awayTeamName = intent.getStringExtra(EXTRA_AWAY_TEAM_NAME) ?: awayTeamId

        setupBackButton()
        loadHighlights()
    }

    private fun loadHighlights() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                when (val result = highlightRepository.getHighlights(gameId)) {
                    is NetworkResult.Success -> {
                        val highlightResponse = result.data
                        setupRecyclerView(highlightResponse.substitutions)
                        showLoading(false)
                    }
                    is NetworkResult.Error -> {
                        Log.e("HighlightActivity", "하이라이트 로딩 실패: ${result.message}")
                        Toast.makeText(this@HighlightActivity, "하이라이트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        showNoHighlights(result.message ?: "알 수 없는 오류가 발생했습니다")
                        showLoading(false)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("HighlightActivity", "예외 발생", e)
                Toast.makeText(this@HighlightActivity, "하이라이트를 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                showNoHighlights("예외가 발생했습니다: ${e.message}")
                showLoading(false)
            }
        }
    }

    private fun setupRecyclerView(substitutions: List<SubstitutionItem>) {
        // 선수 교체 정보 필터링 (HOME/AWAY)
        val filteredSubstitutions = if (isHomeTeam) {
            substitutions.filter { it.team == "HOME" }
        } else {
            substitutions.filter { it.team == "AWAY" }
        }

        if (filteredSubstitutions.isEmpty()) {
            showNoHighlights("하이라이트가 없습니다")
        } else {
            binding.tvNoHighlights.visibility = View.GONE
            binding.rvHighlights.visibility = View.VISIBLE

            binding.rvHighlights.apply {
                layoutManager = LinearLayoutManager(this@HighlightActivity)
                adapter = HighlightAdapter(
                    filteredSubstitutions,
                    isHomeTeam,
                    gameId,
                    homeTeamId,
                    awayTeamId,
                    homeTeamName,
                    awayTeamName
                )
            }
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showNoHighlights(message: String) {
        binding.tvNoHighlights.text = message
        binding.tvNoHighlights.visibility = View.VISIBLE
        binding.rvHighlights.visibility = View.GONE
    }

    inner class HighlightAdapter(
        private val substitutions: List<SubstitutionItem>,
        private val isHomeTeam: Boolean,
        private val gameId: String,
        private val homeTeamId: String,
        private val awayTeamId: String,
        private val homeTeamName: String,
        private val awayTeamName: String
    ) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

        inner class HighlightViewHolder(private val binding: ItemHighlightBinding) :
                RecyclerView.ViewHolder(binding.root) {

                fun bind(substitution: SubstitutionItem) {
                    binding.apply {
                        // 이닝 정보 변환 (예: TOP_3 -> 3회초, BOTTOM_5 -> 5회말)
                        val inningParts = substitution.inning.split("_")
                        val inningNumber = inningParts[1]
                        val inningType = if (inningParts[0] == "TOP") "회초" else "회말"
                        val inningText = "$inningNumber$inningType"

                        tvInning.text = inningText

                        // 팀 이름 (displayName 사용)
                        tvHomeTeamName.text = homeTeamName
                        tvAwayTeamName.text = awayTeamName

                        // 투수와 타자 정보
                        tvHomePlayerInfo.text = substitution.currentHitter.name
                        tvAwayPlayerInfo.text = substitution.currentPitcher.name

                        // 아웃 카운트 표시
                        when (substitution.outCount) {
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

                        // 점수 표시
                        tvHomeScore.text = if (isHomeTeam) substitution.score.home.toString() else substitution.score.away.toString()
                        tvAwayScore.text = if (isHomeTeam) substitution.score.away.toString() else substitution.score.home.toString()

                        // 베이스 상태 표시
                        // TODO: 백엔드 기능이 완성되면 추가될 예정


                        // 팀 로고 이미지 로딩
                        try {
                            // 선수 이미지 URL을 사용하여 팀 로고 대신 표시
                            val currentHitter = substitution.currentHitter
                            val currentPitcher = substitution.currentPitcher

                            // 홈팀/어웨이팀 선수 이미지 로딩
                            GlideApp.with(itemView.context)
                                .load(currentHitter.imageUrl)
                                .placeholder(R.drawable.placeholder_logo)
                                .error(R.drawable.placeholder_logo)
                                .into(ivHomeTeamLogo)

                            GlideApp.with(itemView.context)
                                .load(currentPitcher.imageUrl)
                                .placeholder(R.drawable.placeholder_logo)
                                .error(R.drawable.placeholder_logo)
                                .into(ivAwayTeamLogo)
                        } catch (e: Exception) {
                            Log.e("HighlightAdapter", "이미지 로딩 실패", e)
                        }

                        btnPlay.setOnClickListener {
                            val context = itemView.context
                            val intent = Intent(context, ChangeMemberActivity::class.java).apply {
                                putExtra(ChangeMemberActivity.EXTRA_IS_HOME_TEAM, isHomeTeam)
                                putExtra(ChangeMemberActivity.EXTRA_GAME_ID, gameId)
                                putExtra(ChangeMemberActivity.EXTRA_HOME_TEAM_ID, homeTeamId)
                                putExtra(ChangeMemberActivity.EXTRA_AWAY_TEAM_ID, awayTeamId)
                                putExtra(ChangeMemberActivity.EXTRA_HOME_TEAM_NAME, homeTeamName)
                                putExtra(ChangeMemberActivity.EXTRA_AWAY_TEAM_NAME, awayTeamName)
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
            holder.bind(substitutions[position])
        }

        override fun getItemCount() = substitutions.size
    }

    companion object {
        const val EXTRA_IS_HOME_TEAM = "extra_is_home_team"
        const val EXTRA_GAME_ID = "extra_game_id"
        const val EXTRA_HOME_TEAM_ID = "extra_home_team_id"
        const val EXTRA_AWAY_TEAM_ID = "extra_away_team_id"
        const val EXTRA_HOME_TEAM_NAME = "extra_home_team_name"
        const val EXTRA_AWAY_TEAM_NAME = "extra_away_team_name"
    }
}