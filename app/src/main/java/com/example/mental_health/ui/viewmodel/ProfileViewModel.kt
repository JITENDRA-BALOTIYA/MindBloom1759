package com.example.mental_health.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.User
import com.example.mental_health.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = repository.currentUser?.uid ?: return
        viewModelScope.launch {
            _loading.value = true
            repository.getUserDetails(uid).onSuccess { user ->
                _userState.value = user
            }.onFailure {
                // Handle error
            }
            _loading.value = false
        }
    }

    fun logout() {
        repository.logout()
    }
}
