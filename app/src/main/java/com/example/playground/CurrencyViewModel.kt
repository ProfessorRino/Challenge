package com.example.playground

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*

class CurrencyViewModel @ViewModelInject constructor(
    @ApplicationContext
    context: Context
) : ViewModel() {

    var selectedSourceCurrency: MutableLiveData<CurrencyQuote>? =
        MutableLiveData(CurrencyQuote(1, "", "LOADING...", 0, 0f))
    var currentAmount: MutableLiveData<Float> = MutableLiveData(0f)
    var dateTime: MutableLiveData<String> = MutableLiveData("")


    private val repository: CurrencyRepository = CurrencyRepository(context)

    fun getRemoteQuotes() : MutableLiveData<Pair<ListResponse?, LiveResponse?>> {
        return repository.getRemoteQuotes()
    }

    fun updateDateTime(currencyQuote: CurrencyQuote) {
            val format = SimpleDateFormat("yyyy/MM/dd', 'HH:mm:ss'h'", Locale.getDefault())
            val date = Date((currencyQuote.timeStamp) * 1000.toLong())
            dateTime.postValue(format.format(date))
    }

    fun updateSelectedSourceCurrency(currencyQuote: CurrencyQuote){
        selectedSourceCurrency?.postValue(currencyQuote)
    }

    fun getLocalQuotesLiveData(): LiveData<List<CurrencyQuote>> {
        return repository.getLocalQuotes()
    }

    fun dispose(){
        repository.dispose()
    }
}

