package com.somnium.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somnium.app.models.Dream
import com.somnium.app.repositories.DreamRepository
import kotlinx.coroutines.launch
import java.util.Date

class DreamViewModel : ViewModel() {
    
    private val repository = DreamRepository()
    
    private val _dreams = MutableLiveData<List<Dream>>()
    val dreams: LiveData<List<Dream>> = _dreams
    
    private val _selectedDream = MutableLiveData<Dream?>()
    val selectedDream: LiveData<Dream?> = _selectedDream
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Getter methods for Java compatibility
    fun getDreamsList(): LiveData<List<Dream>> = dreams
    fun getSelectedDreamValue(): LiveData<Dream?> = selectedDream
    fun isLoading(): LiveData<Boolean> = loading
    fun getErrorMessage(): LiveData<String?> = error
    
    fun loadUserDreams(userId: String) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getUserDreams(userId)
                .onSuccess { dreamList ->
                    _dreams.value = dreamList
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load dreams"
                    _loading.value = false
                }
        }
    }
    
    fun getDreamById(dreamId: String) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getDreamById(dreamId)
                .onSuccess { dream ->
                    _selectedDream.value = dream
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to get dream"
                    _loading.value = false
                }
        }
    }
    
    fun saveDream(title: String, content: String, userId: String, dreamDate: Date = Date()) {
        _loading.value = true
        _error.value = null
        
        val dream = Dream(
            id = null,
            user_id = userId,
            title = title,
            content = content,
            created_at = null,
            dream_date = Dream.formatDate(dreamDate)
        )
        
        viewModelScope.launch {
            repository.saveDream(dream)
                .onSuccess { savedDream ->
                    // Refresh the dream list
                    loadUserDreams(userId)
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to save dream"
                    _loading.value = false
                }
        }
    }
    
    // Overloaded function to save a Dream object directly
    fun saveDream(dream: Dream) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.saveDream(dream)
                .onSuccess { savedDream ->
                    // Refresh the dream list
                    dream.user_id?.let { loadUserDreams(it) }
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to save dream"
                    _loading.value = false
                }
        }
    }
    
    fun updateDream(dream: Dream) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.updateDream(dream)
                .onSuccess { updatedDream ->
                    _selectedDream.value = updatedDream
                    // Refresh the dream list
                    dream.user_id?.let { loadUserDreams(it) }
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to update dream"
                    _loading.value = false
                }
        }
    }
    
    fun deleteDream(dream: Dream) {
        _loading.value = true
        _error.value = null
        
        // Clear the selected dream immediately to prevent UI from accessing it
        _selectedDream.value = null
        
        viewModelScope.launch {
            dream.id?.let { dreamId ->
                repository.deleteDream(dreamId)
                    .onSuccess {
                        // Refresh the dream list
                        dream.user_id?.let { userId -> loadUserDreams(userId) }
                        _loading.value = false
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Failed to delete dream"
                        _loading.value = false
                    }
            } ?: run {
                _error.value = "Dream ID is null"
                _loading.value = false
            }
        }
    }
    
    fun saveDreamAnalysis(dreamId: String, analysis: com.somnium.app.ai.DreamAnalysis) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.saveAnalysis(dreamId, analysis)
                .onSuccess { updatedDream ->
                    _selectedDream.value = updatedDream
                    
                    // Refresh the dream list if we know the user ID
                    updatedDream.user_id?.let { userId -> loadUserDreams(userId) }
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to save analysis"
                    _loading.value = false
                }
        }
    }
    
    fun clearSelectedDream() {
        _selectedDream.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
} 