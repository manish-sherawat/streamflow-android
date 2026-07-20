package com.streamflow.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isAuthenticated = authRepository.isAuthenticated

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isRegisterMode = MutableStateFlow(false)
    val isRegisterMode: StateFlow<Boolean> = _isRegisterMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onEmailChanged(value: String) { _email.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }
    fun onDisplayNameChanged(value: String) { _displayName.value = value }

    fun toggleAuthMode() {
        _isRegisterMode.value = !_isRegisterMode.value
        _errorMessage.value = null
    }

    fun submit() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = if (_isRegisterMode.value) {
                authRepository.signup(_email.value, _password.value, _displayName.value)
            } else {
                authRepository.login(_email.value, _password.value)
            }
            _isLoading.value = false
            result.onFailure {
                _errorMessage.value = it.localizedMessage ?: "Authentication failed"
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.loginAsGuest()
            _isLoading.value = false
        }
    }
}
