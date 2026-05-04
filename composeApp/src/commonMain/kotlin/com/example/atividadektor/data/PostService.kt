package com.example.atividadektor.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PostService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        return client.get("https://jsonplaceholder.typicode.com/posts") {
            parameter("_page", page)
            parameter("_limit", limit)
            if (userId != null) {
                parameter("userId", userId)
            }
        }.body()
    }
}
