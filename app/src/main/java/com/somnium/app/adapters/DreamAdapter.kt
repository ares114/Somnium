package com.somnium.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.somnium.app.databinding.ItemDreamBinding
import com.somnium.app.models.Dream
import java.text.SimpleDateFormat
import java.util.Locale

class DreamAdapter(
    private var dreams: List<Dream>,
    private val onDreamClicked: (Dream) -> Unit
) : RecyclerView.Adapter<DreamAdapter.DreamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamViewHolder {
        val binding = ItemDreamBinding.inflate(
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
        dreams = newDreams
        notifyDataSetChanged()
    }

    inner class DreamViewHolder(private val binding: ItemDreamBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(dream: Dream) {
            binding.textViewTitle.text = dream.title
            
            // Format the content as a preview (first 50 characters)
            binding.textViewContent.text = if (dream.content.length > 50) {
                "${dream.content.take(50)}..."
            } else {
                dream.content
            }
            
            // Format the date for display
            binding.textViewDate.text = dream.dream_date
            
            binding.root.setOnClickListener {
                onDreamClicked(dream)
            }
        }
    }
} 