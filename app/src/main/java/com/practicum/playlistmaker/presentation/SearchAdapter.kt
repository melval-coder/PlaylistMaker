package com.practicum.playlistmaker.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track

// Адаптер для отображения списка треков
class SearchAdapter(private val data: List<Track>, private val clickListener: (Track) -> Unit) :
    RecyclerView.Adapter<SearchViewHolder>() {

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_items, parent, false)
        return SearchViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            clickListener(data[position])
        }
    }

    // Возврат количества элементов в списке
    override fun getItemCount(): Int {
        return data.size
    }
}