package com.somnium.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.somnium.app.R
import com.somnium.app.databinding.FragmentDreamDetailBinding
import com.somnium.app.models.Dream
import com.somnium.app.viewmodels.DreamViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DreamDetailFragment : Fragment(), MenuProvider {

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DreamViewModel
    private var currentDream: Dream? = null
    private var isEditMode = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[DreamViewModel::class.java]
        
        // Add menu provider for edit/delete options
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        viewModel.selectedDream.observe(viewLifecycleOwner) { dream ->
            dream?.let {
                currentDream = it
                displayDreamDetails(it)
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
        binding.buttonEdit.setOnClickListener {
            toggleEditMode()
        }
        
        binding.buttonSave.setOnClickListener {
            saveUpdatedDream()
        }
        
        binding.buttonCancel.setOnClickListener {
            toggleEditMode(false)
            currentDream?.let { displayDreamDetails(it) }
        }
        
        binding.buttonAnalyze.setOnClickListener {
            navigateToAnalysis()
        }
        
        binding.analysisIndicator.setOnClickListener {
            navigateToAnalysis()
        }
    }
    
    private fun displayDreamDetails(dream: Dream) {
        binding.textViewTitle.text = dream.title
        binding.editTextTitle.setText(dream.title)
        
        binding.textViewContent.text = dream.content
        binding.editTextContent.setText(dream.content)
        
        binding.textViewDate.text = dream.dream_date
        
        // Show analysis badge if analysis exists
        binding.analysisIndicator.visibility = if (dream.hasAnalysis()) View.VISIBLE else View.GONE
        
        updateViewVisibility(isEditMode)
    }
    
    private fun toggleEditMode(enable: Boolean = true) {
        isEditMode = enable
        updateViewVisibility(enable)
        
        if (enable) {
            // When entering edit mode, make sure the content EditText is visible and focused
            binding.editTextContent.apply {
                requestFocus()
                // Set the cursor at the end of the text
                setSelection(text.length)
            }
        }
    }
    
    private fun updateViewVisibility(editMode: Boolean) {

        binding.textViewTitle.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.scrollViewContent.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.textViewContent.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.textViewDate.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.buttonEdit.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.buttonAnalyze.visibility = if (editMode) View.GONE else View.VISIBLE

        binding.editTextTitle.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.scrollViewEdit.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.buttonSave.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.buttonCancel.visibility = if (editMode) View.VISIBLE else View.GONE
    }
    
    private fun saveUpdatedDream() {
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()
        
        if (title.isEmpty()) {
            binding.editTextTitle.error = "Title is required"
            return
        }
        
        if (content.isEmpty()) {
            binding.editTextContent.error = "Content is required"
            return
        }
        
        currentDream?.let { dream ->
            val updatedDream = Dream(
                id = dream.id,
                user_id = dream.user_id,
                title = title,
                content = content,
                created_at = dream.created_at,
                dream_date = dream.dream_date
            )
            
            viewModel.updateDream(updatedDream)
            toggleEditMode(false)
        }
    }
    
    private fun navigateToAnalysis() {
        val analysisFragment = DreamAnalysisFragment()
        
        // Get current dream ID to load in the analysis fragment
        val dreamId = currentDream?.id
        val bundle = Bundle()
        bundle.putString("dream_id", dreamId)
        analysisFragment.arguments = bundle
        
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, analysisFragment)
            .addToBackStack("dream_analysis")
            .commit()
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_dream_detail, menu)
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                deleteDream()
                true
            }
            else -> false
        }
    }
    
    private fun deleteDream() {
        currentDream?.let { dream ->
            viewModel.deleteDream(dream)
            // Clear the selected dream to prevent accessing it after deletion
            viewModel.clearSelectedDream()
            
            // Safely navigate back
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                // If navigation fails, use FragmentManager instead
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 