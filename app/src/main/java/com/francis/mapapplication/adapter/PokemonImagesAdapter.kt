package com.francis.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.PokemonImagesItemBinding

/**
 * Pokemon images adapter class for displaying pokemon additional images.
 */
class PokemonImagesAdapter :
    ListAdapter<String, PokemonImagesAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: PokemonImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: String) {

            val imgUri = imgUrl //Image Uri to load image.
                .toUri()
                .buildUpon()
                .scheme("https")
                .build()

            Glide.with(binding.pokemonImage.context)  //Use glide to download and cache image from the internet.
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.pokemonImage)

        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    //Inflate view item on recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PokemonImagesItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    //Bind view item on recycler view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = getItem(position)
        holder.bind(imageUrl)
    }

}
