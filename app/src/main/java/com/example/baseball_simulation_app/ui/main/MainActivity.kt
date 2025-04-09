package com.example.baseball_simulation_app

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.baseball_simulation_app.databinding.ActivityMainBinding
import com.example.baseball_simulation_app.ui.calendar.DatePickerPopupManager
import com.example.baseball_simulation_app.ui.main.DailyGamesFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: DailyGamesPagerAdapter
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var datePickerPopupManager: DatePickerPopupManager
    private val datesList = mutableListOf<Pair<Date, String>>() // Date + Code

    // 상수로 제한할 월과 연도 정의
    companion object {
        private const val FIXED_YEAR = 2024
        private const val START_MONTH = Calendar.MARCH
        private const val START_DAY = 23
        private const val END_MONTH = Calendar.OCTOBER
        private const val END_DAY = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeCalendar()
        datePickerPopupManager = DatePickerPopupManager(this)

        setupDateNavigation()
        setupTeamFilter()
        setupViewPager()
    }

    private fun initializeCalendar() {
        // 2024년 3월 23일로 고정
        calendar.clear()
        calendar.set(FIXED_YEAR, START_MONTH, START_DAY)

        // 만약 이 날짜가 월요일이라면 다음 날로 이동
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun setupDateNavigation() {
        // 현재 날짜 설정
        updateDateText()

        // 이전 날 버튼 (버튼 이름 변경: 이전 달 -> 이전 날)
        binding.btnPrevMonth.setOnClickListener {
            navigateToPreviousDay()
        }

        // 다음 날 버튼 (버튼 이름 변경: 다음 달 -> 다음 날)
        binding.btnNextMonth.setOnClickListener {
            navigateToNextDay()
        }

        // 버튼 활성화/비활성화 상태 업데이트
        updateNavigationButtonStates()

        // 날짜 드롭다운 클릭 시 DatePicker 표시
        binding.tvCurrentDate.setOnClickListener { showDatePicker() }
        binding.btnDropdownDate.setOnClickListener { showDatePicker() }
    }

    private fun updateNavigationButtonStates() {
        val currentDate = calendar.time
        val startDate = Calendar.getInstance().apply {
            clear()
            set(FIXED_YEAR, START_MONTH, START_DAY, 0, 0, 0)
        }.time

        val endDate = Calendar.getInstance().apply {
            clear()
            set(FIXED_YEAR, END_MONTH, END_DAY, 23, 59, 59)
        }.time

        // 이전 날 버튼 상태 업데이트
        val isPrevEnabled = currentDate.after(startDate) || dateFormat.format(currentDate) == dateFormat.format(startDate)
        binding.btnPrevMonth.isEnabled = isPrevEnabled
        binding.btnPrevMonth.alpha = if (isPrevEnabled) 1.0f else 0.5f

        // 다음 날 버튼 상태 업데이트
        val isNextEnabled = currentDate.before(endDate) || dateFormat.format(currentDate) == dateFormat.format(endDate)
        binding.btnNextMonth.isEnabled = isNextEnabled
        binding.btnNextMonth.alpha = if (isNextEnabled) 1.0f else 0.5f
    }

    private fun showDatePicker() {
        // minDate와 maxDate 생성
        val minCalendar = Calendar.getInstance()
        minCalendar.set(FIXED_YEAR, START_MONTH, START_DAY, 0, 0, 0)
        val minDate = minCalendar.timeInMillis

        val maxCalendar = Calendar.getInstance()
        maxCalendar.set(FIXED_YEAR, END_MONTH, END_DAY, 23, 59, 59)
        val maxDate = maxCalendar.timeInMillis

        // DatePickerDialog 사용
        datePickerPopupManager.showDatePickerDialog(
            calendar,
            minDate,
            maxDate
        ) { year, month, day ->
            // 임시 캘린더 객체를 생성하여 선택한 날짜가 월요일인지 확인
            val tempCalendar = Calendar.getInstance()
            tempCalendar.set(year, month, day)

            // 월요일인 경우 처리
            if (tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                // 월요일 팝업 표시
                AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("월요일은 경기가 없습니다.")
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                        // 팝업 닫은 후 다음 날(화요일)로 이동
                        tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
                        calendar.time = tempCalendar.time
                        updateDateAndViewPager()
                    }
                    .create()
                    .show()
            } else {
                // 월요일이 아닌 경우 바로 해당 날짜로 설정
                calendar.set(year, month, day)
                updateDateAndViewPager()
            }
        }
    }

    // 날짜 업데이트 및 ViewPager 이동을 위한 메서드 추출
    private fun updateDateAndViewPager() {
        updateDateText()

        // 선택한 날짜로 ViewPager 이동
        val currentPosition = getPositionForDate(calendar.time)
        if (currentPosition != -1) {
            binding.viewPager.setCurrentItem(currentPosition, true)
        } else {
            // 유효하지 않은 날짜면 달력 재구성
            generateDatesList()
            setupViewPagerAdapter()

            // 현재 날짜에 맞는 포지션 찾기
            val positionAfterUpdate = getPositionForDate(calendar.time)
            binding.viewPager.setCurrentItem(
                if (positionAfterUpdate >= 0) positionAfterUpdate else 0,
                false
            )
        }
    }

    private fun updateDateText() {
        // 연, 월, 일 표시하도록 포맷 변경
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        // 버튼 상태 업데이트
        updateNavigationButtonStates()
    }

    private fun setupTeamFilter() {
        binding.cardTeamFilter.setOnClickListener {
            // 팀 필터 팝업 표시 (추후 구현)
            // showTeamFilterPopup()
        }
    }

    private fun setupViewPager() {
        generateDatesList()
        setupViewPagerAdapter()

        // ViewPager 페이지 변경 리스너
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val selectedDate = datesList[position].first // ← .first 추가!
                calendar.time = selectedDate
                updateDateText()
            }

        })
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun generateCodeForDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return "CODE_${sdf.format(date)}"
    }

    private fun generateDatesList() {
        datesList.clear()

        val startCal = Calendar.getInstance().apply {
            clear()
            set(FIXED_YEAR, START_MONTH, START_DAY)
        }

        val endCal = Calendar.getInstance().apply {
            clear()
            set(FIXED_YEAR, END_MONTH, END_DAY)
        }

        val tempCal = Calendar.getInstance().apply {
            time = startCal.time
        }

        while (tempCal.before(endCal) || tempCal.equals(endCal)) {
            if (tempCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                val date = tempCal.time
                val code = generateCodeForDate(date) // ↓ 추가
                datesList.add(date to code)
            }
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        datesList.sortBy { it.first.time }
    }

    private fun getPositionForDate(date: Date): Int {
        val targetDateStr = dateFormat.format(date)
        return datesList.indexOfFirst { dateFormat.format(it.first) == targetDateStr }
    }

    private fun setupViewPagerAdapter() {
        viewPagerAdapter = DailyGamesPagerAdapter(this, datesList)
        binding.viewPager.adapter = viewPagerAdapter

        // 3월 23일에 해당하는 포지션 찾기
        val startDatePosition = getPositionForDate(Calendar.getInstance().apply {
            clear()
            set(FIXED_YEAR, START_MONTH, START_DAY)
        }.time)

        // 시작 포지션 설정 (3월 23일)
        if (startDatePosition != -1) {
            binding.viewPager.setCurrentItem(startDatePosition, false)
        } else {
            binding.viewPager.setCurrentItem(0, false)
        }

        // 미리 로드할 페이지 수 설정
        binding.viewPager.offscreenPageLimit = 3
    }

    private fun navigateToPreviousDay() {
        val currentPosition = binding.viewPager.currentItem
        if (currentPosition > 0) {
            binding.viewPager.setCurrentItem(currentPosition - 1, true)
        }
    }

    private fun navigateToNextDay() {
        val currentPosition = binding.viewPager.currentItem
        if (currentPosition < viewPagerAdapter.itemCount - 1) {
            binding.viewPager.setCurrentItem(currentPosition + 1, true)
        }
    }

    // ViewPager2용 어댑터
    inner class DailyGamesPagerAdapter(fa: FragmentActivity, private val dates: List<Pair<Date, String>>) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = dates.size

        override fun createFragment(position: Int): Fragment {
            val (fragmentDate, code) = dates[position]
            return DailyGamesFragment.newInstance(fragmentDate, code) // ← code 넘기기
        }
    }
}