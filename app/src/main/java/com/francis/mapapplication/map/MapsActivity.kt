package com.francis.mapapplication.map

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.francis.mapapplication.R
import com.francis.mapapplication.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

/**
 * Maps activity. Display a map on the screen with a marker indication my partners location.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //  Declare google map instance.
    private lateinit var map: GoogleMap

    //Declare map activity binding instance.
    private lateinit var binding: ActivityMapsBinding

    //Declare and instantiate view model instance.
    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    //Create Activity constants
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
        private const val REQUEST_CHECK_SETTINGS = 102
    }

    //Declare location callback instance.
    private lateinit var locationCallback: LocationCallback

    //Declare location request instance.
    private lateinit var locationRequest: LocationRequest

    //Set Location update state to check if location updates is registered.
    private var locationUpdateState = false

    //Declare fusedLocationClient instance
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Declare last location.
    private lateinit var lastLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Instantiate binding.
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Instantiate fuseLocationClient which handles location updates
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        //Use location callback to get my device updated location based on set interval.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation

                //Send new location update to firebase.
                viewModel.updateMyLocation(lastLocation.latitude, lastLocation.longitude)
            }
        }

        //Create a location updates request
        createLocationRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //unregister location update request on activity pause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        //if location update state not set, start new location update request.
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    /**
     * Create location update request to track changes in location in realtime.
     */
    private fun createLocationRequest() {
        //Instantiate location request
        locationRequest = LocationRequest()

        //Set location request interval
        locationRequest.interval = 1000 * 5
        locationRequest.fastestInterval = 1000

        //Set priority.
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Create a location setting builder to ask user to turn on location setting on phone
        //for real time location updates
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->

            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        //Use google api fusedLocationClient to register location update request.
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
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
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        map.setMapStyle(style)
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setUpMap()
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
        markerOptions.title("Latitude: ${location.latitude}, Longitude: ${location.longitude}")

        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_marker_round)
            )
        )

        val marker = map.addMarker(markerOptions)
        marker.showInfoWindow()
    }

    /**
     * Setup and overlay map on the screen.
     */
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        //Set location enabled.
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
            }
        }
    }
}