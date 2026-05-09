package com.example.medisync.viewmodels

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    private val _isPhotoLoading = MutableStateFlow(false)
    val isPhotoLoading: StateFlow<Boolean> = _isPhotoLoading.asStateFlow()


    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val id = TokenManager.getUserId(context) ?: return@launch
                _userId.value = id
                fetchProfilePhoto(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchProfilePhoto(userId: Int) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val token = "Bearer ${TokenManager.getToken(context) ?: ""}"
                _isPhotoLoading.value = true
                val response = RetrofitInstance.api.getProfilePhotoUrl(token,userId)
                _profilePhotoUrl.value = response.viewUrl
            } catch (e: Exception) {
                _profilePhotoUrl.value = null
            } finally {
                _isPhotoLoading.value = false
            }
        }
    }

    fun updateProfilePhotoUrl(url: String?) {
        _profilePhotoUrl.value = url
    }

    fun refreshProfilePhoto() {
        viewModelScope.launch {
            val id = _userId.value ?: return@launch
            fetchProfilePhoto(id)
        }
    }
}