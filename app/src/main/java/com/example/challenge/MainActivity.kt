package com.example.challenge

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.challenge.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    var currencies: List<CurrencyQuote> = listOf()
    var updateJob: Job? = null

    companion object{
        private const val SPAN_COUNT_PORTRAIT = 2
        private const val SPAN_COUNT_LANDSCAPE = 3
    }

    private lateinit var recyclerView: RecyclerView
    private val model: CurrencyViewModel by viewModels()
    private lateinit var adapter: QuotesAdapter
    private lateinit var binding: ActivityMainBinding
    private val pickerList: MutableList<String>? = mutableListOf()
    private var spanCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.model = this.model

        spanCount = if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            SPAN_COUNT_LANDSCAPE
        } else {
            SPAN_COUNT_PORTRAIT
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)

        recyclerView = binding.recyclerView
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        )
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        binding.dropButton.setOnClickListener {
            val dialog = CurrencyPickerDialog(
                if (model.selectedSourceCurrency == null) {
                    0
                } else {
                    model.selectedSourceCurrency?.value!!.id - 1
                }, pickerList!!
            )
            dialog.show(supportFragmentManager, null)

            dialog.setValueChangeListener { _, index, _ ->
                model.updateSelectedSourceCurrency(currencies[index])
            }
        }

        binding.editTextNumberDecimal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.toString() != ".") {
                    model.currentAmount = MutableLiveData(s.toString().toFloat())
                } else {
                    model.currentAmount = MutableLiveData(0f)
                }
                updateRecyclerView(QuotesAdapter(currencies, model, spanCount, recyclerView.context))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.editTextNumberDecimal.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm: InputMethodManager =
                    this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

        model.getRemoteList().observe(this, { listResponse ->
            if (listResponse == null || !listResponse.success || listResponse.currencies.isEmpty()) {
                binding.progressBar.visibility = View.GONE
                showErrorToast()
            } else {
                model.getRemoteLive(listResponse.currencies).observe(this, { liveResponse ->
                    if (liveResponse == null || !liveResponse.success || liveResponse.quotes.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        showErrorToast()
                    }
                })
            }
        })

        updateJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                binding.progressBar.visibility = View.VISIBLE
                model.getRemoteList()
                TimeUnit.MINUTES.sleep(30)
            }
        }

        model.selectedSourceCurrency?.observe(this, Observer {
            updateRecyclerView(QuotesAdapter(currencies, model, spanCount, recyclerView.context))
        })

        model.getLocalQuotesLiveData().observe(this, { newCurrencies ->
            if (currencies != newCurrencies) {
                binding.progressBar.visibility = View.GONE
                binding.equals.visibility = View.VISIBLE
                currencies = newCurrencies
                updatePicker(currencies)
                if (currencies.isNotEmpty()) {
                    model.updateDateTime(currencies[0])
                    if (model.selectedSourceCurrency?.value?.fullName == getString(R.string.loading)) {
                        model.updateSelectedSourceCurrency(currencies[0])
                        return@observe
                    }
                }
                updateRecyclerView(QuotesAdapter(currencies, model, spanCount,this))
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        model.dispose()
    }

    private fun updateRecyclerView(adapter : QuotesAdapter){
        val index = (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        (recyclerView.layoutManager as GridLayoutManager).scrollToPosition(index)
    }

    private fun updatePicker(currencies: List<CurrencyQuote>) {
        for (currency in currencies) {
            if (!pickerList!!.contains(currency.targetHandle + ": " + currency.fullName)) {
                pickerList.add(currency.targetHandle + ": " + currency.fullName)
            }
        }
    }

    private fun showErrorToast() {
        Toast.makeText(this, getString(R.string.connectionToast), Toast.LENGTH_LONG).show()
        binding.fullNameText.text=""
    }
}