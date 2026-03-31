package com.example.mental_health.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.MoodEntry
import com.example.mental_health.data.repository.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun saveMood(uid: String, mood: String, note: String, stressScore: Int = 0) {
        viewModelScope.launch {
            _isSaving.value = true
            val entry = MoodEntry(uid = uid, mood = mood, note = note, stressScore = stressScore)
            val result = repository.saveMood(entry)
            if (result.isSuccess) {
                _saveSuccess.value = true
            }
            _isSaving.value = false
        }
    }

    fun resetSuccess() {
        _saveSuccess.value = false
    }
}
