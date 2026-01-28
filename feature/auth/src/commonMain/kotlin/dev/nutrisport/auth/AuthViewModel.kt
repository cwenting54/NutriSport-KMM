package dev.nutrisport.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import dev.nutrisport.data.domain.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val customerRepository: CustomerRepository
): ViewModel() {
    private val _snackbarMsg = MutableStateFlow<String?>(null)
    val snackbarMsg: StateFlow<String?> = _snackbarMsg.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun createCustomer(
        user: FirebaseUser?,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            customerRepository.createCustomer(
                user = user,
                onSuccess = {
                    _isLoading.value = false
                    onSuccess()
                },
                onError = {
                    _isLoading.value = false
                    updateSnackBar(it)
                }
            )
        }
    }

    private fun updateSnackBar(message: String) {
        viewModelScope.launch {
            _snackbarMsg.value = message
        }
    }

    fun onSnackbarShown() {
        _snackbarMsg.value = null
    }

    fun handleSignInError(error: Throwable) {
        _isLoading.value = false
        val message = when {
            error.message?.contains("A network error") == true -> "Internet connection unavailable."
            error.message?.contains("Idtoken is null") == true -> "Sign in cancelled."
            else -> error.message ?: "Unknown error"
        }

        viewModelScope.launch {
            _snackbarMsg.value = message
        }
    }
}

