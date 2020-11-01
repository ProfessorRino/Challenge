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
        const val KEY = "YOUR_ACCESS_KEY_GOES_HERE"
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

    fun getRemoteList(): MutableLiveData<ListResponse> {
        val listResponse: MutableLiveData<ListResponse> = MutableLiveData()
        disposable =
            listService.getCurrencyMap(KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    { result -> listResponse.postValue(result) },
                    { error ->
                        Log.e("", "endpoint list failed ", error)
                        listResponse.postValue(null)
                    }
                )
        return listResponse
    }

    fun getRemoteLive(currencies: Map<String, String>): MutableLiveData<LiveResponse> {
        val liveResponse: MutableLiveData<LiveResponse> = MutableLiveData()
        disposable = listService.getQuotes(KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { result ->
                    if (result.success && result.source == "USD" && isConsistent(
                            currencies,
                            result.quotes
                        )
                    ) {
                        currencyDao.upsertCurrencyQuotes(
                            currencies,
                            result.timestamp,
                            result.quotes
                        )
                        liveResponse.postValue(result)
                    } else {
                        liveResponse.postValue(null)
                    }
                },
                { error ->
                    Log.e("", "endpoint live failed ", error)
                    liveResponse.postValue(null)
                }
            )
        return liveResponse
    }

    fun dispose() {
        disposable?.dispose()
    }

    fun isConsistent(currencies: Map<String, String>, quotes: Map<String, Float>): Boolean {
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