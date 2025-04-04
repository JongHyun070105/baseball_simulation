package com.example.baseball_simulation_app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.data.model.Game
import com.example.baseball_simulation_app.databinding.ItemGameBinding

class GameListAdapter : ListAdapter<Game, GameListAdapter.GameViewHolder>(DiffCallback()) {

    inner class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            binding.apply {
                homeTeam.text = game.homeTeamName
                awayTeam.text = game.awayTeamName
                score.text = game.score
                stadium.text = game.stadium
                date.text = game.date

                Glide.with(binding.root.context)
                    .load(game.homeTeamLogoUrl)
                    .fitCenter()
                    .into(binding.ivHomeTeamLogo)

                Glide.with(binding.root.context)
                    .load(game.awayTeamLogoUrl)
                    .fitCenter()
                    .into(binding.ivAwayTeamLogo)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem == newItem
    }
}
