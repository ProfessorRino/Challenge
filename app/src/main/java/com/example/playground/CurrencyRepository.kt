package com.example.playground

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject


class CurrencyRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private var currencyDao: CurrencyDao
    var disposable: Disposable? = null
    private val listService by lazy {
        createEndpoints()
    }

    companion object {

        const val BASE = "http://api.currencylayer.com"
        const val KEY = "FREE_KEY_GOES_HERE"
        const val LIST = "list"
        const val LIVE = "live"

        fun createEndpoints(): Endpoints {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .baseUrl(BASE)
                .build()
            return retrofit.create(Endpoints::class.java)
        }
    }

    init {
        val database: CurrencyDatabase = CurrencyDatabase.getCurrencyDataBase(
            context
        )!!
        currencyDao = database.currencyDao()
    }

    interface Endpoints {
        @GET(LIST)
        fun getCurrencyMap(@Query("access_key") access_key: String): Observable<ListResponse>

        @GET(LIVE)
        fun getQuotes(@Query("access_key") access_key: String): Observable<LiveResponse>
    }

    fun getLocalQuotes(): LiveData<List<CurrencyQuote>> {
        return currencyDao.getQuotesLiveData()
    }

    fun getRemoteQuotes(): MutableLiveData<Pair<ListResponse?, LiveResponse?>> {
        val liveResponseLiveData: MutableLiveData<Pair<ListResponse?, LiveResponse?>> = MutableLiveData()
        var liveResponseLive: LiveResponse? = null
        var listResponseLive: ListResponse? = null
        disposable = listService.getCurrencyMap(KEY).zipWith(listService.getQuotes(KEY),
            { listResponse: ListResponse, liveResponse: LiveResponse ->
                if (listResponse.success && liveResponse.success && liveResponse.source == "USD"
                    && isConsistent(listResponse.currencies, liveResponse.quotes)
                ) {
                    listResponseLive = listResponse
                    liveResponseLive = liveResponse
                    currencyDao.insertCurrencyQuotes(
                        listResponse.currencies,
                        liveResponse.timestamp,
                        liveResponse.quotes
                    )
                }
            }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { result ->
                   liveResponseLiveData.postValue(Pair(listResponseLive, liveResponseLive))
                },
                { error ->
                    Log.e("", "connection problem ", error)
                    liveResponseLiveData.postValue(null)
                }
            )
        return liveResponseLiveData
    }

    fun dispose() {
        disposable?.dispose()
    }

    private fun isConsistent(currencies: Map<String, String>, quotes: Map<String, Float>): Boolean {
        if (currencies.size != quotes.size) {
            return false
        }
        val handlesCurr: List<String> = currencies.keys.map { it }
        val handlesQuote: List<String> = quotes.keys.map { it.substring(3) }
        if (handlesQuote != handlesCurr) {
            return false
        }
        return true
    }
}