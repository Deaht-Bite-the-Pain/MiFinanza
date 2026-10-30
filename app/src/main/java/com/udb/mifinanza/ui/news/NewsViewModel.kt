package com.udb.mifinanza.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udb.mifinanza.data.NewsCategory
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
    val error: String? = null,
    /** Enlace de la noticia cuyo cuerpo se cargó (o se está cargando) para el detalle. */
    val articleLink: String? = null,
    val articleParagraphs: List<String> = emptyList(),
    val articleLoading: Boolean = false
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

    /** Descarga el cuerpo de una noticia; los eventos no tienen cuerpo. Si falla, el detalle muestra el resumen. */
    fun loadArticle(item: NewsItem) {
        if (item.category != NewsCategory.NOTICIAS || _state.value.articleLink == item.link) return
        _state.update { it.copy(articleLink = item.link, articleParagraphs = emptyList(), articleLoading = true) }
        viewModelScope.launch {
            val result = repository.loadArticle(item.link)
            _state.update {
                if (it.articleLink != item.link) return@update it
                it.copy(
                    articleLink = if (result.isSuccess) item.link else null,
                    articleParagraphs = result.getOrNull()?.paragraphs.orEmpty(),
                    articleLoading = false
                )
            }
        }
    }
}
