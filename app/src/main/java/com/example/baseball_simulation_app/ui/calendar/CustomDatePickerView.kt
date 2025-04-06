package com.example.baseball_simulation_app.ui.calendar

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import com.example.baseball_simulation_app.R
import java.util.Calendar

class CustomDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val datePicker: DatePicker
    private var onDateSelectedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

    // 유효한 연도와 월 정의
    companion object {
        private const val FIXED_YEAR = 2024
        private val VALID_MONTHS = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11) // 3월부터 11월까지만 허용
    }

    init {
        // XML 레이아웃을 inflate
        LayoutInflater.from(context).inflate(R.layout.custom_date_picker, this, true)
        datePicker = findViewById(R.id.datePicker)

        setupDatePicker()
    }

    private fun setupDatePicker() {
        // 연도를 2024년으로 고정 (year 프로퍼티에 직접 할당하지 않고 updateDate 사용)
        datePicker.updateDate(FIXED_YEAR, datePicker.month, datePicker.dayOfMonth)

        // 연도 변경 불가능하게 설정
        datePicker.findViewById<View>(Resources.getSystem().getIdentifier("year", "id", "android"))?.isEnabled = false

        // 최소/최대 날짜 설정
        val minMonth = VALID_MONTHS.minOrNull() ?: 3
        val maxMonth = VALID_MONTHS.maxOrNull() ?: 11

        val minDate = Calendar.getInstance().apply {
            set(FIXED_YEAR, minMonth - 1, 1) // 3월 1일
        }

        val maxDate = Calendar.getInstance().apply {
            set(FIXED_YEAR, maxMonth - 1, 1) // 11월 1일
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        datePicker.minDate = minDate.timeInMillis
        datePicker.maxDate = maxDate.timeInMillis

        // 날짜 변경 리스너 설정
        datePicker.init(
            FIXED_YEAR,  // 고정된 연도 사용
            datePicker.month,
            datePicker.dayOfMonth
        ) { _, year, monthOfYear, dayOfMonth ->
            // 나머지 코드는 그대로 유지
            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, monthOfYear, dayOfMonth)

            if (selectedCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                onDateSelectedListener?.invoke(year, monthOfYear, dayOfMonth)
            } else {
                // 월요일을 선택한 경우 이전 선택으로 되돌림
                val lastValidDate = findLastValidDate(selectedCal)
                datePicker.updateDate(
                    lastValidDate.get(Calendar.YEAR),
                    lastValidDate.get(Calendar.MONTH),
                    lastValidDate.get(Calendar.DAY_OF_MONTH)
                )
            }
        }
    }

    // 마지막으로 유효한 날짜 찾기 (월요일이 아닌 날)
    private fun findLastValidDate(calendar: Calendar): Calendar {
        val lastValidDate = Calendar.getInstance()
        lastValidDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // 현재 날짜가 월요일이면 하루 전으로 설정
        if (lastValidDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            lastValidDate.add(Calendar.DAY_OF_MONTH, -1)
        }

        return lastValidDate
    }

    // 날짜 선택 리스너 설정 메서드
    fun setOnDateSelectedListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        onDateSelectedListener = listener
    }

    // 현재 날짜 설정
    fun setDate(year: Int, month: Int, day: Int) {
        datePicker.updateDate(year, month, day)
    }
}