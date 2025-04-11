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
    private var inning: String = ""

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
        inning = intent.getStringExtra(EXTRA_INNING) ?: ""

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
//            Glide.with(this@ChangeMemberActivity)
//                .load(homeLogoUrl)
//                .placeholder(R.drawable.placeholder_logo)
//                .into(ivHomeTeamLogo)
//
//            Glide.with(this@ChangeMemberActivity)
//                .load(awayLogoUrl)
//                .placeholder(R.drawable.placeholder_logo)
//                .into(ivAwayTeamLogo)
        }
    }

    private fun setupRecyclerViewWithApiData(teamLineup: TeamLineup) {
        val players = convertApiPlayersToAppPlayers(teamLineup, inning)
        changeMemberAdapter = ChangePlayerAdapter(players)
        binding.rvChangeMembers.apply {
            layoutManager = LinearLayoutManager(this@ChangeMemberActivity)
            adapter = changeMemberAdapter
        }
    }

    private fun convertApiPlayersToAppPlayers(teamLineup: TeamLineup, inning: String): List<Player> {
        val players = mutableListOf<Player>()

        // 현재 투수/타자 정보
        val currentPitcherName = intent.getStringExtra(EXTRA_PITCHER_NAME) ?: ""
        val currentBatterName = intent.getStringExtra(EXTRA_BATTER_NAME) ?: ""

        // 이닝 정보로 공격/수비 상황 판단
        val isOffense = isOffenseSituation(inning, isHomeTeam)
        Log.d("FilterPlayers", "공격 상황: $isOffense, 이닝: $inning, 홈팀: $isHomeTeam")

        // 공격 상황에서는 타자만, 수비 상황에서는 투수만 필터링
        val filteredPlayers = if (isOffense) {
            // 공격 상황: 타자만 필터링 (벤치 선수 중 투수가 아닌 선수)
            val batters = mutableListOf<com.example.baseball_simulation_app.API.Player>()

            // 벤치 선수만 가져옴 (현재 타자 제외)
            batters.addAll(teamLineup.benchLineups.filter { player ->
                val isNotPitcher = player.position?.none { it.contains("투수") } ?: true
                val isNotCurrentBatter = player.name != currentBatterName
                isNotPitcher && isNotCurrentBatter
            })

            batters
        } else {
            // 수비 상황: 투수만 필터링 (불펜 투수만, 현재 투수는 제외)
            teamLineup.bullpenLineups.filter { player ->
                player.name != currentPitcherName
            }
        }

        Log.d("FilterPlayers", "필터링된 선수 수: ${filteredPlayers.size}")
        Log.d("FilterPlayers", "필터링된 선수 목록: ${filteredPlayers.map { it.name }}")

        // API 응답 선수를 앱 모델로 변환
        filteredPlayers.forEach { apiPlayer ->
            try {
                val player = Player(
                    name = apiPlayer.name,
                    position = apiPlayer.position?.joinToString("/") ?: "미정",
                    playerImageUrl = apiPlayer.stats?.imageUrl ?: "", // 선수 이미지 URL
                    isHomeTeam = isHomeTeam,

                    // 타자 스탯
                    battingAverage = safeStringToDouble(apiPlayer.stats?.battingAverage),
                    hits = safeStringToInt(apiPlayer.stats?.hits),
                    onBasePercentage = safeStringToDouble(apiPlayer.stats?.onBasePercentage),
                    homeRuns = safeStringToInt(apiPlayer.stats?.homeRuns),
                    sluggingPercentage = safeStringToDouble(apiPlayer.stats?.sluggingPercentage),
                    rbi = safeStringToInt(apiPlayer.stats?.rbi),

                    // 투수 스탯
                    era = safeStringToDouble(apiPlayer.stats?.era),
                    inningsPitched = apiPlayer.stats?.innings,
                    wins = safeStringToInt(apiPlayer.stats?.wins),
                    losses = safeStringToInt(apiPlayer.stats?.losses),
                    holds = safeStringToInt(apiPlayer.stats?.holds),
                    saves = safeStringToInt(apiPlayer.stats?.saves)
                )
                players.add(player)

                // 선수 이미지 URL 로그
                Log.d("ChangeMemberActivity", "선수 ${player.name} 이미지: ${player.playerImageUrl}")
            } catch (e: Exception) {
                Log.e("ChangeMemberActivity", "선수 변환 오류: ${e.message}", e)
            }
        }

        return players
    }

    /**
     * 이닝 정보와 팀 정보를 바탕으로 현재 공격 상황인지 판단
     * @param inning 이닝 정보 (예: "3회초" 또는 "5회말")
     * @param isHomeTeam 홈팀 여부
     * @return 공격 상황이면 true, 수비 상황이면 false
     */
    private fun isOffenseSituation(inning: String, isHomeTeam: Boolean): Boolean {
        // 이닝이 비어있으면 기본적으로 원래 로직대로 처리
        if (inning.isEmpty()) return isHomeTeam

        // "회초"는 원정팀 공격, "회말"은 홈팀 공격
        val isAwayTeamOffense = inning.contains("회초")

        // 홈팀이면서 원정팀 공격 상황이면 수비(false)
        // 홈팀이면서 홈팀 공격 상황이면 공격(true)
        // 원정팀이면서 원정팀 공격 상황이면 공격(true)
        // 원정팀이면서 홈팀 공격 상황이면 수비(false)
        return if (isHomeTeam) !isAwayTeamOffense else isAwayTeamOffense
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

    private fun setupHighlightData(inning: String, outCount: Int, homeScore: Int, awayScore: Int,
                                  pitcherName: String, batterName: String) {
        binding.highlightSection.apply {
            // 이닝과 점수 설정
            tvInning.text = inning
            tvHomeTeamName.text = homeTeamName
            tvAwayTeamName.text = awayTeamName
            tvHomeScore.text = homeScore.toString()
            tvAwayScore.text = awayScore.toString()

            // 선수 이름 표시
            if (isHomeTeam) {
                tvAwayPlayerInfo.text = pitcherName  // 어웨이팀 투수
                tvHomePlayerInfo.text = batterName    // 홈팀 타자
            } else {
                tvHomePlayerInfo.text = batterName   // 홈팀 투수
                tvAwayPlayerInfo.text = pitcherName   // 어웨이팀 타자
            }

            // 선수 이미지 URL 가져오기
            val pitcherImageUrl = intent.getStringExtra(EXTRA_PITCHER_IMAGE_URL)
            val batterImageUrl = intent.getStringExtra(EXTRA_BATTER_IMAGE_URL)

            // 선수 이미지 설정 (팀 로고 대신)
            Glide.with(this@ChangeMemberActivity)
                .load(batterImageUrl)
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .into(ivHomeTeamLogo)

            Glide.with(this@ChangeMemberActivity)
                .load(pitcherImageUrl)
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .into(ivAwayTeamLogo)

            // 아웃 카운트 설정
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
        const val EXTRA_PITCHER_IMAGE_URL = "extra_pitcher_image_url"
        const val EXTRA_BATTER_IMAGE_URL = "extra_batter_image_url"
    }
}