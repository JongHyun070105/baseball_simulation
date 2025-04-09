package com.example.baseball_simulation_app.ui.main

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGameClicked(getItem(position))
                }
            }
        }

        fun bind(game: GameModel) {
            binding.apply {
                // 경기장 정보
                tvGameStatus.text = game.stadium

                // 팀 이름
                tvHomeTeamName.text = game.homeTeam.displayName
                tvAwayTeamName.text = game.awayTeam.displayName

                // 점수
                tvHomeScore.text = game.homeScore.toString()
                tvAwayScore.text = game.awayScore.toString()

                // 팀 로고 (GlideApp 사용)
                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java) // SVG를 PictureDrawable로 변환
                    .load(game.homeTeam.logoUrl)
                    .error(R.drawable.placeholder_logo)
                    .into(ivHomeTeamLogo)

                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java)
                    .load(game.awayTeam.logoUrl)
                    .error(R.drawable.placeholder_logo)
                    .into(ivAwayTeamLogo)
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