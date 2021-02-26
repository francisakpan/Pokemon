package com.francis.mapapplication.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.ActivityDetailsBinding
import com.francis.mapapplication.model.*
import com.francis.mapapplication.adapter.PokemonDetailsAdapter
import com.francis.mapapplication.adapter.PokemonImagesAdapter
import com.francis.mapapplication.network.PokemonApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    companion object {
        private val TAG = DetailsActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Declare and initialize tools bar
        val toolbar: Toolbar = binding.toolsBar
        setSupportActionBar(toolbar) //set tools bar as default support action bar.

        binding.toolsLayout.title = "Loading..."
        binding.weightChip.text = String.format(getString(R.string.weight_text), "")
        binding.heightChip.text = String.format(getString(R.string.height_text), "")

        val id = intent.getStringExtra("id")!!

        registerNetworkListener(id)

        getPokemonData(id)
    }

    /**
     * On destroy unregister network callback on connectivity manager.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }

    /**
     * @param id Pokemon id
     * Register network listener to listen to changes in network states.
     */
    private fun registerNetworkListener(id: String) {
        // get ConnectivityManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                CoroutineScope(Dispatchers.Main).launch {
                    getPokemonData(id)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val builder: NetworkRequest.Builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(
                builder.build(),
                networkCallback
            )
        }
    }

    /**
     * Loads pokemon data to the screen using rxjava3
     */
    private fun getPokemonData(id: String) {
        PokemonApi
            .retrofitService
            .getPokemon(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PokemonData> {
                override fun onSubscribe(d: Disposable?) {
                    Log.i(TAG, "OnSubscribe")
                }

                override fun onNext(pokemonData: PokemonData?) {
                    Log.i(TAG, "onNext")
                    pokemonData?.let { populateDetailsView(it) }
                }

                override fun onError(e: Throwable?) {
                    Log.i(TAG, "onError")
                    e?.printStackTrace()
                    binding.toolsLayout.title = "Failed to load"
                    Toast.makeText(this@DetailsActivity, "An error occurred", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onComplete() {
                    Log.i(TAG, "onComplete")
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateDetailsView(pokemonData: PokemonData) {
        binding.toolsLayout.title = pokemonData.name
        loadPokemonAvatar(pokemonData.id)
        setHWChips(pokemonData.weight, pokemonData.height)
        loadExtraImages(pokemonData.sprites)
        displayPokemonStats(pokemonData.stats)
        displayPokemonAbilities(pokemonData.abilities)
        displayPokemonMoves(pokemonData.moves)
        displayPokemonWeakness(pokemonData.types)
    }

    /**
     * @param id pokemon id
     * Loads the official artwork image using glide
     */
    private fun loadPokemonAvatar(id: Int) {
        val urlString =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

        //Use glide to download and cache image from the internet.
        Glide.with(binding.pokemonImage.context)
            .load(urlString)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_connection_error)
            )
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.pokemonImage)
    }

    /**
     * @param weight pokemon weight.
     * @param height pokemon height.
     * Sets pokemon's height and weight to the height and weight text fields.
     */
    private fun setHWChips(weight: Int, height: Int) {
        binding.weightChip.text = String.format("Weight: %d", weight)
        binding.heightChip.text = String.format("Height: %d", height)
    }

    /**
     * @param data sprites data containing image links concerning a pokemon character.
     */
    private fun loadExtraImages(data: Sprites) {
        val adapter = PokemonImagesAdapter()
        binding.pokemonImagesRecyclerview.adapter = adapter

        //Set layout manager to pokemon image recyclerview.
        binding.pokemonImagesRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //Initialize and instantiate link array list.
        val links = arrayListOf<String>()

        //Add sprites data to link list.
        //Some sprites data contains null. Convert to empty string as links is a list of Strings.
        links.add(data.component1() ?: "")
        links.add(data.component2() ?: "")
        links.add(data.component3() ?: "")
        links.add(data.component4() ?: "")
        links.add(data.component5() ?: "")
        links.add(data.component6() ?: "")
        links.add(data.component7() ?: "")

        //Submit links strings to adapter filter out empty strings entries.
        adapter.submitList(links.filter { it.isNotEmpty() })
    }

    /**
     * @param data All pokemon stats.
     * Display the stats on stats recyclerview
     */
    private fun displayPokemonStats(data: List<Stats>) {
        val adapter = PokemonDetailsAdapter()
        binding.recyclerViewStats.adapter = adapter
        //Initialize pokemon recycler view for all pokemon stats
        binding.recyclerViewStats.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        val list = arrayListOf<String>()
        data.forEach { list.add("${it.stat.name}: ${it.base_stat}") }
        adapter.submitList(list)
    }

    /**
     * @param data All pokemon's abilities.
     * Display pokemon's abilities on the abilities recyclerview.
     */
    private fun displayPokemonAbilities(data: List<Abilities>) {
        val adapter = PokemonDetailsAdapter()
        binding.recyclerViewAbilities.adapter = adapter
        //Initialize pokemon recycler view for all pokemon abilities
        binding.recyclerViewAbilities.layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        val list = arrayListOf<String>()
        data.forEach { list.add(it.ability.name) }
        adapter.submitList(list)
    }

    /**
     * @param data All pokemon's moves.
     * Display pokemon's moves on the moves recyclerview.
     */
    private fun displayPokemonMoves(data: List<Moves>) {
        val adapter = PokemonDetailsAdapter()
        binding.recyclerviewMoves.adapter = adapter
        //Initialize pokemon recycler view for all pokemon moves
        binding.recyclerviewMoves.layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        val list = arrayListOf<String>()
        data.forEach { list.add(it.move.name) }
        adapter.submitList(list)
    }

    /**
     * @param data All pokemon's types.
     * Display pokemon's types on the types recyclerview.
     */
    private fun displayPokemonWeakness(data: List<Types>) {
        val adapter = PokemonDetailsAdapter()
        binding.recyclerviewTypes.adapter = adapter
        //Initialize pokemon recycler view for all pokemon weaknesses
        binding.recyclerviewTypes.layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        val list = arrayListOf<String>()
        data.forEach { list.add(it.type.name) }
        adapter.submitList(list)
    }

}