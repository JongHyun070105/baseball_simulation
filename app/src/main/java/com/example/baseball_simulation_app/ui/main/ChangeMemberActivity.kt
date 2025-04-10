package com.example.baseball_simulation_app.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.API.TeamLineup
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.client.RetrofitClient
import com.example.baseball_simulation_app.data.model.BaseStatus
import com.example.baseball_simulation_app.data.model.HighlightData
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ActivityChangeMemberBinding
import kotlinx.coroutines.launch

class ChangeMemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeMemberBinding
    private lateinit var changeMemberAdapter: ChangePlayerAdapter

    private var isHomeTeam: Boolean = true
    private var gameId: String = ""
    private var homeTeamName: String = ""
    private var awayTeamName: String = ""

    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 값 받기
        isHomeTeam = intent.getBooleanExtra(EXTRA_IS_HOME_TEAM, true)
        gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: ""
        homeTeamName = intent.getStringExtra(EXTRA_HOME_TEAM_NAME) ?: ""
        awayTeamName = intent.getStringExtra(EXTRA_AWAY_TEAM_NAME) ?: ""

        // 하이라이트 데이터 받기
        val inning = intent.getStringExtra(EXTRA_INNING) ?: ""
        val outCount = intent.getIntExtra(EXTRA_OUT_COUNT, 0)
        val homeScore = intent.getIntExtra(EXTRA_HOME_SCORE, 0)
        val awayScore = intent.getIntExtra(EXTRA_AWAY_SCORE, 0)
        val pitcherName = intent.getStringExtra(EXTRA_PITCHER_NAME) ?: ""
        val batterName = intent.getStringExtra(EXTRA_BATTER_NAME) ?: ""

        // 하이라이트 데이터 설정
        setupHighlightData(inning, outCount, homeScore, awayScore, pitcherName, batterName)
        setupListeners()

        // API 데이터 로드
        loadLineupData()

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun loadLineupData() {
        lifecycleScope.launch {
            try {
                val response = apiService.getLineups(gameId)
                if (response.isSuccessful && response.body() != null) {
                    val lineupData = response.body()!!

                    // 팀 정보 업데이트
                    updateTeamInfo(lineupData.home.team.name, lineupData.home.team.logoUrl,
                        lineupData.away.team.name, lineupData.away.team.logoUrl)

                    // 선수 목록 설정
                    val teamLineup = if (isHomeTeam) lineupData.home else lineupData.away
                    setupRecyclerViewWithApiData(teamLineup)
                } else {
                    Log.e("API_ERROR", "코드: ${response.code()}, 메시지: ${response.message()}")
                    Toast.makeText(this@ChangeMemberActivity,
                        "데이터를 가져오지 못했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT).show()

                    // API 호출 실패 시 더미 데이터 사용
                    setupRecyclerViewWithDummyData()
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "오류: ${e.message}", e)
                Toast.makeText(this@ChangeMemberActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT).show()

                // 예외 발생 시 더미 데이터 사용
                setupRecyclerViewWithDummyData()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateTeamInfo(homeName: String, homeLogoUrl: String,
                               awayName: String, awayLogoUrl: String) {
        with(binding.highlightSection) {
            tvHomeTeamName.text = homeName
            tvAwayTeamName.text = awayName

            // 팀 로고 로딩
            Glide.with(this@ChangeMemberActivity)
                .load(homeLogoUrl)
                .placeholder(R.drawable.placeholder_logo)
                .into(ivHomeTeamLogo)

            Glide.with(this@ChangeMemberActivity)
                .load(awayLogoUrl)
                .placeholder(R.drawable.placeholder_logo)
                .into(ivAwayTeamLogo)
        }
    }

    private fun setupRecyclerViewWithApiData(teamLineup: TeamLineup) {
        val players = convertApiPlayersToAppPlayers(teamLineup)
        changeMemberAdapter = ChangePlayerAdapter(players)
        binding.rvChangeMembers.apply {
            layoutManager = LinearLayoutManager(this@ChangeMemberActivity)
            adapter = changeMemberAdapter
        }
    }

    private fun convertApiPlayersToAppPlayers(teamLineup: TeamLineup): List<Player> {
        Log.d("ChangeMemberActivity", teamLineup.team.name)
        val players = mutableListOf<Player>()

        // 선발, 벤치, 불펜 선수를 모두 리스트에 추가
        val allPlayers = mutableListOf<com.example.baseball_simulation_app.API.Player>().apply {
            addAll(teamLineup.startingLineups)
            addAll(teamLineup.benchLineups)
            addAll(teamLineup.bullpenLineups)
        }

        allPlayers.forEach { apiPlayer ->
            try {
                players.add(
                    Player(
                        name = apiPlayer.name,
                        position = apiPlayer.position?.joinToString("/") ?: "미정",
                        playerImageUrl = apiPlayer.imageUrl ?: "",
                        isHomeTeam = isHomeTeam,

                        // 안전한 변환 함수 사용
                        battingAverage = safeStringToDouble(apiPlayer.stats?.battingAverage),
                        hits = safeStringToInt(apiPlayer.stats?.hits),
                        onBasePercentage = safeStringToDouble(apiPlayer.stats?.onBasePercentage),
                        homeRuns = safeStringToInt(apiPlayer.stats?.homeRuns),
                        sluggingPercentage = safeStringToDouble(apiPlayer.stats?.sluggingPercentage),
                        rbi = safeStringToInt(apiPlayer.stats?.rbi),

                        era = safeStringToDouble(apiPlayer.stats?.era),
                        inningsPitched = safeStringToDouble(apiPlayer.stats?.innings),
                        wins = safeStringToInt(apiPlayer.stats?.wins),
                        losses = safeStringToInt(apiPlayer.stats?.losses),
                        holds = safeStringToInt(apiPlayer.stats?.holds),
                        saves = safeStringToInt(apiPlayer.stats?.saves)
                    )
                )
            } catch (e: Exception) {
                Log.e("ChangeMemberActivity", "선수 변환 오류: ${e.message}", e)
            }
        }

        return players
    }

    private fun safeStringToDouble(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        return try {
            value.replace(",", ".").trim().toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun safeStringToInt(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        return try {
            value.replace("[^0-9-]".toRegex(), "").trim().toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun setupRecyclerViewWithDummyData() {
        val dummyMembers = loadDummyCandidates("teamId", isHomeTeam, gameId)
        changeMemberAdapter = ChangePlayerAdapter(dummyMembers)
        binding.rvChangeMembers.apply {
            layoutManager = LinearLayoutManager(this@ChangeMemberActivity)
            adapter = changeMemberAdapter
        }
    }

    private fun setupHighlightData(inning: String, outCount: Int, homeScore: Int, awayScore: Int, pitcherName: String, batterName: String) {
        binding.highlightSection.apply {
            // 이닝 설정
            tvInning.text = inning

            // 팀 이름 설정
            tvHomeTeamName.text = homeTeamName
            tvAwayTeamName.text = awayTeamName

            // 점수 설정
            tvHomeScore.text = homeScore.toString()
            tvAwayScore.text = awayScore.toString()

            if (isHomeTeam) {
                tvAwayPlayerInfo.text = pitcherName  // 어웨이팀 투수
                tvHomePlayerInfo.text = batterName    // 홈팀 타자
            } else {
                tvHomePlayerInfo.text = batterName   // 홈팀 투수
                tvAwayPlayerInfo.text = pitcherName   // 어웨이팀 타자
            }

            // 아웃카운트 설정
            when (outCount) {
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
        const val EXTRA_INNING = "extra_inning"
        const val EXTRA_OUT_COUNT = "extra_out_count"
        const val EXTRA_HOME_SCORE = "extra_home_score"
        const val EXTRA_AWAY_SCORE = "extra_away_score"
        const val EXTRA_PITCHER_NAME = "extra_pitcher_name"
        const val EXTRA_BATTER_NAME = "extra_batter_name"
    }
}