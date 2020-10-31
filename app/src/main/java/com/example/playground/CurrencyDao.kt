package com.example.playground

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(currency: CurrencyQuote)

    @Delete
    fun delete(currency: CurrencyQuote)

    @Update
    fun update(currency: CurrencyQuote)

    @Query("Select * from currencyquotes")
    fun getQuotesLiveData(): LiveData<List<CurrencyQuote>>

    @Update(entity = CurrencyQuote::class)
    fun updateRate(update: RateUpdate)

    @Entity(tableName = "quotes")
    class RateUpdate(
        val id: Int,
        var timeStamp: Int?,
        var usdRate: Float?
    )

    @Transaction
    fun upsertCurrencyQuotes(currencies: Map<String, String>, timeStamp: Int, quotes: Map<String, Float>){
        var id = 1
        for (pair in currencies) {
            val currency = CurrencyQuote(id, pair.key, pair.value,null,null)
            insert(currency)
            id += 1
        }
        id = 1
        for (quote in quotes) {
            val rateUpdate = RateUpdate(id,  timeStamp, quote.value)
            updateRate(rateUpdate)
            id += 1
        }
    }
}



