package com.example.playground

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencyquotes")
data class CurrencyQuote(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val targetHandle: String,
    val fullName: String,
    val timeStamp: Int,
    val usdRate: Float
)
