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

    @Transaction
    fun insertCurrencyQuotes(
        currencies: Map<String, String>,
        timeStamp: Int,
        quotes: Map<String, Float>
    ) {
        var id = 1
        for (quote in quotes) {
            insert(
                CurrencyQuote(
                    id,
                    quote.key.substring(3),
                    currencies.get(quote.key.substring(3)).toString(),
                    timeStamp,
                    quote.value
                )
            )
            id += 1
        }
    }
}



