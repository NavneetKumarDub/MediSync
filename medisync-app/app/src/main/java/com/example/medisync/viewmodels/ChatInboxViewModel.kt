package com.example.medisync.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.local.ChatInboxEntity
import com.example.medisync.data.repository.ChatInboxRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatInboxViewModel(
    private val repository: ChatInboxRepository
) : ViewModel() {

    val inboxChats: StateFlow<List<ChatInboxEntity>> = repository.allChats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun triggerSync(token: String) {
        viewModelScope.launch {
            repository.syncChats(token)
        }
    }

    class Factory(private val repository: ChatInboxRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatInboxViewModel(repository) as T
        }
    }
}