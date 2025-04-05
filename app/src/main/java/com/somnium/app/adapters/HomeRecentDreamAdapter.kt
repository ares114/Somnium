package com.somnium.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.somnium.app.databinding.ItemDreamCardBinding
import com.somnium.app.models.Dream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeRecentDreamAdapter(
    private var dreams: List<Dream> = emptyList(),
    private val onDreamClicked: (Dream) -> Unit
) : RecyclerView.Adapter<HomeRecentDreamAdapter.DreamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamViewHolder {
        val binding = ItemDreamCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DreamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DreamViewHolder, position: Int) {
        val dream = dreams[position]
        holder.bind(dream)
    }

    override fun getItemCount(): Int = dreams.size

    fun updateDreams(newDreams: List<Dream>) {
        dreams = newDreams.take(3) // Only show up to 3 recent dreams
        notifyDataSetChanged()
    }

    inner class DreamViewHolder(private val binding: ItemDreamCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(dream: Dream) {
            binding.textViewDreamTitle.text = dream.title
            
            // Format the content as a preview
            binding.textViewDreamContent.text = if (dream.content.length > 100) {
                "${dream.content.take(100)}..."
            } else {
                dream.content
            }
            
            // Format the date for display
            binding.textViewDreamDate.text = formatDisplayDate(dream.dream_date)
            
            // Set up click listener for the entire card
            binding.root.setOnClickListener {
                onDreamClicked(dream)
            }
            
            // Set up click listener for view button
            binding.buttonViewDream.setOnClickListener {
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