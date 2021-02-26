package com.francis.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.francis.mapapplication.databinding.PokemonDetailsItemBinding


/**
 * Pokemon details adapter class for displaying pokemon string data.
 */
class PokemonDetailsAdapter: ListAdapter<String, PokemonDetailsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: PokemonDetailsItemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(item: String){
            binding.textItem.text = item //set item to text view.
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    //Inflate view item on recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PokemonDetailsItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    //Bind view item on recycler view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) // get item at a position
        holder.bind(item)
    }
}