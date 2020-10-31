package com.example.playground

class LiveResponse(
    val success: Boolean,
    val terms: String,
    val privacy: String,
    val timestamp: Int,
    val source: String,
    val quotes: Map<String, Float>
)