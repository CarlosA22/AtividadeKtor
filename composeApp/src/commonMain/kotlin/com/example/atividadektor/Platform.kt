package com.example.atividadektor

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform