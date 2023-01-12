package com.equadriga.mapcomponent

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.equadriga.mapcomponent.Utils.GPSTracker
import com.equadriga.mapcomponent.Utils.Utilities
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.activity_map_location.*
import java.util.*

class MapLocation : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private  lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    var currentLocation: LatLng = LatLng(0.0, 0.0)
    var locationManager: LocationManager? = null
    var locationVal: Location? = null
    var gpsTracker: GPSTracker? = null
    var latitude: Double? = null
    var longitude: Double? = null
    val PERMISSION_REQUEST_LOCATION = 1
    var checkLocationPermission = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)
        supportActionBar!!.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Initializing Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initializing fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        gpsTracker = GPSTracker(this)
        locationVal = gpsTracker!!.getLocation()
        if (locationVal != null) {
            latitude = locationVal!!.latitude
            longitude = locationVal!!.longitude
            val mapCoordinate = LatLng(latitude!!, longitude!!)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapCoordinate, 16.0f)
            mMap.setOnMapClickListener { location ->
                mMap.clear()
                marker.remove()
                placeMarkerOnMap(location)
            }
            mMap.isMyLocationEnabled = false
            mMap.moveCamera(cameraUpdate)
            mMap.clear()
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        marker = mMap.addMarker(markerOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,16.0F))
        val geocoder = Geocoder(this, Locale.getDefault())
        val list: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1) as List<Address>
        latitude_map.text = "Latitude\n${list[0].latitude}"
        longitude_map.text = "Longitude\n${list[0].longitude}"
    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Utilities.dismissProgress(this)
                        placeMarkerOnMap(LatLng(location.latitude, location.longitude))
                    }
                }
            }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
                Utilities.showProgress(this)
                checkGPS()
            } else {
                Utilities.showProgress(this)
                notifyLocationPermission()
            }
        }
    }

    //To check gps is available or not
    private fun checkGPS() {
        val pm = this.packageManager
        // To check the GPS Feature available or not
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            // getting GPS status
            val isGPSEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsTracker = GPSTracker(this)

            // check if GPS enabled
            if (isGPSEnabled || isNetworkEnabled) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    Utilities.showProgress(this)// Your Code
                    getLastLocation()
                }, 3000)
            } else {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("GPS")
                alertDialog.setMessage("To enable location-based comparison function, please activate GPS on device used")
                alertDialog.setCancelable(false)
                alertDialog.setPositiveButton("Settings")
                { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    dialog.dismiss()
                }

                val gpsDialog = alertDialog.create()
                gpsDialog.show()
            }
        } else {
            Toast.makeText(this, "No GPS function available", Toast.LENGTH_LONG).show()
        }
    }

    private fun notifyLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission !== 1) {
                checkLocationPermission = 1
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
            }
        }
    }
    // Get current location, if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        Utilities.dismissProgress(this)
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (Utilities.isConnectivity(this)) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                checkLocationPermission()
            } else {
                checkGPS()
            }
        } else {
            Toast.makeText(this, "check connection", Toast.LENGTH_SHORT).show()
        }
    }

}