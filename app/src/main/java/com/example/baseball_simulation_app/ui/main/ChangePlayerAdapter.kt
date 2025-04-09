package com.example.baseball_simulation_app.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.Player
import com.example.baseball_simulation_app.databinding.ItemChangeHitterBinding
import com.example.baseball_simulation_app.databinding.ItemChangePitcherBinding

class ChangePlayerAdapter(
    private val players: List<Player>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HITTER = 0
        private const val TYPE_PITCHER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (players[position].position.contains("투수")) TYPE_PITCHER else TYPE_HITTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HITTER) {
            val binding = ItemChangeHitterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HitterViewHolder(binding)
        } else {
            val binding = ItemChangePitcherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PitcherViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val player = players[position]
        when (holder) {
            is HitterViewHolder -> holder.bind(player)
            is PitcherViewHolder -> holder.bind(player)
        }
    }

    override fun getItemCount() = players.size

    class HitterViewHolder(private val binding: ItemChangeHitterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Player) {
            binding.apply {
                tvPlayerInfo.text = player.name
                tvBattingAverage.text = player.battingAverage?.let { String.format("%.3f", it) } ?: "-"
                tvHits.text = player.hits?.toString() ?: "-"
                tvOnBasePercentage.text = player.onBasePercentage?.let { String.format("%.3f", it) } ?: "-"
                tvHomeRuns.text = player.homeRuns?.toString() ?: "-"
                tvSlg.text = player.sluggingPercentage?.let { String.format("%.3f", it) } ?: "-"
                tvRbi.text = player.rbi?.toString() ?: "-"

                Glide.with(root.context)
                    .load(player.playerImageUrl)
                    .placeholder(R.drawable.placeholder_logo)
                    .error(R.drawable.placeholder_logo)
                    .into(ivPlayerImage)

                root.setOnClickListener {
                    val context = it.context
                    val intent = Intent(context, MyPickActivity::class.java).apply {
                        putExtra("selected_player", player)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    class PitcherViewHolder(private val binding: ItemChangePitcherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Player) {
            binding.apply {
                tvPlayerInfo.text = player.name
                tvEra.text = player.era?.let { String.format("%.2f", it) } ?: "-"
                tvInnings.text = player.inningsPitched?.toString() ?: "-"
                tvWins.text = player.wins?.toString() ?: "-"
                tvLosses.text = player.losses?.toString() ?: "-"
                tvHolds.text = player.holds?.toString() ?: "-"
                tvSaves.text = player.saves?.toString() ?: "-"

                Glide.with(root.context)
                    .load(player.playerImageUrl)
                    .placeholder(R.drawable.placeholder_logo)
                    .error(R.drawable.placeholder_logo)
                    .into(ivPlayerImage)

                root.setOnClickListener {
                    val context = it.context
                    val intent = Intent(context, MyPickActivity::class.java).apply {
                        putExtra("selected_player", player)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}
