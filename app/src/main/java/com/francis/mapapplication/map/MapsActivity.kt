package com.francis.mapapplication.map

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

/**
 * Maps activity. Display a map on the screen with a marker indication my partners location.
 */
class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, LocationListener {

    //  Declare google map instance.
    private lateinit var map: GoogleMap

    //Declare map activity binding instance.
    private lateinit var binding: ActivityMapsBinding

    //Declare and instantiate view model instance.
    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    //Declare the location manager instance.
    private lateinit var locationManager: LocationManager

    //Create Activity constants
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
        private const val MIN_TIME = 1000L * 5
        private const val MIN_DIST = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Instantiate binding.
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //instantiate location manager.
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Use live data object in the view model to observe for partner's location change in firebase database.
        viewModel.location.observe(this) { partner ->
            if (partner != null) {
                val latLng = LatLng(partner.latitude!!, partner.longitude!!)
                placeMarkerOnMap(latLng)
            }
        }
    }

    /**
     * Request for location updates once map is available for use
     */
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true //enable location on map.

        //Check for enabled provider and set location request on the provider
        //Provider can be GPS provider or Network provider.
        when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME,
                    MIN_DIST,
                    this
                )
            }
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME,
                    MIN_DIST,
                    this
                )
            }
            else -> {
                Toast.makeText(this, "", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        requestLocationUpdates()
    }

    /**
     * Request location permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Permission is required",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction("Request") {
                    requestLocationUpdates()
                    snackbar.dismiss()
                }.show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    /**
     * @param location contains the latitude and longitude coordinates to place partner's marker on.
     * Places a marker on the map to indicate partner's location.
     */
    private fun placeMarkerOnMap(location: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
        map.clear()
        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.mipmap.ic_marker_round)))
        map.addMarker(markerOptions)
    }

    /**
     * @param location updated location coordinates
     * Location listener callback that returns my current location.
     */
    override fun onLocationChanged(location: Location) {
        //Get latitude and longitude from position.
        val currentLatLng = LatLng(location.latitude, location.longitude)

        //Send new coordinates to firebase.
        viewModel.updateMyLocation(currentLatLng.latitude, currentLatLng.longitude)
    }

    /**
     * On stop remove location updates listener.
     */
    override fun onStop() {
        super.onStop()
        locationManager.removeUpdates(this)
    }

}