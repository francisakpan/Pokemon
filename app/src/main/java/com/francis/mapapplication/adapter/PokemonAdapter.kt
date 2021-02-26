package com.francis.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.GridViewItemBinding
import com.francis.mapapplication.model.Pokemon

/**
 * @param clickListener listener object to observe item click on recycler view.
 * Adapter class for displaying pokemon data on recycler view.
 */
class PokemonAdapter(private val clickListener: ItemOnClickListener) :
    RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    // Declare and initialize an array list of pokemon.
    private var pokemons: ArrayList<Pokemon> = arrayListOf()

    //Declare recycler view item click listener
    interface ItemOnClickListener {
        fun onPokemonClicked(id: String)
    }

    /**
     * @param binding layout to inflate
     * View holder class to hold individual items on the recycler view.
     */
    inner class ViewHolder(private val binding: GridViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var id: String

        //Initialize view holder and set onclick listener.
        init {
            binding.container.setOnClickListener { clickListener.onPokemonClicked(id) }
        }

        /**
         * @param pokemon pokemon data.
         * binds pokemon item to recycler view
         */
        fun bind(pokemon: Pokemon) {
            //Get id from pokemon url
            val splitUrl = pokemon.url.split("/")
            id = splitUrl[splitUrl.size - 2]

            val urlString =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

            //Using glide to display and cache network image
            Glide.with(binding.pokemonImage.context)
                .asBitmap()
                .load(urlString)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .override(150, 150)
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.pokemonImage)

            //Set pokemon name to text view.
            binding.pokemonName.text = pokemon.name
        }
    }

    //Inflate view item on recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    //Bind view item on recycler view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pokemons[position])
    }

    //Get recycler view item count.
    override fun getItemCount(): Int = pokemons.size

    //Populate recycler view with new items from network call.
    fun populateList(items: List<Pokemon>) {
        pokemons.addAll(items)
        notifyDataSetChanged()
    }

    //Rest the recycler view
    fun clearItems() {
        pokemons.clear()
        notifyDataSetChanged()
    }
}