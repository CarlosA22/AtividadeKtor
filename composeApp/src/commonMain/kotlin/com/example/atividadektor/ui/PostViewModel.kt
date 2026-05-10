package com.example.atividadektor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atividadektor.data.Post
import com.example.atividadektor.data.PostService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val page: Int = 1
)

class PostViewModel(private val postService: PostService = PostService()) : ViewModel() {
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null
    private val limit = 10

    init {
        loadPosts()
    }

    fun onUserIdChanged(userIdString: String) {
        val userId = userIdString.toIntOrNull()
        if (userId != currentUserId) {
            currentUserId = userId
            _uiState.update { PostUiState(page = 1) }
            loadPosts()
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && !_uiState.value.endReached && _uiState.value.error == null) {
            loadPosts()
        }
    }

    fun retry() {
        _uiState.update { it.copy(error = null) }
        loadPosts()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, posts = emptyList(), page = 1, endReached = false, error = null) }
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val newPosts = postService.getPosts(
                    page = _uiState.value.page,
                    limit = limit,
                    userId = currentUserId
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        posts = if (currentState.page == 1) newPosts else currentState.posts + newPosts,
                        isLoading = false,
                        isRefreshing = false,
                        endReached = newPosts.size < limit,
                        page = currentState.page + 1,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Erro desconhecido"
                    )
                }
            }
        }
    }
}
