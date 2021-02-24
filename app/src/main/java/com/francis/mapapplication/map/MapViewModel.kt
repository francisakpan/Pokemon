package com.francis.mapapplication.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.francis.mapapplication.model.Partner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapViewModel : ViewModel() {

    /**
     * Get a reference to the database with path contacts.
     */
    private val dbLocation = FirebaseDatabase.getInstance().getReference("users")

    //mutable live data to access partner's location
    private var _location = MutableLiveData<Partner>()

    //immutable live data to access partner's location
    val location: LiveData<Partner>
        get() = _location

    //mutable live data to access update result
    private var _result = MutableLiveData<Boolean>()

    //immutable live data to access update result
    val result: LiveData<Boolean>
        get() = _result

    //Initialize viewmodel and make network call to listen to partners location change.
    init {
        getPartnerLocations()
    }

    /**
     * Load current partner's location saved on firebase database.
     */
    private fun getPartnerLocations() {
        dbLocation.child("jesse").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val partnerLocation = snapshot.getValue(Partner::class.java)
                    partnerLocation?.id = snapshot.key
                    _location.value = partnerLocation
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LOCATION_DB", error.message)
            }
        })
    }

    /**
     * @param lat latitude
     * @param long longitude
     * Send current location to firebase.
     */
    fun updateMyLocation(lat: Double, long: Double) {
        val ref = dbLocation.child("francis")

        val data = mutableMapOf<String, Any>()
        data["latitude"] = lat
        data["longitude"] = long

        ref.setValue(data).addOnCompleteListener {
            _result.value = it.isSuccessful
        }
    }
}