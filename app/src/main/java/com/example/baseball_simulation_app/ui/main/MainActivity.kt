package com.example.baseball_simulation_app.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private lateinit var tvMonthYear: TextView
    private lateinit var calendar: Calendar
    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var gameListAdapter: GameListAdapter
    private lateinit var teamFilterSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2024)

        tvMonthYear = findViewById(R.id.tvMonthYear)
        gameRecyclerView = findViewById(R.id.gameRecyclerView)
        teamFilterSpinner = findViewById(R.id.teamFilterSpinner)

        gameRecyclerView.layoutManager = LinearLayoutManager(this)
        gameListAdapter = GameListAdapter()
        gameRecyclerView.adapter = gameListAdapter

        updateMonthYearDisplay()

        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener {
            changeMonth(-1)
        }

        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener {
            changeMonth(1)
        }

        tvMonthYear.setOnClickListener {
            showDatePickerDialog()
        }

        gameViewModel.games.observe(this) { games ->
            gameListAdapter.submitList(games)
        }

        gameViewModel.loadGames(2024, calendar.get(Calendar.MONTH) + 1)
        loadTeamNames()
    }

    private fun loadTeamNames() {
        gameViewModel.loadTeamNames(this)

        gameViewModel.teams.observe(this) { teams ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teams)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            teamFilterSpinner.adapter = adapter
        }
    }

    private fun changeMonth(amount: Int) {
        calendar.add(Calendar.MONTH, amount)
        if (calendar.get(Calendar.YEAR) != 2024) {
            calendar.add(Calendar.MONTH, -amount)
            return
        }
        updateMonthYearDisplay()
        gameViewModel.loadGames(2024, calendar.get(Calendar.MONTH) + 1)
    }

    private fun updateMonthYearDisplay() {
        val month = calendar.get(Calendar.MONTH) + 1
        tvMonthYear.text = String.format("2024. %02d", month)
    }

    private fun showDatePickerDialog() {
        val currentMonth = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateMonthYearDisplay()
                gameViewModel.loadGames(2024, month + 1)
            },
            2024, currentMonth, calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.findViewById<View>(
            resources.getIdentifier("android:id/day", null, null)
        )?.visibility = View.GONE

        val minDate = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 1)
        }.timeInMillis

        val maxDate = Calendar.getInstance().apply {
            set(2024, Calendar.NOVEMBER, 30)
        }.timeInMillis

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate = maxDate

        datePickerDialog.show()

        disableMondays(datePickerDialog)
    }

    private fun disableMondays(datePickerDialog: DatePickerDialog) {
        val calendarView = datePickerDialog.datePicker

        calendarView.setOnDateChangedListener { _, year, month, dayOfMonth ->
            val tempCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            if (tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                calendarView.updateDate(2024, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            }
        }

        calendarView.post {
            disableMondaysInCalendarView(calendarView)
        }
    }

    private fun disableMondaysInCalendarView(calendarView: DatePicker) {
        val currentYear = 2024
        val currentMonth = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            val tempCalendar = Calendar.getInstance().apply {
                set(currentYear, currentMonth, day)
            }

            if (tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                disableDayButton(calendarView, day)
            }
        }
    }

    private fun disableDayButton(calendarView: DatePicker, day: Int) {
        val dayPickerViewGroup = findDayPickerViewGroup(calendarView)
        if (dayPickerViewGroup != null) {
            for (i in 0 until dayPickerViewGroup.childCount) {
                val child = dayPickerViewGroup.getChildAt(i)
                if (child is ViewGroup) {
                    for (j in 0 until child.childCount) {
                        val dayButton = child.getChildAt(j)
                        if (dayButton is TextView && dayButton.text.toString() == day.toString()) {
                            dayButton.setTextColor(resources.getColor(R.color.gray, theme))
                            dayButton.background = null
                            dayButton.isClickable = false
                            dayButton.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun findDayPickerViewGroup(view: View): ViewGroup? {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is ViewGroup && child.javaClass.simpleName.contains("DayPickerView")) {
                    return child
                }
                val result = findDayPickerViewGroup(child)
                if (result != null) return result
            }
        }
        return null
    }
}
