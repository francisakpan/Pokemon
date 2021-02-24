package com.francis.mapapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.francis.mapapplication.databinding.ActivityMainBinding
import com.francis.mapapplication.map.MapsActivity
import com.francis.mapapplication.pokemon.PokemonActivity

/**
 * Application Entry point
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Binds imp1 button to map activity on click
        binding.imp1Button.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        // Binds imp2 button to pokemon activity on click
        binding.imp2Button.setOnClickListener {
            startActivity(Intent(this, PokemonActivity::class.java))
        }
    }
}