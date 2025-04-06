package com.example.baseball_simulation_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.data.model.TeamModel
import com.example.baseball_simulation_app.databinding.FragmentDailyGamesBinding
import com.example.baseball_simulation_app.ui.main.GameAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class DailyGamesFragment : Fragment() {

    private var _binding: FragmentDailyGamesBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameAdapter: GameAdapter
    private val gameList = mutableListOf<GameModel>()
    private var currentDate: Date? = null

    companion object {
        private const val ARG_DATE = "date"

        fun newInstance(date: Date): DailyGamesFragment {
            val fragment = DailyGamesFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentDate = it.getSerializable(ARG_DATE) as Date
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // 월요일인 경우 "경기가 없습니다" 메시지 표시
        val calendar = Calendar.getInstance()
        currentDate?.let { calendar.time = it }

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            // 월요일일 경우 경기 없음 표시
            binding.tvNoGames.visibility = View.VISIBLE
            binding.rvDailyGameList.visibility = View.GONE
        } else {
            // 월요일이 아닌 경우 게임 목록 표시
            binding.tvNoGames.visibility = View.GONE
            binding.rvDailyGameList.visibility = View.VISIBLE
            loadGamesForCurrentDate()
        }
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(gameList) { game ->
            // 경기 클릭 시 선수 교체 화면으로 이동
            (activity as? MainActivity)?.navigateToPlayerSwapScreen(game)
        }

        binding.rvDailyGameList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gameAdapter
        }
    }

    private fun loadGamesForCurrentDate() {
        // 현재 날짜에 맞는 게임 데이터 로드
        // 실제로는 API 또는 로컬 DB에서 데이터를 로드합니다
        gameList.clear()

        // 날짜 형식 변환
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
        val dateString = currentDate?.let { dateFormat.format(it) } ?: return

        // 더미 데이터 추가 (현재 날짜에 5개의 경기 데이터 생성)
        for (i in 1..5) {
            gameList.add(
                GameModel(
                    id = "$dateString-$i",
                    homeTeam = TeamModel("LG", "엘지", R.drawable.placeholder_logo),
                    awayTeam = TeamModel("KT", "KT", R.drawable.placeholder_logo),
                    homeScore = (1..10).random(),
                    awayScore = (1..10).random(),
                    stadium = "잠실",
                    date = dateString,
                    highlights = listOf("highlight1", "highlight2", "highlight3", "highlight4", "highlight5")
                )
            )
        }

        gameAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}