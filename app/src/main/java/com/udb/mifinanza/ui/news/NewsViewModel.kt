package com.udb.mifinanza.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udb.mifinanza.data.NewsItem
import com.udb.mifinanza.data.NewsRepository
import com.udb.mifinanza.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = false,
    val loaded: Boolean = false,
    val items: List<NewsItem> = emptyList(),
    val error: String? = null
)

class NewsViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    fun load() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadNews()
                .onSuccess { items -> _state.update { it.copy(isLoading = false, loaded = true, items = items) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
        }
    }
}
