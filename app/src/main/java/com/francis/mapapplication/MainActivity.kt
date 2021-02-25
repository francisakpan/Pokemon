package com.francis.mapapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.francis.mapapplication.databinding.ActivityMainBinding
import com.francis.mapapplication.map.MapsActivity
import com.francis.mapapplication.pokemon.PokemonActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Application Entry point
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //Create Activity constants
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Binds imp1 button to map activity on click
        binding.imp1Button.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
                return@setOnClickListener
            }
            startActivity(Intent(this, MapsActivity::class.java))
        }

        // Binds imp2 button to pokemon activity on click
        binding.imp2Button.setOnClickListener {
            startActivity(Intent(this, PokemonActivity::class.java))
        }
    }

    /**
     * Request location permission.
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Confirm location request permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, MapsActivity::class.java))
            } else {
                Toast.makeText(this, "Permission is required to open map.", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}