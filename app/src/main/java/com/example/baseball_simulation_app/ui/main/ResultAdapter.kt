package com.example.baseball_simulation_app.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.MainActivity
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.databinding.ItemResultBinding

import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ResultAdapter : ListAdapter<ResultActivity.ResultItem, ResultAdapter.ResultViewHolder>(ResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResultViewHolder(private val binding: ItemResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResultActivity.ResultItem) {
            with(binding) {
                // Set team info
                tvHomeTeamName.text = item.homeTeamName
                tvAwayTeamName.text = item.awayTeamName
                tvHomeScore.text = item.homeScore.toString()
                tvAwayScore.text = item.awayScore.toString()
                tvStadium.text = item.stadium

                // Load team logos
                ivHomeTeamLogo.setImageResource(R.drawable.placeholder_logo)
                ivAwayTeamLogo.setImageResource(R.drawable.placeholder_logo)

                // Setup chart
                setupChart()

                // Setup buttons
                btnRetry.setOnClickListener {
                    // Navigate to ChangeMemberActivity
                    val intent = Intent(itemView.context, ChangeMemberActivity::class.java)
                    itemView.context.startActivity(intent)
                    (itemView.context as? ResultActivity)?.finish()
                }

                btnExit.setOnClickListener {
                    // Exit to MainActivity
                    val intent = Intent(itemView.context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    itemView.context.startActivity(intent)
                    (itemView.context as? ResultActivity)?.finish()
                }
            }
        }

        private fun setupChart() {
            val chart = binding.winRateChart

            // Create data for original win rate
            val originalEntries = ArrayList<Entry>().apply {
                add(Entry(1f, 45f))
                add(Entry(2f, 47f))
                add(Entry(3f, 49f))
                add(Entry(4f, 50f))
                add(Entry(5f, 52f))
                add(Entry(6f, 53f))
                add(Entry(7f, 54f))
                add(Entry(8f, 55f))
                add(Entry(9f, 56f))
            }

            // Create data for win rate after player change
            val changedEntries = ArrayList<Entry>().apply {
                add(Entry(1f, 48f))
                add(Entry(2f, 51f))
                add(Entry(3f, 54f))
                add(Entry(4f, 57f))
                add(Entry(5f, 59f))
                add(Entry(6f, 61f))
                add(Entry(7f, 63f))
                add(Entry(8f, 65f))
                add(Entry(9f, 67f))
            }

            // Create dataset for original win rate (blue)
            val originalDataSet = LineDataSet(originalEntries, "기존 승률").apply {
                color = Color.BLUE
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setDrawCircles(true)
                setCircleColor(Color.BLUE)
                setDrawValues(true)
                valueTextSize = 10f
            }

            // Create dataset for changed win rate (red)
            val changedDataSet = LineDataSet(changedEntries, "교체 후 승률").apply {
                color = Color.RED
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setDrawCircles(true)
                setCircleColor(Color.RED)
                setDrawValues(true)
                valueTextSize = 10f
            }

            val lineData = LineData(originalDataSet, changedDataSet)

            // Customize chart
            chart.apply {
                data = lineData
                description.isEnabled = false
                legend.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                setTouchEnabled(true)
                setPinchZoom(true)

                // X-axis customization (innings)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    granularity = 1f
                    labelCount = 9
                    valueFormatter = IndexAxisValueFormatter(
                        arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
                    )
                }

                // Y-axis customization (win rate percentage)
                axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = 100f
                    setDrawLabels(true)
                    setDrawGridLines(true)
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}"
                        }
                    }
                }

                // Remove right axis
                axisRight.isEnabled = false

                // Refresh chart
                invalidate()
            }
        }
    }
}

class ResultDiffCallback : DiffUtil.ItemCallback<ResultActivity.ResultItem>() {
    override fun areItemsTheSame(oldItem: ResultActivity.ResultItem, newItem: ResultActivity.ResultItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ResultActivity.ResultItem, newItem: ResultActivity.ResultItem): Boolean {
        return oldItem == newItem
    }
}