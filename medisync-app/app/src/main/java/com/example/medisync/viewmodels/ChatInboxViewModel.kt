package com.example.medisync.viewmodels // Adjust this to match your folder structure

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.InboxChat
import com.example.medisync.networks.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


// 1. The UI State Manager
sealed class InboxUiState {
    object Loading : InboxUiState()
    data class Success(val chats: List<InboxChat>) : InboxUiState()
    data class Error(val message: String) : InboxUiState()
}

// 2. The ViewModel (The Brains)
class ChatInboxViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    fun fetchInbox(context: Context) {
        viewModelScope.launch {
            _uiState.value = InboxUiState.Loading

            try {
                val token = TokenManager.getToken(context)
                if (token == null) {
                    _uiState.value = InboxUiState.Error("Please log in again.")
                    return@launch
                }

                val response = RetrofitInstance.api.getInbox(token = "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val chatsList = response.body()!!.chats
                    _uiState.value = InboxUiState.Success(chatsList)
                } else {
                    _uiState.value = InboxUiState.Error("Failed to load chats (${response.code()})")
                }

            } catch (e: IOException) {
                _uiState.value = InboxUiState.Error("Network error. Check your connection.")
            } catch (e: HttpException) {
                _uiState.value = InboxUiState.Error("Server error occurred.")
            } catch (e: Exception) {
                Log.e("ChatInboxViewModel", "Error fetching inbox", e)
                _uiState.value = InboxUiState.Error("An unexpected error occurred.")
            }
        }
    }
}