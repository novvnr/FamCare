package com.app.famcare.view.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class DaycareMapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DaycareAdapter
    private lateinit var tvUserLocation: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "DaycareMapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        tvUserLocation = findViewById(R.id.tv_UserLocation)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        adapter = DaycareAdapter(emptyList()) { websiteURL ->
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("WEBSITE_URL", websiteURL)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(TAG, "User location: Lat=${location.latitude}, Lng=${location.longitude}")
                    getUserAddress(location)
                    loadDaycareData(location)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        val locationLayout = findViewById<CardView>(R.id.locationLayout)
        locationLayout.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(
                            TAG,
                            "User location: Lat=${location.latitude}, Lng=${location.longitude}"
                        )
                        showLocationPopup(location)
                    } else {
                        showLocationPopup(null)
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error obtaining location", exception)
                    showLocationPopup(null)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showLocationPopup(location: Location?) {
        val message = if (location != null) {
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val addresses: List<Address>? =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.get(0)?.getAddressLine(0) ?: "Unknown location"
            } catch (e: IOException) {
                Log.e(TAG, "Geocoder failed", e)
                "Unknown location"
            }
        } else {
            "Location not available"
        }

        AlertDialog.Builder(this).setTitle("Your Location").setMessage(message)
            .setPositiveButton("OK", null).show()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d(TAG, "User location: Lat=${location.latitude}, Lng=${location.longitude}")
                getUserAddress(location)
                loadDaycareData(location)
            } else {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error obtaining location", exception)
            tvUserLocation.text = "Location not available"
        }
    }

    private fun getUserAddress(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.get(0)?.getAddressLine(0) ?: "Unknown location"
            if (address == "Unnamed Road") {
                Log.d(TAG, "Geocoder returned 'Unnamed Road'")
            }
            tvUserLocation.text = address
        } catch (e: IOException) {
            Log.e(TAG, "Geocoder failed", e)
            tvUserLocation.text = "Unknown location"
        }
    }

    private fun loadDaycareData(userLocation: Location) {
        progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        val daycareCollection = firestore.collection("Daycare")

        daycareCollection.get().addOnSuccessListener { result ->
            val daycares = mutableListOf<Daycare>()
            for (document in result) {
                val geoPoint = document.getGeoPoint("Coordinat")
                val locationName = document.getString("Location")
                val daycareName = document.getString("Name")
                val photoURL = document.getString("PhotoURL")
                val websiteURL = document.getString("WebsiteURL")

                if (geoPoint != null && locationName != null && daycareName != null && photoURL != null && websiteURL != null) {
                    val daycareLocation = Location("").apply {
                        latitude = geoPoint.latitude
                        longitude = geoPoint.longitude
                    }
                    val distanceInMeters = userLocation.distanceTo(daycareLocation)
                    val distanceInKilometers = distanceInMeters / 1000
                    val df = DecimalFormat("#.#").apply { roundingMode = RoundingMode.HALF_UP }
                    val roundedDistanceInKilometers = df.format(distanceInKilometers).toDouble()

                    daycares.add(
                        Daycare(
                            daycareName,
                            locationName,
                            photoURL,
                            geoPoint,
                            roundedDistanceInKilometers,
                            websiteURL
                        )
                    )
                    Log.d(
                        TAG,
                        "Daycare: $daycareName, Lat=${geoPoint.latitude}, Lng=${geoPoint.longitude}, Distance: $roundedDistanceInKilometers km"
                    )
                } else {
                    Log.d(TAG, "Document is missing required fields")
                }
            }

            progressBar.visibility = View.GONE
            daycares.sortBy { it.distanceFromUser }
            adapter.updateDaycares(daycares)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "get failed with ", exception)
            progressBar.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                tvUserLocation.text = "Location not enabled"
                loadDaycareDataWithoutUserLocation()
            }
        }
    }

    private fun loadDaycareDataWithoutUserLocation() {
        progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        val daycareCollection = firestore.collection("Daycare")

        daycareCollection.get().addOnSuccessListener { result ->
            val daycares = mutableListOf<Daycare>()
            for (document in result) {
                val geoPoint = document.getGeoPoint("Coordinat")
                val locationName = document.getString("Location")
                val daycareName = document.getString("Name")
                val photoURL = document.getString("PhotoURL")
                val websiteURL = document.getString("WebsiteURL")

                if (geoPoint != null && locationName != null && daycareName != null && photoURL != null && websiteURL != null) {
                    daycares.add(
                        Daycare(
                            daycareName, locationName, photoURL, geoPoint, 0.0, websiteURL
                        )
                    )
                } else {
                    Log.d(TAG, "Document is missing required fields")
                }
            }
            adapter.updateDaycares(daycares)
            progressBar.visibility = View.GONE
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
            progressBar.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}