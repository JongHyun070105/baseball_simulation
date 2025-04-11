package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.os.Build // Build 버전 체크 추가
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.BaseStatus
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ActivityMypickBinding

class MyPickActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypickBinding

    // 선수 비교 섹션 표시용 선수 데이터
    private var batterPlayer: Player? = null // 선택된/교체된 타자
    private var pitcherPlayer: Player? = null // 선택된/교체된 투수

    // 게임 기본 정보 (Intent로 전달받음)
    private var gameId: String = ""
    private var homeTeamName: String = ""
    private var awayTeamName: String = ""
    private var inning: String = ""
    private var outCount: Int = 0
    private var homeScore: Int = 0
    private var awayScore: Int = 0
    private var isHomeTeamSelected: Boolean = true // 사용자가 홈팀 라인업에서 선택했는지 여부

    // 하이라이트 섹션 표시용 데이터 (Intent로 전달받음)
    private var highlightBatterName: String = ""   // 하이라이트 시점 타자 이름
    private var highlightPitcherName: String = ""  // 하이라이트 시점 투수 이름
    private var highlightBatterImageUrl: String = "" // 하이라이트 시점 타자 이미지 URL
    private var highlightPitcherImageUrl: String = "" // 하이라이트 시점 투수 이미지 URL
    private var highlightBaseStatus: BaseStatus? = null // 하이라이트 시점 베이스 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        getIntentData() // Intent로부터 모든 필요 데이터 추출
        displayPlayersForComparison() // 하단 선수 비교 섹션 UI 업데이트
        setupHighlightData() // 상단 하이라이트 섹션 UI 업데이트 (수정됨)
        setupStartButton() // 시작 버튼 리스너 설정
    }

    // 뒤로가기 버튼 설정
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    // Intent로부터 데이터 추출하여 멤버 변수에 저장
    private fun getIntentData() {
        // 선수 비교 섹션용 데이터 (선택/교체된 선수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            batterPlayer = intent.getSerializableExtra("selected_batter", Player::class.java)
            pitcherPlayer = intent.getSerializableExtra("selected_pitcher", Player::class.java)
//            highlightBaseStatus = intent.getSerializableExtra(ChangeMemberActivity.EXTRA_BASE_STATUS, BaseStatus::class.java) // 키 수정 및 타입 명시
        } else {
            @Suppress("DEPRECATION")
            batterPlayer = intent.getSerializableExtra("selected_batter") as? Player
            @Suppress("DEPRECATION")
            pitcherPlayer = intent.getSerializableExtra("selected_pitcher") as? Player
//            @Suppress("DEPRECATION")
//            highlightBaseStatus = intent.getSerializableExtra(ChangeMemberActivity.EXTRA_BASE_STATUS) as? BaseStatus // 키 수정
        }

        // 게임 기본 정보
        gameId = intent.getStringExtra(ChangeMemberActivity.EXTRA_GAME_ID) ?: ""
        homeTeamName = intent.getStringExtra(ChangeMemberActivity.EXTRA_HOME_TEAM_NAME) ?: "홈팀"
        awayTeamName = intent.getStringExtra(ChangeMemberActivity.EXTRA_AWAY_TEAM_NAME) ?: "원정팀"
        inning = intent.getStringExtra(ChangeMemberActivity.EXTRA_INNING) ?: "?"
        outCount = intent.getIntExtra(ChangeMemberActivity.EXTRA_OUT_COUNT, 0)
        homeScore = intent.getIntExtra(ChangeMemberActivity.EXTRA_HOME_SCORE, 0)
        awayScore = intent.getIntExtra(ChangeMemberActivity.EXTRA_AWAY_SCORE, 0)
        isHomeTeamSelected = intent.getBooleanExtra(ChangeMemberActivity.EXTRA_IS_HOME_TEAM, true)

        // 하이라이트 섹션 표시용 정보 (원본 하이라이트 시점)
        highlightBatterName = intent.getStringExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_NAME) ?: "타자?"
        highlightPitcherName = intent.getStringExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_NAME) ?: "투수?"
        highlightBatterImageUrl = intent.getStringExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_IMAGE_URL) ?: ""
        highlightPitcherImageUrl = intent.getStringExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_IMAGE_URL) ?: ""

        // 로그로 받은 데이터 확인
        Log.d("MyPickActivity", "비교 타자: ${batterPlayer?.name}, 비교 투수: ${pitcherPlayer?.name}")
        Log.d("MyPickActivity", "하이라이트 타자: $highlightBatterName, 하이라이트 투수: $highlightPitcherName")
        Log.d("MyPickActivity", "게임ID: $gameId, 이닝: $inning, 홈팀선택: $isHomeTeamSelected, 베이스: ${highlightBaseStatus}")
    }

    // 하단 선수 비교 섹션 UI 업데이트
    private fun displayPlayersForComparison() {
        // 선택된 타자 정보 표시 (null 처리 포함)
        batterPlayer?.let { batter ->
            binding.tvBatterName.text = batter.name // 비교 섹션 타자 이름
            Glide.with(this)
                .load(batter.playerImageUrl.ifBlank { R.drawable.placeholder_logo })
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .circleCrop()
                .into(binding.ivBatterImage) // 비교 섹션 타자 이미지

            // 타자 스탯 표시
            binding.tvBattingAvg.text = batter.battingAverage?.let { String.format("%.3f", it) } ?: "-"
            binding.tvHits.text = batter.hits?.toString() ?: "-"
            binding.tvHomeRuns.text = batter.homeRuns?.toString() ?: "-"
            binding.tvRBIs.text = batter.rbi?.toString() ?: "-"
        } ?: run {
            // 타자 정보 없을 경우 기본값 표시
            binding.tvBatterName.text = "타자 정보 없음"
            binding.ivBatterImage.setImageResource(R.drawable.placeholder_logo)
            binding.tvBattingAvg.text = "-"
            binding.tvHits.text = "-"
            binding.tvHomeRuns.text = "-"
            binding.tvRBIs.text = "-"
            Log.w("MyPickActivity", "Batter data for comparison is null.")
        }

        // 선택된 투수 정보 표시 (null 처리 포함)
        pitcherPlayer?.let { pitcher ->
            binding.tvPitcherName.text = pitcher.name // 비교 섹션 투수 이름
            Glide.with(this)
                .load(pitcher.playerImageUrl.ifBlank { R.drawable.placeholder_logo })
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .circleCrop()
                .into(binding.ivPitcherImage) // 비교 섹션 투수 이미지

            // 투수 스탯 표시
            binding.tvERA.text = pitcher.era?.let { String.format("%.2f", it) } ?: "-"
            binding.tvWinLoss.text = "${pitcher.wins ?: "-"}승 ${pitcher.losses ?: "-"}패"
            binding.tvHolds.text = pitcher.holds?.toString() ?: "-"
            binding.tvSaves.text = pitcher.saves?.toString() ?: "-"
        } ?: run {
            // 투수 정보 없을 경우 기본값 표시
            binding.tvPitcherName.text = "투수 정보 없음"
            binding.ivPitcherImage.setImageResource(R.drawable.placeholder_logo)
            binding.tvERA.text = "-"
            binding.tvWinLoss.text = "-승 -패"
            binding.tvHolds.text = "-"
            binding.tvSaves.text = "-"
            Log.w("MyPickActivity", "Pitcher data for comparison is null.")
        }
    }

    // 상단 하이라이트 섹션 UI 업데이트 (레이아웃 ID 수정 후 직접 할당)
    // MyPickActivity.kt - setupHighlightData 수정
    private fun setupHighlightData() {
        binding.highlightSection.apply {
            tvInning.text = inning

            // 항상 어웨이팀 정보를 왼쪽, 홈팀 정보를 오른쪽에 표시
            tvAwayTeamName.text = awayTeamName
            tvHomeTeamName.text = homeTeamName
            tvAwayScore.text = awayScore.toString()
            tvHomeScore.text = homeScore.toString()

            tvAwayPlayerInfo.text = highlightPitcherName
            tvHomePlayerInfo.text = highlightBatterName

            Glide.with(this@MyPickActivity)
                .load(highlightBatterImageUrl.ifBlank { R.drawable.placeholder_logo })
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .circleCrop()
                .into(ivHomeTeamLogo)

            Glide.with(this@MyPickActivity)
                .load(highlightPitcherImageUrl.ifBlank { R.drawable.placeholder_logo })
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

            ivFirstBase.setBackgroundResource(if (highlightBaseStatus?.first == true) R.drawable.base_fill else R.drawable.base_empty)
            ivSecondBase.setBackgroundResource(if (highlightBaseStatus?.second == true) R.drawable.base_fill else R.drawable.base_empty)
            ivThirdBase.setBackgroundResource(if (highlightBaseStatus?.third == true) R.drawable.base_fill else R.drawable.base_empty)

            btnPlay.visibility = View.GONE
        }
    }


    // '시작' 버튼 리스너 설정
    private fun setupStartButton() {
        binding.btnStart.setOnClickListener {
            // 비교 섹션의 선수 데이터 유효성 검사
            if (batterPlayer == null || pitcherPlayer == null) {
                Toast.makeText(this, "선수 정보가 올바르지 않습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show()
                Log.e("MyPickActivity", "Start button clicked but player data for comparison is missing. Batter: ${batterPlayer?.name}, Pitcher: ${pitcherPlayer?.name}")
                return@setOnClickListener
            }

            Log.d("MyPickActivity", "시뮬레이션 시작 버튼 클릭: Batter=${batterPlayer!!.name}, Pitcher=${pitcherPlayer!!.name}")

            // ResultActivity로 데이터 전달 및 시작 (SimulationActivity 대신)
            val intent = Intent(this, ResultActivity::class.java).apply {
                // 선수 정보 전달 (비교 섹션의 선수들)
                putExtra("BATTER_PLAYER", batterPlayer) // 선택/교체된 타자
                putExtra("PITCHER_PLAYER", pitcherPlayer) // 선택/교체된 투수

                // 게임 컨텍스트 정보 전달
                putExtra("HOME_TEAM_NAME", homeTeamName)
                putExtra("AWAY_TEAM_NAME", awayTeamName)
                putExtra("HOME_SCORE", homeScore) // 현재 점수 전달
                putExtra("AWAY_SCORE", awayScore) // 현재 점수 전달
                // TODO: 실제 경기장 정보 가져오기
                val stadiumName = "경기장 정보 필요" // 실제 경기장 이름 가져오는 로직 추가 필요
                putExtra("STADIUM", stadiumName)
                // 필요하다면 gameId, 이닝, 아웃카운트 등 추가 정보 전달 가능
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // 화면 전환 애니메이션
        }
    }
}