package com.example.atividadektor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atividadektor.data.Post
import com.example.atividadektor.data.PostService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
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
            _uiState.value = PostUiState() // Reset state
            loadPosts()
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && !_uiState.value.endReached) {
            loadPosts()
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val newPosts = postService.getPosts(
                    page = _uiState.value.page,
                    limit = limit,
                    userId = currentUserId
                )
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts + newPosts,
                    isLoading = false,
                    endReached = newPosts.size < limit,
                    page = _uiState.value.page + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
