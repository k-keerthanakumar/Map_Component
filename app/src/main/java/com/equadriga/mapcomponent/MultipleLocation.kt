package com.equadriga.mapcomponent

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData.Item
import android.content.Intent
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
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.equadriga.mapcomponent.Utils.GPSTracker
import com.equadriga.mapcomponent.Utils.Utilities
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_multiple_location.*
import java.io.IOException


class MultipleLocation : FragmentActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mMap: GoogleMap? = null
    private lateinit var mLastLocation: Location
    private var mCurrLocationMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mLocationRequest: LocationRequest
    var locationVal: Location? = null
    var gpsTracker: GPSTracker? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var checkLocationPermission = 0
    val PERMISSION_REQUEST_LOCATION = 1
    var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        search_loc.setOnClickListener {
            searchLocationMap()
        }
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
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
            mMap!!.moveCamera(cameraUpdate)
            mMap!!.clear()
        }

    }

    @Synchronized
    private fun buildGoogleApiClient() {
        Utilities.dismissProgress(this)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap!!.addMarker(markerOptions)

        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(16.0f))

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.getFusedLocationProviderClient(this)
        }

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
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
                    Utilities.showProgress(this)
                    buildGoogleApiClient()
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

    fun searchLocationMap() {
        val locationSearch: EditText = findViewById<EditText>(R.id.editText)
        val location_enter: String = locationSearch.text.toString()
        var addressList: List<Address>? = null
        if (location_enter == "") {
            Toast.makeText(applicationContext,"provide location", Toast.LENGTH_SHORT).show()
        }
        else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location_enter, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val address = addressList!![0]
             val latLng = LatLng(address.latitude, address.longitude)
            mMap!!.addMarker(MarkerOptions().position(latLng).title(location_enter))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            edit_latitude.text = "Latitude\n" + address.latitude.toString()
            edit_longitude.text = "Longitude\n" +address.longitude.toString()

//            Toast.makeText(applicationContext, address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()
        }
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}