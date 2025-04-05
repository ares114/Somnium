package com.somnium.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.somnium.app.R
import com.somnium.app.adapters.DreamAdapter
import com.somnium.app.databinding.FragmentDreamListBinding
import com.somnium.app.viewmodels.DreamViewModel

class DreamListFragment : Fragment() {

    private var _binding: FragmentDreamListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DreamViewModel
    private lateinit var adapter: DreamAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        // Load dreams for current user
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            viewModel.loadUserDreams(userId)
        } ?: run {
            // Handle user not logged in
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_dreamListFragment_to_loginFragment)
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity())[DreamViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = DreamAdapter(
            dreams = emptyList(),
            onDreamClicked = { dream ->
                // Handle dream selection
                viewModel.getDreamById(dream.id!!)
                findNavController().navigate(R.id.action_dreamListFragment_to_dreamDetailFragment)
            }
        )
        
        binding.recyclerViewDreams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DreamListFragment.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.dreams.observe(viewLifecycleOwner) { dreams ->
            adapter.updateDreams(dreams)
            binding.progressBar.visibility = View.GONE
            
            if (dreams.isEmpty()) {
                binding.textViewNoDreams.visibility = View.VISIBLE
            } else {
                binding.textViewNoDreams.visibility = View.GONE
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun setupListeners() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_dreamListFragment_to_addDreamFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 