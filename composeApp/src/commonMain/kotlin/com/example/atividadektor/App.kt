package com.example.atividadektor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.atividadektor.ui.PostViewModel
import com.example.atividadektor.data.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ktor Posts KMP") },
                    actions = {
                        val viewModel: PostViewModel = viewModel { PostViewModel() }
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text("Atualizar", color = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                val viewModel: PostViewModel = viewModel { PostViewModel() }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    var userIdInput by remember { mutableStateOf("") }
                    
                    TextField(
                        value = userIdInput,
                        onValueChange = { 
                            userIdInput = it
                            viewModel.onUserIdChanged(it)
                        },
                        label = { Text("Filtrar por User ID") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        singleLine = true,
                        placeholder = { Text("Ex: 1") }
                    )

                    if (uiState.posts.isEmpty() && uiState.isLoading && !uiState.isRefreshing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.posts.isEmpty() && uiState.error != null) {
                        ErrorView(message = uiState.error!!, onRetry = { viewModel.retry() })
                    } else {
                        PostList(
                            posts = uiState.posts,
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onLoadMore = { viewModel.loadNextPage() },
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Tentar Novamente")
        }
    }
}

@Composable
fun PostList(
    posts: List<Post>,
    isLoading: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && error == null) {
            onLoadMore()
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(posts, key = { it.id }) { post ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = post.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("User ID: ${post.userId}") },
                        colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }

        if (error != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Tentar Novamente", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
