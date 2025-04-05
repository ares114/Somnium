package com.somnium.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.somnium.app.databinding.ItemAnalyzedDreamCardBinding
import com.somnium.app.models.Dream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeAnalyzedDreamAdapter(
    private var dreams: List<Dream> = emptyList(),
    private val onDreamClicked: (Dream) -> Unit
) : RecyclerView.Adapter<HomeAnalyzedDreamAdapter.AnalyzedDreamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyzedDreamViewHolder {
        val binding = ItemAnalyzedDreamCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnalyzedDreamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnalyzedDreamViewHolder, position: Int) {
        val dream = dreams[position]
        holder.bind(dream)
    }

    override fun getItemCount(): Int = dreams.size

    fun updateDreams(newDreams: List<Dream>) {
        // Filter only dreams with analysis and take top 3
        dreams = newDreams.filter { it.hasAnalysis() }.take(3)
        notifyDataSetChanged()
    }

    inner class AnalyzedDreamViewHolder(private val binding: ItemAnalyzedDreamCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(dream: Dream) {
            binding.textViewDreamTitle.text = dream.title
            
            // Show analysis summary
            binding.textViewAnalysisSummary.text = dream.analysis_summary ?: "No analysis available"
            
            // Format the date for display
            binding.textViewDreamDate.text = formatDisplayDate(dream.dream_date)
            
            // Set up click listener for view analysis button
            binding.buttonViewAnalysis.setOnClickListener {
                onDreamClicked(dream)
            }
            
            // Set up click listener for the entire card
            binding.root.setOnClickListener {
                onDreamClicked(dream)
            }
        }
        
        private fun formatDisplayDate(dateString: String?): String {
            if (dateString == null) return ""
            
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString) ?: return ""
                return outputFormat.format(date)
            } catch (e: Exception) {
                return dateString
            }
        }
    }
} 