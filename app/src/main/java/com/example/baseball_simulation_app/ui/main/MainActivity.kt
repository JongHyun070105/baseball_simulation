package com.example.baseball_simulation_app

import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.data.model.TeamModel
import com.example.baseball_simulation_app.databinding.ActivityMainBinding
import com.example.baseball_simulation_app.ui.calendar.DatePickerPopupManager
import com.example.baseball_simulation_app.ui.main.GameAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: DailyGamesPagerAdapter
    private val calendar = Calendar.getInstance()
    private lateinit var datePickerPopupManager: DatePickerPopupManager
    private val datesList = mutableListOf<Date>()
    private val DAYS_TO_LOAD = 60 // 앞뒤로 로딩할 날짜 수

    // 상수로 제한할 월과 연도 정의
    companion object {
        private const val FIXED_YEAR = 2024
        private val VALID_MONTHS = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11) // 3월부터 11월까지만 허용
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 연도를 2024년으로 고정
        calendar.set(Calendar.YEAR, FIXED_YEAR)

        // DatePicker 팝업 매니저 초기화
        datePickerPopupManager = DatePickerPopupManager(this)

        // 만약 현재 월이 유효하지 않은 월이라면, 가장 가까운 유효한 월로 설정
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작
        if (!VALID_MONTHS.contains(currentMonth)) {
            calendar.set(Calendar.MONTH, VALID_MONTHS[0] - 1) // 첫 번째 유효한 월로 설정
        }

        // 월요일인 경우 다음 날로 설정
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        setupDateNavigation()
        setupTeamFilter()
        setupViewPager()
    }

    private fun setupDateNavigation() {
        // 현재 날짜 설정
        updateDateText()

        // 이전 달 버튼
        binding.btnPrevMonth.setOnClickListener {
            navigateToPreviousDay()
        }

        // 다음 달 버튼
        binding.btnNextMonth.setOnClickListener {
            navigateToNextDay()
        }

        // 버튼 활성화/비활성화 상태 업데이트
        updateNavigationButtonStates()

        // 날짜 드롭다운 클릭 시 DatePicker 표시
        binding.tvCurrentDate.setOnClickListener { showDatePicker() }
        binding.btnDropdownDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        // DatePickerDialog 사용
        datePickerPopupManager.showDatePickerDialog(calendar) { year, month, day ->
            val previousDate = calendar.time
            calendar.set(year, month, day)

            updateDateText()

            // 선택한 날짜로 ViewPager 이동
            val position = getPositionForDate(calendar.time)
            if (position != -1) {
                binding.viewPager.setCurrentItem(position, true)
            } else {
                // 유효하지 않은 날짜면 달력 재구성
                generateDatesList()
                setupViewPagerAdapter()
                binding.viewPager.setCurrentItem(DAYS_TO_LOAD, false) // 중간 위치로 설정
            }
        }
    }

    private fun updateNavigationButtonStates() {
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        // 이전 달 버튼 상태 업데이트
        binding.btnPrevMonth.isEnabled = currentMonth >= VALID_MONTHS.minOrNull()!!
        binding.btnPrevMonth.alpha = if (binding.btnPrevMonth.isEnabled) 1.0f else 0.5f

        // 다음 달 버튼 상태 업데이트
        binding.btnNextMonth.isEnabled = currentMonth <= VALID_MONTHS.maxOrNull()!!
        binding.btnNextMonth.alpha = if (binding.btnNextMonth.isEnabled) 1.0f else 0.5f
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
                val selectedDate = datesList[position]

                // 달력 시간 업데이트
                calendar.time = selectedDate
                updateDateText()
            }
        })
    }

    private fun generateDatesList() {
        datesList.clear()

        // 현재 날짜를 기준으로 앞뒤로 DAYS_TO_LOAD일만큼 날짜 생성
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = calendar.time

        // 현재 날짜 이전의 날짜들 추가
        tempCalendar.add(Calendar.DAY_OF_YEAR, -DAYS_TO_LOAD)
        for (i in 0 until DAYS_TO_LOAD) {
            // 월요일이 아닌 날짜만 추가
            if (tempCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                datesList.add(tempCalendar.time)
            }
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 현재 날짜 추가 (현재 날짜가 월요일이 아닌 경우에만)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            datesList.add(calendar.time)
        }

        // 현재 날짜 이후의 날짜들 추가
        tempCalendar.time = calendar.time
        tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        for (i in 0 until DAYS_TO_LOAD) {
            // 월요일이 아닌 날짜만 추가
            if (tempCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                datesList.add(tempCalendar.time)
            }
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // MainActivity.kt 파일에 이 함수 추가
    fun navigateToPlayerSwapScreen(game: GameModel) {
        // 선수 교체 화면으로 이동하는 코드
        // Intent를 사용하여 PlayerSwapActivity로 이동
        // val intent = Intent(this, PlayerSwapActivity::class.java)
        // intent.putExtra("GAME_ID", game.id)
        // startActivity(intent)

        // 구현 예정
    }

    private fun setupViewPagerAdapter() {
        viewPagerAdapter = DailyGamesPagerAdapter(this, datesList)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.setCurrentItem(DAYS_TO_LOAD, false) // 중간 위치에서 시작 (현재 날짜)

        // 미리 로드할 페이지 수 설정
        binding.viewPager.offscreenPageLimit = 3
    }

    private fun getPositionForDate(date: Date): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDateStr = dateFormat.format(date)

        datesList.forEachIndexed { index, currentDate ->
            if (dateFormat.format(currentDate) == targetDateStr) {
                return index
            }
        }
        return -1
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
    inner class DailyGamesPagerAdapter(fa: FragmentActivity, private val dates: List<Date>) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = dates.size

        override fun createFragment(position: Int): Fragment {
            return DailyGamesFragment.newInstance(dates[position])
        }
    }
}