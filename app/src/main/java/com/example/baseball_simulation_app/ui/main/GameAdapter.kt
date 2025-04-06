package com.example.baseball_simulation_app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.databinding.ItemGameBinding

class GameAdapter(
    private val gameList: List<GameModel>,
    private val onGameClicked: (GameModel) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(gameList[position])
    }

    override fun getItemCount(): Int = gameList.size

    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGameClicked(gameList[position])
                }
            }
        }

        fun bind(game: GameModel) {
            // 팀 로고 이미지 설정
            binding.ivHomeTeamLogo.setImageResource(game.homeTeam.logoResId)
            binding.ivAwayTeamLogo.setImageResource(game.awayTeam.logoResId)

            // 팀 이름 설정
            binding.tvHomeTeamName.text = game.homeTeam.displayName
            binding.tvAwayTeamName.text = game.awayTeam.displayName

            // 점수 설정
            binding.tvHomeScore.text = game.homeScore.toString()
            binding.tvAwayScore.text = game.awayScore.toString()

            // 이기고 있는 팀과 지고 있는 팀의 스코어 색상 설정
            when {
                game.homeScore > game.awayScore -> {
                    binding.tvHomeScore.setTextColor(android.graphics.Color.parseColor("#0000FF")) // 이기는 팀 - 파란색
                    binding.tvAwayScore.setTextColor(android.graphics.Color.parseColor("#808080")) // 지는 팀 - 진한 회색
                }
                game.homeScore < game.awayScore -> {
                    binding.tvHomeScore.setTextColor(android.graphics.Color.parseColor("#808080")) // 지는 팀 - 진한 회색
                    binding.tvAwayScore.setTextColor(android.graphics.Color.parseColor("#0000FF")) // 이기는 팀 - 파란색
                }
                else -> {
                    // 동점인 경우 두 팀 모두 기본 색상(검정) 사용
                    binding.tvHomeScore.setTextColor(android.graphics.Color.BLACK)
                    binding.tvAwayScore.setTextColor(android.graphics.Color.BLACK)
                }
            }

            // 경기장과 날짜 설정
            binding.tvGameStatus.text = game.stadium
        }
    }
}