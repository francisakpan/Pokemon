package com.francis.mapapplication.pokemon

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.ActivityPokemonBinding
import com.francis.mapapplication.model.Property
import com.francis.mapapplication.pokemon.adapter.PokemonAdapter
import com.francis.mapapplication.pokemon.network.PokemonApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PokemonActivity : AppCompatActivity(), PokemonAdapter.ItemOnClickListener {

    companion object {
        private val TAG: String = PokemonActivity::class.java.simpleName
    }

    //set request offset to zero.
    private var offset = 0
    private var limit = 20

    //Declare and initialize adapter for recyclerView
    private val adapter = PokemonAdapter(this)

    //Declare activity pokemon binding
    private lateinit var binding: ActivityPokemonBinding

    //Declare connectivity manager.
    private lateinit var connectivityManager: ConnectivityManager

    //Declare connectivity network callback
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set onScroll listener to recycler view to query for more data when it has been scrolled
        // to last item on the list.
        binding.photosGrid.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.photosGrid.layoutManager!! as GridLayoutManager
                if (dy > 0) {
                    val visibleItemCount: Int = layoutManager.childCount
                    val totalItemCount: Int = layoutManager.itemCount
                    val pastVisibleItems: Int = layoutManager.findFirstVisibleItemPosition()
                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        Log.i(TAG, "Last item reached.")
                        offset += limit
                        getProperties(offset, limit)
                    }
                }
            }
        })

        //Enable network listener to listener for changes in network states.
        //Like turning on and off network.
        registerNetworkListener()

        //Set adapter to recycler view.
        binding.photosGrid.adapter = adapter

        //Display loading indicator before making network call
        showLoading()

        //Make network call to get pokemon properties.
        getProperties(offset, limit)
    }

    /**
     * Register network listener to listen to changes in network states.
     */
    private fun registerNetworkListener() {
        // get ConnectivityManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //Instantiate network callback
        networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (adapter.itemCount < 1) {
                    CoroutineScope(Dispatchers.Main).launch {
                        showLoading()
                        getProperties(offset, limit)
                    }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_limit -> {
                //Show Alert dialog o specify number of item to display.
                showDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * on destroy unregister network callback from connectivity manager.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }

    /**
     * @param offset page to start loading next data.
     * @param limit number of item to return on each network call.
     * Using RxJava to observer for pokemon data from the network
     */
    private fun getProperties(offset: Int, limit: Int) {
        PokemonApi
            .retrofitService
            .getProperties(offset, limit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Property> {
                override fun onSubscribe(d: Disposable?) {
                    Log.d(TAG, "onSubscribe")
                }

                override fun onNext(property: Property?) {
                    //Populate the adapter with new data as it is being received.
                    property?.results?.let { adapter.populateList(it) }
                }

                override fun onError(e: Throwable?) {
                    if (adapter.itemCount < 1) {
                        //show network error indicator if there is no data in the adapter.
                        showNetworkError()
                        Toast.makeText(this@PokemonActivity, "An error occurred", Toast.LENGTH_LONG)
                            .show()
                    }
                    e?.printStackTrace()
                }

                override fun onComplete() {
                    hideLoading() // on task complete hide loading indicator.
                }
            })
    }

    /**
     * Create an Alert dialog to let user set limits of information to display
     */
    private fun showDialog() {
        val dialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.menu_edit_text_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.menu_edit_text)
        dialog.setTitle("Set Limit")
        dialog.setView(dialogView)
        dialog.setPositiveButton("Ok") { it, _ ->
            this@PokemonActivity.offset = 0
            this@PokemonActivity.limit = editText.text.toString().toInt()
            adapter.clearItems()
            showLoading()
            getProperties(offset, limit)
            it.dismiss()
        }
        dialog.setNegativeButton("Cancel") { it, _ ->
            it.dismiss()
        }
        dialog.show()
    }

    /**
     * Show loading indicator.
     */
    private fun showLoading() {
        binding.connectionError.visibility = View.GONE
        binding.loadingImage.visibility = View.VISIBLE
        binding.photosGrid.visibility = View.INVISIBLE
    }

    /**
     * show network error inidcator
     */
    private fun showNetworkError() {
        binding.connectionError.visibility = View.VISIBLE
        binding.loadingImage.visibility = View.GONE
        binding.photosGrid.visibility = View.GONE
    }

    /**
     * hide loading indicator.
     */
    private fun hideLoading() {
        binding.photosGrid.visibility = View.VISIBLE
        binding.loadingImage.visibility = View.GONE
    }

    /**
     * @param id pokemon id of the clicked item
     * Navigate to details activity on item click.
     */
    override fun onPokemonClicked(id: String) {
        //Navigate to details view when an item in the recycler view is pressed.
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }
}