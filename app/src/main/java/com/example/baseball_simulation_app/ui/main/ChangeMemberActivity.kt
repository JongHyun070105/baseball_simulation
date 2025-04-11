package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.API.LineupResponse // Import LineupResponse
import com.example.baseball_simulation_app.API.Player // Import API Player
import com.example.baseball_simulation_app.API.TeamLineup
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.client.RetrofitClient
import com.example.baseball_simulation_app.data.model.BaseStatus // BaseStatus 추가
import com.example.baseball_simulation_app.data.model.Player as AppPlayer // Alias App Player model
import com.example.baseball_simulation_app.databinding.ActivityChangeMemberBinding
import kotlinx.coroutines.launch

class ChangeMemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeMemberBinding
    private lateinit var changeMemberAdapter: ChangePlayerAdapter

    private var isHomeTeam: Boolean = true // 현재 사용자가 보고 있는 팀이 홈팀인지 여부 (Intent로 받음)
    private var gameId: String = ""
    private var homeTeamName: String = "" // API에서 가져올 최종 '홈' 팀 이름
    private var awayTeamName: String = "" // API에서 가져올 최종 '어웨이' 팀 이름
    private var inning: String = ""
    private var currentHomeScore: Int = 0 // 하이라이트 시점 '홈팀' 점수
    private var currentAwayScore: Int = 0 // 하이라이트 시점 '어웨이팀' 점수


    // Hold full lineup data
    private var fullLineupData: LineupResponse? = null

    // Hold fully populated current players for comparison
    private var currentPitcherPlayer: AppPlayer? = null
    private var currentBatterPlayer: AppPlayer? = null
    private var isOffenseSituation: Boolean = false

    // Hold initial names/images from intent used to FIND current players
    private var initialPitcherName: String = ""
    private var initialBatterName: String = ""
    private var initialPitcherImageUrl: String = ""
    private var initialBatterImageUrl: String = ""

    // Hold initial names/images from HighlightActivity intent for the highlight section display
    private var highlightPitcherName: String = ""
    private var highlightBatterName: String = ""
    private var highlightPitcherImageUrl: String = ""
    private var highlightBatterImageUrl: String = ""
    private var highlightBaseStatus: BaseStatus? = null // 하이라이트 시점 베이스 상태

    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 기본 값 받기
        isHomeTeam = intent.getBooleanExtra(EXTRA_IS_HOME_TEAM, true) // 사용자가 홈팀 로고를 클릭했는지 여부
        gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: ""
        // 초기 팀 이름 (API 응답 후 실제 이름으로 업데이트됨)
        val initialHomeTeamName = intent.getStringExtra(EXTRA_HOME_TEAM_NAME) ?: "홈팀"
        val initialAwayTeamName = intent.getStringExtra(EXTRA_AWAY_TEAM_NAME) ?: "원정팀"
        this.homeTeamName = initialHomeTeamName // 우선 초기값 저장 (API 로드 후 덮어씀)
        this.awayTeamName = initialAwayTeamName // 우선 초기값 저장 (API 로드 후 덮어씀)

        // 하이라이트 데이터 받기
        inning = intent.getStringExtra(EXTRA_INNING) ?: ""
        val outCount = intent.getIntExtra(EXTRA_OUT_COUNT, 0)
        currentHomeScore = intent.getIntExtra(EXTRA_HOME_SCORE, 0) // 멤버 변수에 저장
        currentAwayScore = intent.getIntExtra(EXTRA_AWAY_SCORE, 0) // 멤버 변수에 저장
        // BaseStatus 타입으로 안전하게 변환 시도
        highlightBaseStatus = try {
            intent.getSerializableExtra(EXTRA_BASE_STATUS) as? BaseStatus
        } catch (e: Exception) {
            Log.e("ChangeMemberActivity", "Error reading BaseStatus from intent", e)
            null
        }

        // Store initial player info *specifically for the highlight section display*
        highlightPitcherName = intent.getStringExtra(EXTRA_PITCHER_NAME) ?: "?"
        highlightBatterName = intent.getStringExtra(EXTRA_BATTER_NAME) ?: "?"
        highlightPitcherImageUrl = intent.getStringExtra(EXTRA_PITCHER_IMAGE_URL) ?: ""
        highlightBatterImageUrl = intent.getStringExtra(EXTRA_BATTER_IMAGE_URL) ?: ""

        // Store initial names/images used to *find* the full current player data from lineup
        initialPitcherName = highlightPitcherName
        initialBatterName = highlightBatterName
        initialPitcherImageUrl = highlightPitcherImageUrl
        initialBatterImageUrl = highlightBatterImageUrl

        // Determine offense/defense situation early based on inning and USER'S selected team (isHomeTeam)
        isOffenseSituation = isOffenseSituation(inning, isHomeTeam)

        // 하이라이트 섹션 설정 (API 로드 전 초기 정보 사용, 여기서 점수/팀명 직접 할당)
        setupHighlightData(inning, outCount, currentHomeScore, currentAwayScore,
            highlightPitcherName, highlightBatterName,
            highlightPitcherImageUrl, highlightBatterImageUrl,
            highlightBaseStatus)
        setupListeners()

        // API 데이터 로드 시작 (이후 updateTeamInfo 호출하여 실제 팀 이름 업데이트)
        loadLineupData()

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    // 라인업 데이터에서 현재 투수/타자를 찾아 AppPlayer 객체로 설정
    private fun findAndSetCurrentPlayersFromLineup(lineupData: LineupResponse) {
        val homeLineup = lineupData.home
        val awayLineup = lineupData.away

        // API 라인업 전체에서 초기 이름과 일치하는 선수 검색 (시작, 벤치, 불펜 모두)
        // 투수는 상대팀 라인업에서 먼저 찾고, 타자는 우리팀 라인업에서 먼저 찾음 (일반적인 경우)
        val apiPitcher = findApiPlayer(initialPitcherName, if (isHomeTeam) awayLineup else homeLineup)
            ?: findApiPlayer(initialPitcherName, if (isHomeTeam) homeLineup else awayLineup) // 만약 같은팀 선수면 여기서 찾음
        val apiBatter = findApiPlayer(initialBatterName, if (isHomeTeam) homeLineup else awayLineup)
            ?: findApiPlayer(initialBatterName, if (isHomeTeam) awayLineup else homeLineup) // 만약 상대팀 선수면 여기서 찾음


        // 찾은 API 선수 정보를 AppPlayer 모델로 변환하여 저장 (선수의 실제 소속팀 기준)
        currentPitcherPlayer = if (apiPitcher != null) {
            Log.d("ChangeMemberActivity", "Found full data for pitcher: ${apiPitcher.name}")
            // apiPitcher가 homeLineup에서 찾아졌는지 awayLineup에서 찾아졌는지 확인하여 isHomeTeam 설정 필요
            val pitcherIsActuallyHome = lineupData.home.startingLineups.any { it.name == apiPitcher.name } ||
                    lineupData.home.benchLineups.any { it.name == apiPitcher.name } ||
                    lineupData.home.bullpenLineups.any { it.name == apiPitcher.name }
            convertApiPlayerToAppPlayer(apiPitcher, pitcherIsActuallyHome)
        } else {
            Log.w("ChangeMemberActivity", "Could not find pitcher '$initialPitcherName' in lineup data. Using partial data.")
            // 찾지 못했을 경우, 초기 정보로 최소한의 AppPlayer 생성 (상대팀으로 가정)
            AppPlayer(name = initialPitcherName, position = "투수?", playerImageUrl = initialPitcherImageUrl, isHomeTeam = !isHomeTeam)
        }

        currentBatterPlayer = if (apiBatter != null) {
            Log.d("ChangeMemberActivity", "Found full data for batter: ${apiBatter.name}")
            // apiBatter가 homeLineup에서 찾아졌는지 awayLineup에서 찾아졌는지 확인하여 isHomeTeam 설정 필요
            val batterIsActuallyHome = lineupData.home.startingLineups.any { it.name == apiBatter.name } ||
                    lineupData.home.benchLineups.any { it.name == apiBatter.name } ||
                    lineupData.home.bullpenLineups.any { it.name == apiBatter.name }
            convertApiPlayerToAppPlayer(apiBatter, batterIsActuallyHome)
        } else {
            Log.w("ChangeMemberActivity", "Could not find batter '$initialBatterName' in lineup data. Using partial data.")
            // 찾지 못했을 경우, 초기 정보로 최소한의 AppPlayer 생성 (우리팀으로 가정)
            AppPlayer(name = initialBatterName, position = "타자?", playerImageUrl = initialBatterImageUrl, isHomeTeam = isHomeTeam)
        }


        // 변환된 선수 정보 로그 확인
        Log.d("ChangeMemberActivity", "Current Pitcher Set: ${currentPitcherPlayer?.name} (Home: ${currentPitcherPlayer?.isHomeTeam}), ERA: ${currentPitcherPlayer?.era}")
        Log.d("ChangeMemberActivity", "Current Batter Set: ${currentBatterPlayer?.name} (Home: ${currentBatterPlayer?.isHomeTeam}), AVG: ${currentBatterPlayer?.battingAverage}")

        // 현재 선수 정보가 설정된 후, 리사이클러뷰 어댑터 설정 진행
        setupRecyclerView()
    }

    // 팀 라인업 내에서 이름으로 API Player 객체 찾기 (시작, 벤치, 불펜 순서)
    private fun findApiPlayer(name: String, teamLineup: TeamLineup): com.example.baseball_simulation_app.API.Player? {
        if (name.isBlank() || name == "?") return null // 유효하지 않은 이름은 검색하지 않음
        return teamLineup.startingLineups.find { it.name == name }
            ?: teamLineup.benchLineups.find { it.name == name }
            ?: teamLineup.bullpenLineups.find { it.name == name }
    }

    // API를 호출하여 전체 라인업 데이터 로드
    private fun loadLineupData() {
        binding.progressBar.visibility = View.VISIBLE // 로딩 시작 시 프로그레스 바 표시
        lifecycleScope.launch {
            try {
                val response = apiService.getLineups(gameId)
                if (response.isSuccessful && response.body() != null) {
                    fullLineupData = response.body()!! // 전체 라인업 데이터 저장
                    Log.d("ChangeMemberActivity", "Lineup data loaded successfully for game $gameId")

                    // API 응답에서 실제 팀 이름 가져와 업데이트 (setupHighlightData 이후 호출될 수 있음)
                    updateTeamInfo(fullLineupData!!.home.team.name, fullLineupData!!.away.team.name)

                    // 저장된 전체 라인업 데이터를 사용하여 현재 투수/타자 정보 설정
                    findAndSetCurrentPlayersFromLineup(fullLineupData!!)

                    // setupRecyclerView는 findAndSetCurrentPlayersFromLineup 내부에서 호출됨

                } else {
                    Log.e("API_ERROR", "Failed to load lineup. Code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(this@ChangeMemberActivity,
                        "라인업 데이터를 가져오지 못했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT).show()
                    setupRecyclerViewWithDummyData() // API 실패 시 더미 데이터 사용
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Exception during lineup fetch: ${e.message}", e)
                Toast.makeText(this@ChangeMemberActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                setupRecyclerViewWithDummyData() // 예외 발생 시 더미 데이터 사용
            } finally {
                binding.progressBar.visibility = View.GONE // 로딩 완료 시 프로그레스 바 숨김
            }
        }
    }

    // API 응답에서 받은 팀 정보로 UI 업데이트 (팀 이름만 업데이트)
    private fun updateTeamInfo(homeName: String, awayName: String) {
        // 멤버 변수에 최종 팀 이름 저장
        this.homeTeamName = homeName
        this.awayTeamName = awayName

        // 하이라이트 섹션의 팀 이름 재설정 (레이아웃 ID 수정되었으므로 직접 할당)
        binding.highlightSection.apply {
            tvHomeTeamName.text = this@ChangeMemberActivity.homeTeamName // 오른쪽 TextView에 홈 이름
            tvAwayTeamName.text = this@ChangeMemberActivity.awayTeamName // 왼쪽에 어웨이 이름
        }

        Log.d("ChangeMemberActivity", "Team names updated in highlight: Left=${this.awayTeamName}, Right=${this.homeTeamName}")
    }

    // 리사이클러뷰 설정 (API 데이터 기반)
    private fun setupRecyclerView() {
        if (fullLineupData == null) {
            Log.e("ChangeMemberActivity", "Cannot setup RecyclerView: fullLineupData is null")
            Toast.makeText(this, "라인업 정보가 없어 선수 목록을 표시할 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        // 현재 사용자가 선택한 팀(isHomeTeam)의 라인업 선택
        val teamLineup = if (isHomeTeam) fullLineupData!!.home else fullLineupData!!.away
        // 해당 라인업에서 교체 가능한 선수 목록 생성
        val players = convertApiPlayersToAppPlayers(teamLineup)
        // 생성된 선수 목록으로 어댑터 설정
        setupAdapter(players)
    }

    // 선수 목록으로 리사이클러뷰 어댑터 설정
    private fun setupAdapter(players: List<AppPlayer>) {
        // 어댑터 생성 전, 비교 대상 선수 정보가 준비되었는지 확인
        if (currentBatterPlayer == null || currentPitcherPlayer == null) {
            Log.w("ChangeMemberActivity", "Cannot setup Adapter: current players not fully resolved yet.")
            // Toast.makeText(this, "선수 정보를 로딩 중입니다...", Toast.LENGTH_SHORT).show() // API 로드 중일 수 있으므로 잠시 대기
            return // 선수 정보 없으면 어댑터 설정 중단
        }

        // 어댑터 생성: 교체 선수 목록, 현재 상황, 비교 대상 선수, 게임/하이라이트 정보 전달
        changeMemberAdapter = ChangePlayerAdapter(
            players = players,
            isOffense = isOffenseSituation, // 사용자의 팀이 공격/수비 상황인지 여부
            currentPitcher = currentPitcherPlayer, // 비교용 현재 투수 (Full Data)
            currentBatter = currentBatterPlayer,   // 비교용 현재 타자 (Full Data)
            // MyPickActivity로 전달할 게임/하이라이트 정보
            gameId = gameId,
            homeTeamName = this.homeTeamName, // API에서 받은 최종 '홈' 이름
            awayTeamName = this.awayTeamName, // API에서 받은 최종 '어웨이' 이름
            inning = inning,
            outCount = intent.getIntExtra(EXTRA_OUT_COUNT, 0), // 최신 상태 반영
            homeScore = currentHomeScore, // 저장된 홈 스코어 전달
            awayScore = currentAwayScore, // 저장된 어웨이 스코어 전달
            selectedTeamIsHome = isHomeTeam, // 사용자가 선택한 팀이 홈팀인지 여부
            // MyPickActivity의 하이라이트 섹션용 원본 정보 (이름/이미지)
            highlightBatterName = highlightBatterName,
            highlightPitcherName = highlightPitcherName,
            highlightBatterImageUrl = highlightBatterImageUrl,
            highlightPitcherImageUrl = highlightPitcherImageUrl,
            highlightBaseStatus = highlightBaseStatus
        )

        // 리사이클러뷰에 어댑터 및 레이아웃 매니저 설정
        binding.rvChangeMembers.apply {
            layoutManager = LinearLayoutManager(this@ChangeMemberActivity)
            adapter = changeMemberAdapter
        }
        Log.d("ChangeMemberActivity", "RecyclerView Adapter setup complete with ${players.size} players.")
    }

    // API 팀 라인업 정보를 앱에서 사용할 선수 목록(AppPlayer)으로 변환
    private fun convertApiPlayersToAppPlayers(teamLineup: TeamLineup): List<AppPlayer> {
        val players = mutableListOf<AppPlayer>()
        val currentTeamIsHome = (teamLineup.team.code == fullLineupData?.home?.team?.code) // 이 라인업이 홈팀 라인업인지 확인 (ID 비교)

        Log.d("FilterPlayers", "Converting players. Offense Situation: $isOffenseSituation, Team: ${if (currentTeamIsHome) "Home" else "Away"}")

        // 공격/수비 상황에 따라 교체 가능한 선수 목록 필터링
        val candidateApiPlayers: List<com.example.baseball_simulation_app.API.Player> = if (isOffenseSituation) {
            // 공격 시: 벤치 선수 중 (투수 아니고) && (현재 타자 아닌) 선수
            teamLineup.benchLineups.filter { player ->
                val isPitcher = player.position?.any { it.contains("투수", ignoreCase = true) } == true
                // 현재 타자가 이 팀 소속이고 이름이 같은지 확인
                val isCurrentBatter = player.name == currentBatterPlayer?.name && currentBatterPlayer?.isHomeTeam == currentTeamIsHome
                !isPitcher && !isCurrentBatter
            }
        } else {
            // 수비 시: 불펜 선수 중 (현재 투수 아닌) 선수
            teamLineup.bullpenLineups.filter { player ->
                // 현재 투수가 이 팀 소속이고 이름이 같은지 확인
                val isCurrentPitcher = player.name == currentPitcherPlayer?.name && currentPitcherPlayer?.isHomeTeam == currentTeamIsHome
                !isCurrentPitcher
            }
        }

        Log.d("FilterPlayers", "Filtered API candidate count: ${candidateApiPlayers.size}")
        Log.d("FilterPlayers", "Filtered API candidates: ${candidateApiPlayers.map { it.name }}")

        // 필터링된 API 선수들을 AppPlayer 모델로 변환하여 리스트에 추가
        candidateApiPlayers.forEach { apiPlayer ->
            players.add(convertApiPlayerToAppPlayer(apiPlayer, currentTeamIsHome)) // 선수의 실제 소속팀 정보 전달
        }

        Log.d("ChangeMemberActivity", "Converted ${players.size} AppPlayers for adapter.")
        return players
    }

    // API Player 객체를 AppPlayer 객체로 변환하는 공통 함수
    private fun convertApiPlayerToAppPlayer(apiPlayer: com.example.baseball_simulation_app.API.Player, playerIsActuallyHomeTeam: Boolean): AppPlayer {
        try {
            return AppPlayer(
                name = apiPlayer.name,
                position = apiPlayer.position?.joinToString("/") ?: "미정",
                playerImageUrl = apiPlayer.stats?.imageUrl ?: "",
                isHomeTeam = playerIsActuallyHomeTeam, // 선수의 실제 소속팀 정보 사용

                // 타자 스탯 (안전하게 변환)
                battingAverage = safeStringToDouble(apiPlayer.stats?.battingAverage),
                hits = safeStringToInt(apiPlayer.stats?.hits),
                onBasePercentage = safeStringToDouble(apiPlayer.stats?.onBasePercentage),
                homeRuns = safeStringToInt(apiPlayer.stats?.homeRuns),
                sluggingPercentage = safeStringToDouble(apiPlayer.stats?.sluggingPercentage),
                rbi = safeStringToInt(apiPlayer.stats?.rbi),

                // 투수 스탯 (안전하게 변환)
                era = safeStringToDouble(apiPlayer.stats?.era),
                inningsPitched = apiPlayer.stats?.innings, // 문자열 그대로 저장 가능하면 유지, 아니면 Double 변환
                wins = safeStringToInt(apiPlayer.stats?.wins),
                losses = safeStringToInt(apiPlayer.stats?.losses),
                holds = safeStringToInt(apiPlayer.stats?.holds),
                saves = safeStringToInt(apiPlayer.stats?.saves)
            )
        } catch (e: Exception) {
            Log.e("PlayerConversion", "Error converting API player ${apiPlayer.name}: ${e.message}", e)
            // 변환 오류 시 최소 정보만 가진 객체 반환
            return AppPlayer(name = apiPlayer.name, position = "변환 오류", isHomeTeam = playerIsActuallyHomeTeam)
        }
    }

    // 이닝 정보와 현재 사용자가 보고 있는 팀(isHomeTeam) 기준으로 공격/수비 상황 판단
    private fun isOffenseSituation(inning: String, userViewingHomeTeam: Boolean): Boolean {
        if (inning.isBlank()) {
            Log.w("isOffenseSituation", "Inning is blank, defaulting to defense (false)")
            return false // 이닝 정보 없으면 기본값 (수비)
        }

        val isAwayTeamTurn = inning.contains("회초") // 원정팀 공격 이닝
        val isHomeTeamTurn = inning.contains("회말") // 홈팀 공격 이닝

        if (!isAwayTeamTurn && !isHomeTeamTurn) {
            Log.w("isOffenseSituation", "Inning format unclear ('$inning'), defaulting to defense (false)")
            return false
        }

        // 사용자가 홈팀을 보고 있을 때 홈팀 공격 이닝이면 공격 상황
        // 사용자가 원정팀을 보고 있을 때 원정팀 공격 이닝이면 공격 상황
        val offense = (userViewingHomeTeam && isHomeTeamTurn) || (!userViewingHomeTeam && isAwayTeamTurn)
        Log.d("isOffenseSituation", "Inning: '$inning', User Viewing Home: $userViewingHomeTeam -> isOffense: $offense")
        return offense
    }

    // 문자열을 Double로 안전하게 변환 (null, 빈 문자열, "-" 처리)
    private fun safeStringToDouble(value: String?): Double? {
        if (value.isNullOrBlank() || value == "-") return null
        return try {
            value.replace(",", "").trim().toDoubleOrNull() // 쉼표 제거 및 공백 제거 후 변환
        } catch (e: NumberFormatException) {
            Log.w("SafeParse", "Failed to parse '$value' as Double: ${e.message}")
            null
        }
    }

    // 문자열을 Int로 안전하게 변환 (null, 빈 문자열, "-" 처리)
    private fun safeStringToInt(value: String?): Int? {
        if (value.isNullOrBlank() || value == "-") return null
        return try {
            value.replace("[^\\d-]".toRegex(), "").trim().toIntOrNull() // 숫자와 '-' 외의 문자 제거
        } catch (e: NumberFormatException) {
            Log.w("SafeParse", "Failed to parse '$value' as Int: ${e.message}")
            null
        }
    }

    // API 호출 실패 또는 예외 발생 시 더미 데이터로 리사이클러뷰 설정
    private fun setupRecyclerViewWithDummyData() {
        Log.w("ChangeMemberActivity", "Setting up RecyclerView with DUMMY data.")
        // 비교 대상 선수 객체가 null이면 어댑터 생성 시 오류 발생 가능하므로 최소 객체 생성
        if (currentPitcherPlayer == null) {
            currentPitcherPlayer = AppPlayer(name = "투수?", position = "투수")
        }
        if (currentBatterPlayer == null) {
            currentBatterPlayer = AppPlayer(name = "타자?", position = "타자")
        }

        val dummyMembers = loadDummyCandidates() // 더미 선수 목록 로드
        setupAdapter(dummyMembers) // 더미 목록으로 어댑터 설정
    }

    // 하이라이트 섹션 UI 설정 함수 (레이아웃 ID 수정 후 직접 할당)
    // ChangeMemberActivity.kt - setupHighlightData 수정
    // ChangeMemberActivity.kt - setupHighlightData 수정
    private fun setupHighlightData(
        inning: String,
        outCount: Int,
        homeScore: Int,
        awayScore: Int,
        pitcherNameToDisplay: String,
        batterNameToDisplay: String,
        pitcherImageUrlToDisplay: String,
        batterImageUrlToDisplay: String,
        baseStatus: BaseStatus?
    ) {
        binding.highlightSection.apply {
            tvInning.text = inning

            // 항상 어웨이팀 정보를 왼쪽, 홈팀 정보를 오른쪽에 표시
            tvAwayTeamName.text = this@ChangeMemberActivity.awayTeamName
            tvHomeTeamName.text = this@ChangeMemberActivity.homeTeamName
            tvAwayScore.text = currentAwayScore.toString()
            tvHomeScore.text = currentHomeScore.toString()

            tvAwayPlayerInfo.text = pitcherNameToDisplay
            tvHomePlayerInfo.text = batterNameToDisplay

            Glide.with(this@ChangeMemberActivity)
                .load(batterImageUrlToDisplay.ifBlank { R.drawable.placeholder_logo })
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .circleCrop()
                .into(ivHomeTeamLogo)

            Glide.with(this@ChangeMemberActivity)
                .load(pitcherImageUrlToDisplay.ifBlank { R.drawable.placeholder_logo })
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .circleCrop()
                .into(ivAwayTeamLogo)

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
                else -> {
                    ivOut1.setBackgroundResource(R.drawable.out_empty)
                    ivOut2.setBackgroundResource(R.drawable.out_empty)
                }
            }

            ivFirstBase.setBackgroundResource(if (baseStatus?.first == true) R.drawable.base_fill else R.drawable.base_empty)
            ivSecondBase.setBackgroundResource(if (baseStatus?.second == true) R.drawable.base_fill else R.drawable.base_empty)
            ivThirdBase.setBackgroundResource(if (baseStatus?.third == true) R.drawable.base_fill else R.drawable.base_empty)

            btnPlay.visibility = View.GONE
        }
    }


    // 상황에 맞는 더미 선수 목록 생성 (API 실패 시 사용)
    private fun loadDummyCandidates(): List<AppPlayer> {
        Log.d("DummyData", "Loading dummy player candidates.")
        val teamForDummy = isHomeTeam // 사용자가 보고 있는 팀 기준으로 isHomeTeam 설정
        return if (isOffenseSituation) {
            // 더미 타자 목록
            listOf(
                AppPlayer(name = "더미 타자1", position = "외야수", isHomeTeam = teamForDummy, battingAverage = 0.250, hits = 10, homeRuns = 1, rbi = 5),
                AppPlayer(name = "더미 타자2", position = "내야수", isHomeTeam = teamForDummy, battingAverage = 0.300, hits = 15, homeRuns = 2, rbi = 8)
            )
        } else {
            // 더미 투수 목록
            listOf(
                AppPlayer(name = "더미 투수1", position = "투수", isHomeTeam = teamForDummy, era = 3.50, wins = 1, losses = 0, saves = 0),
                AppPlayer(name = "더미 투수2", position = "투수", isHomeTeam = teamForDummy, era = 4.10, wins = 0, losses = 1, holds = 2)
            )
        }
    }

    // 리스너 설정 (뒤로가기 버튼 등)
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    // Intent Extra 키 정의
    companion object {
        const val EXTRA_IS_HOME_TEAM = "extra_is_home_team"
        const val EXTRA_GAME_ID = "extra_game_id"
        const val EXTRA_HOME_TEAM_ID = "extra_home_team_id" // 팀 ID (필요시 사용)
        const val EXTRA_AWAY_TEAM_ID = "extra_away_team_id" // 팀 ID (필요시 사용)
        const val EXTRA_HOME_TEAM_NAME = "extra_home_team_name"
        const val EXTRA_AWAY_TEAM_NAME = "extra_away_team_name"
        const val EXTRA_INNING = "extra_inning"
        const val EXTRA_OUT_COUNT = "extra_out_count"
        const val EXTRA_HOME_SCORE = "extra_home_score"
        const val EXTRA_AWAY_SCORE = "extra_away_score"
        const val EXTRA_PITCHER_NAME = "extra_pitcher_name" // 하이라이트 시점 투수 이름
        const val EXTRA_BATTER_NAME = "extra_batter_name"   // 하이라이트 시점 타자 이름
        const val EXTRA_PITCHER_IMAGE_URL = "extra_pitcher_image_url" // 하이라이트 시점 투수 이미지
        const val EXTRA_BATTER_IMAGE_URL = "extra_batter_image_url"   // 하이라이트 시점 타자 이미지
        const val EXTRA_BASE_STATUS = "extra_base_status" // 하이라이트 시점 베이스 상태

        // MyPickActivity로 전달할 하이라이트 섹션 표시용 Extra 키
        const val EXTRA_HIGHLIGHT_PITCHER_NAME = "extra_highlight_pitcher_name"
        const val EXTRA_HIGHLIGHT_BATTER_NAME = "extra_highlight_batter_name"
        const val EXTRA_HIGHLIGHT_PITCHER_IMAGE_URL = "extra_highlight_pitcher_image_url"
        const val EXTRA_HIGHLIGHT_BATTER_IMAGE_URL = "extra_highlight_batter_image_url"
        // EXTRA_BASE_STATUS 키는 동일하게 사용
    }
}