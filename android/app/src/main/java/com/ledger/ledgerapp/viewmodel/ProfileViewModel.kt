package com.ledger.ledgerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.ledger.ledgerapp.network.RetrofitClient
import com.ledger.ledgerapp.network.models.UserUpdate
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class ProfileUiState(
    val username: String? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadSuccess: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val apiService = RetrofitClient.createAuthenticatedApiService(tokenManager)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    fun resetUploadSuccess() {
        _uiState.value = _uiState.value.copy(uploadSuccess = false)
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Try to get from API first for latest data including avatar
                val response = apiService.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        username = user.username,
                        avatarUrl = user.avatarUrl,
                        isLoading = false
                    )
                } else {
                    // Fallback to token if API fails
                    val token = tokenManager.getToken()
                    if (token != null) {
                        val username = JwtUtils.getUsernameFromToken(token)
                        _uiState.value = _uiState.value.copy(
                            username = username,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateAvatar(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Upload Image
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                
                val uploadResponse = apiService.uploadImage(body)
                
                if (uploadResponse.isSuccessful) {
                    val imagePath = uploadResponse.body()?.get("url")
                    
                    if (imagePath != null) {
                        // 2. Update User Profile
                        val updateResponse = apiService.updateCurrentUser(UserUpdate(avatarUrl = imagePath))
                        if (updateResponse.isSuccessful) {
                            _uiState.value = _uiState.value.copy(
                                avatarUrl = imagePath,
                                isLoading = false,
                                uploadSuccess = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to update profile")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to get image path")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to upload image")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}