package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.databinding.ItemGameBinding
import com.example.baseball_simulation_app.svgformat.GlideApp

class GameAdapter(
    private val onGameClicked: (GameModel) -> Unit
) : ListAdapter<GameModel, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameModel) {
            binding.apply {
                // 경기 정보 바인딩
                tvGameStatus.text = game.stadium
                tvHomeTeamName.text = game.homeTeam.displayName
                tvAwayTeamName.text = game.awayTeam.displayName
                tvHomeScore.text = game.homeScore.toString()
                tvAwayScore.text = game.awayScore.toString()

                // id를 root.tag에 저장
                root.tag = game.id

                // 홈팀 로고
                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java)
                    .load(game.homeTeam.logoUrl)
                    .error(R.drawable.placeholder_logo)
                    .into(ivHomeTeamLogo)

                // 어웨이팀 로고
                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java)
                    .load(game.awayTeam.logoUrl)
                    .error(R.drawable.placeholder_logo)
                    .into(ivAwayTeamLogo)

                // 카드 전체 클릭
                root.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onGameClicked(getItem(adapterPosition))
                    }
                }

                // 홈팀 로고 클릭
                ivHomeTeamLogo.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val game = getItem(adapterPosition)
                        navigateToHighlight(game.homeTeam.id, true, game.id)
                    }
                }

                // 어웨이팀 로고 클릭
                ivAwayTeamLogo.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val game = getItem(adapterPosition)
                        navigateToHighlight(game.awayTeam.id, false, game.id)
                    }
                }
            }
        }

        private fun navigateToHighlight(teamId: String, isHomeTeam:Boolean, gameId: String) {
            val context = itemView.context
            val intent = Intent(context, HighlightActivity::class.java).apply {
                putExtra(HighlightActivity.EXTRA_TEAM_ID, teamId)
                putExtra(HighlightActivity.EXTRA_IS_HOME_TEAM, isHomeTeam)
                putExtra(HighlightActivity.EXTRA_GAME_ID, gameId)
            }
            context.startActivity(intent)

            // ⭐ 전환 애니메이션 추가
            if (context is AppCompatActivity) {
                context.overridePendingTransition(
                    R.anim.slide_in_right,  // 새로운 화면이 오른쪽에서 들어옴
                    R.anim.slide_out_left   // 현재 화면이 왼쪽으로 나감
                )
            }
        }
    }

    private class GameDiffCallback : DiffUtil.ItemCallback<GameModel>() {
        override fun areItemsTheSame(oldItem: GameModel, newItem: GameModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GameModel, newItem: GameModel): Boolean {
            return oldItem == newItem
        }
    }
}
