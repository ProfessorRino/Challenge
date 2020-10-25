package com.example.challenge

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =[CurrencyQuote::class], version = 1)

abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao

    companion object {
        var INSTANCE: CurrencyDatabase? = null

        fun getCurrencyDataBase(context: Context): CurrencyDatabase? {
            if (INSTANCE == null) {
                synchronized(CurrencyDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CurrencyDatabase::class.java, "currencyDB"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}