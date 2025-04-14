package com.example.baseball_simulation_app.ui.main

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
     import androidx.core.content.res.ResourcesCompat
     import com.bumptech.glide.Glide
     import com.github.mikephil.charting.components.Legend
     import com.github.mikephil.charting.components.XAxis
     import com.github.mikephil.charting.data.Entry
     import com.github.mikephil.charting.data.LineData
     import com.github.mikephil.charting.data.LineDataSet
     import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
     import com.github.mikephil.charting.formatter.ValueFormatter
     import com.github.mikephil.charting.animation.Easing

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
                     tvHomeTeamName.text = item.homeTeamName
                     tvAwayTeamName.text = item.awayTeamName
                     tvHomeScore.text = item.homeScore.toString()
                     tvAwayScore.text = item.awayScore.toString()
                     tvStadium.text = item.stadium

                     // 로고 로딩 - Glide 사용해 URL로부터 이미지 로드
                     Glide.with(itemView.context)
                         .load(item.homeTeamLogoUrl)
                         .placeholder(R.drawable.kia)
                         .error(R.drawable.kia)
                         .into(ivHomeTeamLogo)

                     Glide.with(itemView.context)
                         .load(item.awayTeamLogoUrl)
                         .placeholder(R.drawable.kiwoom)
                         .error(R.drawable.kiwoom)
                         .into(ivAwayTeamLogo)

                     setupChart()

                     btnRetry.setOnClickListener {
                         val intent = Intent(itemView.context, ChangeMemberActivity::class.java)
                         itemView.context.startActivity(intent)
                         (itemView.context as? ResultActivity)?.finish()
                     }

                     btnExit.setOnClickListener {
                         val intent = Intent(itemView.context, MainActivity::class.java)
                         intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                         itemView.context.startActivity(intent)
                         (itemView.context as? ResultActivity)?.finish()
                     }
                 }
             }

             private fun setupChart() {
                 val chart = binding.winRateChart
                 val customTypeface = ResourcesCompat.getFont(itemView.context, R.font.kbo)

                 // 수정: 각 이닝별 승률 데이터 (합이 100%가 되도록)
                 val originalEntries = ArrayList<Entry>().apply {
                     add(Entry(0f, 50f)) // 시작 시점: 50:50
                     add(Entry(1f, 48f))
                     add(Entry(2f, 46f))
                     add(Entry(3f, 44f))
                     add(Entry(4f, 45f))
                     add(Entry(5f, 47f))
                     add(Entry(6f, 46f))
                     add(Entry(7f, 45f))
                     add(Entry(8f, 43f))
                     add(Entry(9f, 40f))
                 }

                 val changedEntries = ArrayList<Entry>().apply {
                     add(Entry(0f, 50f)) // 시작 시점: 50:50
                     add(Entry(1f, 52f))
                     add(Entry(2f, 54f))
                     add(Entry(3f, 56f))
                     add(Entry(4f, 55f))
                     add(Entry(5f, 53f))
                     add(Entry(6f, 54f))
                     add(Entry(7f, 55f))
                     add(Entry(8f, 57f))
                     add(Entry(9f, 60f))
                 }

                 class WinRateFormatter : ValueFormatter() {
                     override fun getFormattedValue(value: Float): String {
                         return "${value.toInt()}%"
                     }
                 }

                 val originalDataSet = LineDataSet(originalEntries, "기존 승률").apply {
                     color = Color.BLUE
                     valueTextColor = Color.BLUE
                     lineWidth = 2f
                     setDrawCircles(true)
                     setCircleColor(Color.BLUE)
                     setDrawValues(true)
                     valueTextSize = 10f
                     valueTypeface = customTypeface
                     valueFormatter = WinRateFormatter()
                 }

                 val changedDataSet = LineDataSet(changedEntries, "교체 후 승률").apply {
                     color = Color.RED
                     valueTextColor = Color.RED
                     lineWidth = 2f
                     setDrawCircles(true)
                     setCircleColor(Color.RED)
                     setDrawValues(true)
                     valueTextSize = 10f
                     valueTypeface = customTypeface
                     valueFormatter = WinRateFormatter()
                 }

                 val lineData = LineData(originalDataSet, changedDataSet)

                 chart.apply {
                     data = lineData
                     description.isEnabled = false

                     legend.apply {
                         isEnabled = true
                         verticalAlignment = Legend.LegendVerticalAlignment.TOP
                         horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                         yOffset = 15f
                         typeface = customTypeface
                     }

                     isDoubleTapToZoomEnabled = false
                     setTouchEnabled(false)
                     setPinchZoom(false)
                     isDragEnabled = false
                     isScaleXEnabled = false
                     isScaleYEnabled = false

                     setExtraOffsets(10f, 30f, 15f, 10f)

                     xAxis.apply {
                         position = XAxis.XAxisPosition.BOTTOM
                         setDrawGridLines(true)
                         granularity = 1f
                         axisMinimum = 0f
                         axisMaximum = 9.5f
                         labelCount = 10
                         typeface = customTypeface
                         axisLineColor = Color.BLACK
                         textSize = 8f
                         valueFormatter = IndexAxisValueFormatter(
                             arrayOf("시작", "1회", "2회", "3회", "4회", "5회", "6회", "7회", "8회", "9회")
                         )
                     }

                     axisLeft.apply {
                         axisMinimum = 0f
                         axisMaximum = 100f
                         setDrawLabels(true)
                         setDrawGridLines(true)
                         typeface = customTypeface
                         valueFormatter = object : ValueFormatter() {
                             override fun getFormattedValue(value: Float): String {
                                 return "${value.toInt()}%"
                             }
                         }
                         setDrawZeroLine(true)
                         spaceTop = 5f
                         axisLineWidth = 1f
                         axisLineColor = Color.BLACK
                         textSize = 8f
                     }

                     axisRight.isEnabled = false

                     setViewPortOffsets(70f, 20f, 30f, 50f)

                     animateX(1000, Easing.EaseInSine)
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