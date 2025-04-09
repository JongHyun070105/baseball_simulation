package com.example.baseball_simulation_app.ui.main

import android.Manifest
import com.example.baseball_simulation_app.data.repository.GameRepository
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseball_simulation_app.data.model.GameModel
import com.example.baseball_simulation_app.network.NetworkResult
import com.example.baseball_simulation_app.databinding.FragmentDailyGamesBinding
import com.example.baseball_simulation_app.utils.DateUtils
import com.example.baseball_simulation_app.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DailyGamesFragment : Fragment() {
    private var _binding: FragmentDailyGamesBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameAdapter: GameAdapter
    private val gameRepository = GameRepository()
    private var currentDate: Date? = null

    companion object {
        private const val ARG_DATE = "date"
        private const val ARG_CODE = "code"

        fun newInstance(date: Date, code: String): DailyGamesFragment {
            val fragment = DailyGamesFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            args.putString(ARG_CODE, code)
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

        // 어댑터 초기화
        gameAdapter = GameAdapter { game ->
            // 게임 클릭 시 처리 (예: 상세 페이지로 이동)
            navigateToGameDetail(game)
        }

        setupRecyclerView()

        // 월요일인 경우 "경기가 없습니다" 메시지 표시
        val calendar = Calendar.getInstance()
        currentDate?.let { calendar.time = it }

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            // 월요일일 경우 경기 없음 표시
            showNoGamesMessage("월요일은 경기가 없습니다.")
        } else {
            // 월요일이 아닌 경우 게임 목록 로드
            loadGamesForCurrentDate()
        }
    }

    private fun setupRecyclerView() {
        binding.rvDailyGameList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gameAdapter
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun loadGamesForCurrentDate() {
        // 네트워크 연결 확인
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showNetworkError()
            return
        }

        // 로딩 표시
        showLoading(true)

        // 현재 선택된 날짜 사용
        val selectedDate = currentDate ?: Date()

        // API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = gameRepository.getGames(selectedDate)) {
                is NetworkResult.Success -> {
                    val games = result.data
                    if (games.isEmpty()) {
                        showNoGamesMessage("해당 날짜에 경기가 없습니다.")
                    } else {
                        updateGameList(games)
                    }
                }
                is NetworkResult.Error -> {
                    showError(result.message)
                }
                is NetworkResult.Loading -> {
                    // 이미 위에서 로딩 표시함
                }
            }
            showLoading(false)
        }
    }
    private fun updateGameList(games: List<GameModel>) {
        binding.tvNoGames.visibility = View.GONE
        binding.rvDailyGameList.visibility = View.VISIBLE
        gameAdapter.submitList(games)
    }

    private fun showNoGamesMessage(message: String) {
        binding.tvNoGames.text = message
        binding.tvNoGames.visibility = View.VISIBLE
        binding.rvDailyGameList.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showNetworkError() {
        binding.tvNoGames.text = "인터넷 연결이 없습니다.\n연결 후 다시 시도해주세요."
        binding.tvNoGames.visibility = View.VISIBLE
        binding.rvDailyGameList.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvNoGames.text = "데이터를 불러오는 데 실패했습니다."
        binding.tvNoGames.visibility = View.VISIBLE
        binding.rvDailyGameList.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToGameDetail(game: GameModel) {
        // TODO: 게임 상세 페이지로 이동 처리
        // 예: findNavController().navigate(R.id.action_dailyGamesFragment_to_gameDetailFragment, bundleOf("gameId" to game.id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}