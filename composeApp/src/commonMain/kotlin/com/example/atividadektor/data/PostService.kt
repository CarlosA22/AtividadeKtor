package com.example.atividadektor.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.io.IOException
import kotlinx.coroutines.CancellationException

class PostService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        return try {
            client.get("https://jsonplaceholder.typicode.com/posts") {
                parameter("_page", page)
                parameter("_limit", limit)
                if (userId != null) {
                    parameter("userId", userId)
                }
                expectSuccess = true
            }.body()
        } catch (e: ClientRequestException) {
            val status = e.response.status
            throw Exception("Erro no Cliente (${status.value}): ${status.description}")
        } catch (e: ServerResponseException) {
            val status = e.response.status
            throw Exception("Erro no Servidor (${status.value}): O serviço está temporariamente indisponível.")
        } catch (e: IOException) {
            throw Exception("Falha de Conexão: Verifique sua internet.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("Erro inesperado: ${e.message}")
        }
    }
}
