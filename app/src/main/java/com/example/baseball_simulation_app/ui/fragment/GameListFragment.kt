package com.example.baseball_simulation_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseball_simulation_app.databinding.FragmentGameListBinding
import com.example.baseball_simulation_app.ui.main.GameListAdapter
import com.example.baseball_simulation_app.viewmodel.GameViewModel
import androidx.fragment.app.activityViewModels


class GameListFragment : Fragment() {

    private var _binding: FragmentGameListBinding? = null
    private val binding get() = _binding!!
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = GameListAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        gameViewModel.games.observe(viewLifecycleOwner) { games ->
            adapter.submitList(games)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
