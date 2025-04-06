package com.example.baseball_simulation_app.ui.calendar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.widget.CalendarView
import android.widget.DatePicker
import java.util.Calendar

/**
 * DatePicker Dialog 에서 월요일을 회색으로 표시하는 클래스
 */
class MondayDateDecorator(private val context: Context) {

    /**
     * DatePickerDialog를 생성하고 월요일을 비활성화합니다
     */
    fun createDatePickerDialog(
        initialYear: Int,
        initialMonth: Int,
        initialDay: Int,
        onDateSet: (year: Int, month: Int, day: Int) -> Unit
    ): DatePickerDialog {

        // 현재 선택된 날짜가 월요일인지 확인
        val calendar = Calendar.getInstance()
        calendar.set(initialYear, initialMonth, initialDay)

        // 월요일이면 이전 날짜(일요일)로 설정
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        // DatePickerDialog 생성
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                // 월요일인 경우 팝업 메시지 표시
                if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    showNoGameOnMondayDialog()
                } else {
                    // 월요일이 아닌 경우에만 콜백 호출
                    onDateSet(year, month, dayOfMonth)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 연도 제한 (2024년만)
        datePickerDialog.datePicker.minDate = Calendar.getInstance().apply {
            set(2024, 2, 1) // 2024년 3월 1일
        }.timeInMillis

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().apply {
            set(2024, 10, 30) // 2024년 11월 30일
        }.timeInMillis

        // 월요일 비활성화 설정 (다이얼로그 표시 후)
        datePickerDialog.setOnShowListener {
            disableMondaysInDatePicker(datePickerDialog.datePicker)
        }

        return datePickerDialog
    }

    /**
     * DatePicker에서 CalendarView를 찾아 월요일을 비활성화하는 메소드
     */
    private fun disableMondaysInDatePicker(datePicker: DatePicker) {
        // CalendarView 찾기
        val calendarView = findCalendarView(datePicker)

        calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // 월요일인 경우 팝업 메시지 표시 후 선택 취소
            if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                showNoGameOnMondayDialog()

                // 이전 선택 날짜로 돌아가기
                val lastValidDate = Calendar.getInstance()
                lastValidDate.timeInMillis = calendarView.date

                if (lastValidDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    calendarView.date = lastValidDate.timeInMillis
                } else {
                    // 이전 선택도 월요일이면 일요일로 설정
                    lastValidDate.add(Calendar.DAY_OF_MONTH, -1)
                    calendarView.date = lastValidDate.timeInMillis
                }
            }
        }
    }

    /**
     * DatePicker에서 CalendarView를 찾는 메소드
     */
    private fun findCalendarView(datePicker: DatePicker): CalendarView? {
        // DatePicker에서 CalendarView ID 찾기
        val calendarViewId = context.resources.getIdentifier("calendar", "id", "android")
        return datePicker.findViewById(calendarViewId)
    }

    /**
     * 월요일에는 경기가 없음을 알리는 팝업 다이얼로그
     */
    private fun showNoGameOnMondayDialog() {
        AlertDialog.Builder(context)
            .setTitle("알림")
            .setMessage("월요일은 경기가 없습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}