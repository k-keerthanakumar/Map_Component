package com.equadriga.mapcomponent

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.equadriga.mapcomponent.Utils.GPSTracker
import com.equadriga.mapcomponent.Utils.Utilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var locationManager: LocationManager? = null
    var gpsTracker: GPSTracker? = null
    val PERMISSION_REQUEST_LOCATION = 1
    var checkLocationPermission = 0

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }

     @SuppressLint("MissingPermission", "SetTextI18n")
     private fun getLocation() {
    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
        val location: Location? = task.result
        if (location != null) {
            Utilities.dismissProgress(this)
            val geocoder = Geocoder(this, Locale.getDefault())
            val list: List<Address> =
                geocoder.getFromLocation(location.latitude, location.longitude, 1) as List<Address>
            tv_latitude.text = "Latitude\n${list[0].latitude}"
            tv_longitude.text = "Longitude\n${list[0].longitude}"
            tv_countryName.text = "Country Name\n${list[0].countryName}"
            tv_Locality.text = "Locality\n${list[0].locality}"
            tv_address.text = "Address\n${list[0].getAddressLine(0)}"
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
                    getLocation()
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