package com.example.baseball_simulation_app.ui.main

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.BaseStatus
import com.example.baseball_simulation_app.data.model.HighlightData
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ActivityChangeMemberBinding

class ChangeMemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeMemberBinding
    private lateinit var changeMemberAdapter: ChangePlayerAdapter

    private var isHomeTeam: Boolean = true
    private var gameId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 값 받기
        isHomeTeam = intent.getBooleanExtra(EXTRA_IS_HOME_TEAM, true)
        gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: ""

        setupHighlightData()
        setupRecyclerView()
        setupListeners()

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupHighlightData() {
        // 하이라이트 데이터 설정 (더미 데이터 사용)
        val highlight = getDummyHighlightData()

        // highlightSection은 include된 item_highlight.xml의 참조
        with(binding.highlightSection) {
            // 이닝 정보
            tvInning.text = highlight.inning

            // 팀 정보
            tvHomeTeamName.text = highlight.homeTeamName
            tvAwayTeamName.text = highlight.awayTeamName

            // 점수 정보 (표시하지 않을 경우 주석 처리)
            // tvHomeScore.text = "0"
            // tvAwayScore.text = "0"

            // 선수 정보
            tvHomePlayerInfo.text = highlight.batterName
            tvAwayPlayerInfo.text = highlight.pitcherName

            // 로고 이미지
            Glide.with(this@ChangeMemberActivity)
                .load(highlight.homeTeamLogoRes)
                .placeholder(R.drawable.placeholder_logo)
                .into(ivHomeTeamLogo)

            Glide.with(this@ChangeMemberActivity)
                .load(highlight.awayTeamLogoRes)
                .placeholder(R.drawable.placeholder_logo)
                .into(ivAwayTeamLogo)

            // 베이스 상태
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

            // 아웃 카운트
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

            // PLAY 버튼 숨기기 (이미 플레이 중이므로)
            btnPlay.visibility = android.view.View.GONE
        }
    }

    private fun getDummyHighlightData(): HighlightData {
        return if (isHomeTeam) {
            HighlightData(
                inning = "5회초",
                homeTeamName = "KT",
                homeTeamLogoRes = R.drawable.placeholder_logo,
                awayTeamName = "LG",
                awayTeamLogoRes = R.drawable.placeholder_logo,
                batterName = "강백호",
                pitcherName = "고우석",
                baseStatus = BaseStatus(first = true, second = false, third = true),
                outCount = 1
            )
        } else {
            HighlightData(
                inning = "7회말",
                homeTeamName = "KT",
                homeTeamLogoRes = R.drawable.placeholder_logo,
                awayTeamName = "LG",
                awayTeamLogoRes = R.drawable.placeholder_logo,
                batterName = "김현수",
                pitcherName = "벤자민",
                baseStatus = BaseStatus(first = false, second = true, third = false),
                outCount = 0
            )
        }
    }

    private fun setupRecyclerView() {
        val dummyMembers = loadDummyCandidates("teamId", isHomeTeam, gameId)
        changeMemberAdapter = ChangePlayerAdapter(dummyMembers)
        binding.rvChangeMembers.apply {
            layoutManager = LinearLayoutManager(this@ChangeMemberActivity)
            adapter = changeMemberAdapter
        }
    }

    private fun loadDummyCandidates(teamId: String, isHomeTeam: Boolean, gameId: String): List<Player> {
        return listOf(
            Player(
                name = "김하성",
                position = "유격수",
                playerImageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Son_Ah-Seop2023.webp",
                isHomeTeam = isHomeTeam,
                battingAverage = 0.315,
                hits = 150,
                onBasePercentage = 0.390,
                homeRuns = 10,
                sluggingPercentage = 0.450,
                rbi = 70
            ),
            Player(
                name = "박병호",
                position = "1루수",
                playerImageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Son_Ah-Seop2023.webp",
                isHomeTeam = isHomeTeam,
                battingAverage = 0.270,
                hits = 130,
                onBasePercentage = 0.350,
                homeRuns = 25,
                sluggingPercentage = 0.520,
                rbi = 90
            )
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
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