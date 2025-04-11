package com.example.baseball_simulation_app.ui.main

import android.app.Activity // Import Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.BaseStatus // BaseStatus import 추가
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ItemChangeHitterBinding
import com.example.baseball_simulation_app.databinding.ItemChangePitcherBinding

class ChangePlayerAdapter(
    private val players: List<Player>, // 교체 후보 선수 목록
    private val isOffense: Boolean = true, // 현재 공격 상황 여부
    private val currentPitcher: Player?, // 비교 대상 현재 투수 (Full Data)
    private val currentBatter: Player?,  // 비교 대상 현재 타자 (Full Data)
    // MyPickActivity로 전달할 게임 컨텍스트 정보
    private val gameId: String,
    private val homeTeamName: String,
    private val awayTeamName: String,
    private val inning: String,
    private val outCount: Int,
    private val homeScore: Int,
    private val awayScore: Int,
    private val selectedTeamIsHome: Boolean, // 현재 리스트가 홈팀 선수인지 여부
    // MyPickActivity의 하이라이트 섹션에 표시할 원본 정보
    private val highlightBatterName: String,
    private val highlightPitcherName: String,
    private val highlightBatterImageUrl: String,
    private val highlightPitcherImageUrl: String,
    private val highlightBaseStatus: BaseStatus? // 하이라이트 시점 베이스 상태
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HITTER = 0
        private const val TYPE_PITCHER = 1
    }

    // 선수의 포지션 정보를 보고 타자/투수 뷰 타입 결정
    override fun getItemViewType(position: Int): Int {
        // 포지션 문자열에 "투수"가 포함되어 있는지 확인 (대소문자 구분 없이)
        return if (players[position].position.contains("투수", ignoreCase = true)) TYPE_PITCHER else TYPE_HITTER
    }

    // 뷰 타입에 따라 다른 ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HITTER) {
            // 타자 뷰 홀더 생성
            val binding = ItemChangeHitterBinding.inflate(inflater, parent, false)
            HitterViewHolder(binding)
        } else {
            // 투수 뷰 홀더 생성
            val binding = ItemChangePitcherBinding.inflate(inflater, parent, false)
            PitcherViewHolder(binding)
        }
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val player = players[position] // 현재 위치의 선수 데이터
        when (holder) {
            is HitterViewHolder -> holder.bind(player) // 타자 뷰홀더 바인딩
            is PitcherViewHolder -> holder.bind(player) // 투수 뷰홀더 바인딩
        }
    }

    // 전체 아이템 개수 반환
    override fun getItemCount() = players.size

    // --- 타자 ViewHolder ---
    inner class HitterViewHolder(private val binding: ItemChangeHitterBinding) : RecyclerView.ViewHolder(binding.root) {
        // 타자 데이터 UI 바인딩 및 클릭 리스너 설정
        fun bind(player: Player) {
            binding.apply {
                // 선수 기본 정보 (이름, 포지션)
                tvPlayerInfo.text = "${player.name} (${player.position})"
                // 선수 이미지 (Glide, 원형)
                Glide.with(root.context)
                    .load(player.playerImageUrl.ifBlank { R.drawable.placeholder_logo })
                    .placeholder(R.drawable.placeholder_logo)
                    .error(R.drawable.placeholder_logo)
                    .circleCrop()
                    .into(ivPlayerImage)

                // 타자 스탯 (null 또는 "-" 처리)
                tvBattingAverage.text = player.battingAverage?.let { String.format("%.3f", it) } ?: "-"
                tvHits.text = player.hits?.toString() ?: "-"
                tvOnBasePercentage.text = player.onBasePercentage?.let { String.format("%.3f", it) } ?: "-"
                tvHomeRuns.text = player.homeRuns?.toString() ?: "-"
                tvSlg.text = player.sluggingPercentage?.let { String.format("%.3f", it) } ?: "-"
                tvRbi.text = player.rbi?.toString() ?: "-"

                // 아이템 클릭 시 MyPickActivity 시작
                root.setOnClickListener {
                    // 비교 대상 선수 정보가 없으면 진행 불가
                    if ((isOffense && currentPitcher == null) || (!isOffense && currentBatter == null)) {
                        Log.e("ChangePlayerAdapter", "Cannot start MyPickActivity: Current opponent player data is missing.")
                        Toast.makeText(it.context, "선수 정보 로딩 오류", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val context = it.context
                    val intent = Intent(context, MyPickActivity::class.java).apply {
                        Log.d("ChangePlayerAdapter", "Hitter selected: ${player.name}. Offense: $isOffense")

                        // MyPickActivity의 선수 비교 섹션에 들어갈 선수 결정
                        val selectedBatterForIntent: Player?
                        val selectedPitcherForIntent: Player?

                        if (isOffense) {
                            // 공격 상황: 선택된 타자가 새 타자, 상대 투수는 그대로
                            selectedBatterForIntent = player
                            selectedPitcherForIntent = currentPitcher
                            Log.d("IntentSetup", "Offense: Selected Batter=${selectedBatterForIntent?.name}, Opponent Pitcher=${selectedPitcherForIntent?.name}")
                        } else {
                            // 수비 상황에서 타자 선택됨 (일반적이지 않음, 경고 로그)
                            Log.w("ChangePlayerAdapter", "Warning: Hitter selected (${player.name}) during defense situation.")
                            selectedBatterForIntent = currentBatter // 현재 타자 유지
                            selectedPitcherForIntent = player // 선택된 타자를 투수로 전달 (로직 확인 필요)
                            Log.d("IntentSetup", "Defense (Hitter Clicked!): Current Batter=${selectedBatterForIntent?.name}, Selected as Pitcher=${selectedPitcherForIntent?.name}")
                        }

                        // Intent에 선수 정보 추가 (선수 비교 섹션용)
                        putExtra("selected_batter", selectedBatterForIntent)
                        putExtra("selected_pitcher", selectedPitcherForIntent)

                        // Intent에 게임 컨텍스트 정보 추가
                        putExtra(ChangeMemberActivity.EXTRA_GAME_ID, gameId)
                        putExtra(ChangeMemberActivity.EXTRA_HOME_TEAM_NAME, homeTeamName)
                        putExtra(ChangeMemberActivity.EXTRA_AWAY_TEAM_NAME, awayTeamName)
                        putExtra(ChangeMemberActivity.EXTRA_INNING, inning)
                        putExtra(ChangeMemberActivity.EXTRA_OUT_COUNT, outCount)
                        putExtra(ChangeMemberActivity.EXTRA_HOME_SCORE, homeScore)
                        putExtra(ChangeMemberActivity.EXTRA_AWAY_SCORE, awayScore)
                        putExtra(ChangeMemberActivity.EXTRA_IS_HOME_TEAM, selectedTeamIsHome)

                        // Intent에 하이라이트 섹션 표시용 원본 정보 추가
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_NAME, highlightBatterName)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_NAME, highlightPitcherName)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_IMAGE_URL, highlightBatterImageUrl)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_IMAGE_URL, highlightPitcherImageUrl)
//                        putExtra(ChangeMemberActivity.EXTRA_BASE_STATUS, highlightBaseStatus) // 베이스 상태 전달

                    }
                    // MyPickActivity 시작 및 화면 전환 애니메이션
                    context.startActivity(intent)
                    if (context is Activity) {
                        context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
        }
    }

    // --- 투수 ViewHolder ---
    inner class PitcherViewHolder(private val binding: ItemChangePitcherBinding) : RecyclerView.ViewHolder(binding.root) {
        // 투수 데이터 UI 바인딩 및 클릭 리스너 설정
        fun bind(player: Player) {
            binding.apply {
                // 선수 기본 정보 (이름, 포지션)
                tvPlayerInfo.text = "${player.name} (${player.position})"
                // 선수 이미지 (Glide, 원형)
                Glide.with(root.context)
                    .load(player.playerImageUrl.ifBlank { R.drawable.placeholder_logo })
                    .placeholder(R.drawable.placeholder_logo)
                    .error(R.drawable.placeholder_logo)
                    .circleCrop()
                    .into(ivPlayerImage)

                // 투수 스탯 (null 또는 "-" 처리)
                tvEra.text = player.era?.let { String.format("%.2f", it) } ?: "-"
                tvInnings.text = player.inningsPitched?.toString() ?: "-" // IP는 문자열 가능성
                tvWins.text = player.wins?.toString() ?: "-"
                tvLosses.text = player.losses?.toString() ?: "-"
                tvHolds.text = player.holds?.toString() ?: "-"
                tvSaves.text = player.saves?.toString() ?: "-"

                // 아이템 클릭 시 MyPickActivity 시작
                root.setOnClickListener {
                    // 비교 대상 선수 정보가 없으면 진행 불가
                    if ((isOffense && currentPitcher == null) || (!isOffense && currentBatter == null)) {
                        Log.e("ChangePlayerAdapter", "Cannot start MyPickActivity: Current opponent player data is missing.")
                        Toast.makeText(it.context, "선수 정보 로딩 오류", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val context = it.context
                    val intent = Intent(context, MyPickActivity::class.java).apply {
                        Log.d("ChangePlayerAdapter", "Pitcher selected: ${player.name}. Offense: $isOffense")

                        // MyPickActivity의 선수 비교 섹션에 들어갈 선수 결정
                        val selectedBatterForIntent: Player?
                        val selectedPitcherForIntent: Player?

                        if (isOffense) {
                            // 공격 상황에서 투수 선택됨 (일반적이지 않음, 경고 로그)
                            Log.w("ChangePlayerAdapter", "Warning: Pitcher selected (${player.name}) during offense situation.")
                            selectedBatterForIntent = player // 선택된 투수를 타자로 전달 (로직 확인 필요)
                            selectedPitcherForIntent = currentPitcher // 현재 투수 유지
                            Log.d("IntentSetup", "Offense (Pitcher Clicked!): Selected as Batter=${selectedBatterForIntent?.name}, Opponent Pitcher=${selectedPitcherForIntent?.name}")
                        } else {
                            // 수비 상황: 선택된 투수가 새 투수, 상대 타자는 그대로
                            selectedBatterForIntent = currentBatter
                            selectedPitcherForIntent = player
                            Log.d("IntentSetup", "Defense: Opponent Batter=${selectedBatterForIntent?.name}, Selected Pitcher=${selectedPitcherForIntent?.name}")
                        }

                        // Intent에 선수 정보 추가 (선수 비교 섹션용)
                        putExtra("selected_batter", selectedBatterForIntent)
                        putExtra("selected_pitcher", selectedPitcherForIntent)

                        // Intent에 게임 컨텍스트 정보 추가
                        putExtra(ChangeMemberActivity.EXTRA_GAME_ID, gameId)
                        putExtra(ChangeMemberActivity.EXTRA_HOME_TEAM_NAME, homeTeamName)
                        putExtra(ChangeMemberActivity.EXTRA_AWAY_TEAM_NAME, awayTeamName)
                        putExtra(ChangeMemberActivity.EXTRA_INNING, inning)
                        putExtra(ChangeMemberActivity.EXTRA_OUT_COUNT, outCount)
                        putExtra(ChangeMemberActivity.EXTRA_HOME_SCORE, homeScore)
                        putExtra(ChangeMemberActivity.EXTRA_AWAY_SCORE, awayScore)
                        putExtra(ChangeMemberActivity.EXTRA_IS_HOME_TEAM, selectedTeamIsHome)

                        // Intent에 하이라이트 섹션 표시용 원본 정보 추가
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_NAME, highlightBatterName)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_NAME, highlightPitcherName)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_BATTER_IMAGE_URL, highlightBatterImageUrl)
                        putExtra(ChangeMemberActivity.EXTRA_HIGHLIGHT_PITCHER_IMAGE_URL, highlightPitcherImageUrl)
//                        putExtra(ChangeMemberActivity.EXTRA_BASE_STATUS, highlightBaseStatus) // 베이스 상태 전달
                    }
                    // MyPickActivity 시작 및 화면 전환 애니메이션
                    context.startActivity(intent)
                    if (context is Activity) {
                        context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
        }
    }
}