package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val data: List<Track>, private val clickListener: (Track) -> Unit) :
    RecyclerView.Adapter<SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_items, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            clickListener(data[position])
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
