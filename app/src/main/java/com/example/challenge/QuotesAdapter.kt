package com.example.challenge

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.challenge.databinding.QuoteItemBinding

class QuotesAdapter(
    private val currencies: List<CurrencyQuote>,
    private val model: CurrencyViewModel,
    private val spanCount: Int,
    val context: Context
) : RecyclerView.Adapter<QuotesAdapter.ViewHolder>() {

    class ViewHolder(val binding: QuoteItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = QuoteItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.quote = currencies.get(position)
        holder.binding.itemContainer.setBackgroundColor(
            if (position / spanCount % 2 != 0) {
                Color.LTGRAY
            } else {
                Color.WHITE
            }
        )
        holder.binding.model = model
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return currencies.size
    }
}