package com.crack.gastoslistener.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crack.gastoslistener.db.SourceTotal
import java.util.Locale

class TotalesAdapter : RecyclerView.Adapter<TotalesAdapter.VH>() {

    private var items: List<SourceTotal> = emptyList()

    fun submitList(newItems: List<SourceTotal>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val euros = item.total / 100.0
        holder.text.text = String.format(Locale("es", "ES"), "%s: %.2f €", item.source, euros)
    }

    override fun getItemCount() = items.size
}
