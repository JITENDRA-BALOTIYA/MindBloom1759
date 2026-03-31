package com.example.mental_health.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.User
import com.example.mental_health.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        repository.currentUser?.let {
            fetchUserDetails(it.uid)
        }
    }

    // ─── Register ─────────────────────────────────────────────────────────────
    // ALL fields from RegisterScreen are now passed here
    fun registerStudent(
        name: String,
        age: Int,
        gender: String,             // ← added
        collegeName: String,
        course: String,
        year: String,               // ← added
        rollNumber: String = "",    // ← added (optional)
        parentName: String,         // ← added
        parentPhone: String,
        emergencyContact: String,   // ← added
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = withContext(Dispatchers.IO) {
                repository.registerUser(
                    name             = name,
                    age              = age,
                    gender           = gender,
                    collegeName      = collegeName,
                    course           = course,
                    year             = year,
                    rollNumber       = rollNumber,
                    parentName       = parentName,
                    parentPhone      = parentPhone,
                    emergencyContact = emergencyContact,
                    email            = email,
                    password         = password
                )
            }

            if (result.isSuccess) {
                _authState.value = AuthState.Success
                repository.currentUser?.let { fetchUserDetails(it.uid) }
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Registration failed"
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = withContext(Dispatchers.IO) {
                repository.login(email, password)
            }

            if (result.isSuccess) {
                repository.currentUser?.let { fetchUserDetails(it.uid) }
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    // ─── Fetch User ───────────────────────────────────────────────────────────
    private fun fetchUserDetails(uid: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getUserDetails(uid)
            }
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value   = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

// ─── Auth State ───────────────────────────────────────────────────────────────
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}