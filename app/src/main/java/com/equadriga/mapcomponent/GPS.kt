package com.equadriga.mapcomponent

import android.Manifest
import android.R
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import com.equadriga.mapcomponent.Utils.GPSTracker
import com.equadriga.mapcomponent.Utils.Utilities

class GPS: AppCompatActivity() {

    var locationManager: LocationManager? = null
    var gpsTracker: GPSTracker? = null
    val PERMISSION_REQUEST_LOCATION = 1
    var checkLocationPermission = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
                checkGPS()
            } else {
                notifyLocationPermission()
            }
        }
    }

    //To check gps is available or not
    private fun checkGPS() {
        val pm = this.packageManager
        // To check the GPS Feature available or not
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            locationManager =
                this.getSystemService(LOCATION_SERVICE) as LocationManager

            // getting GPS status
            val isGPSEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsTracker = GPSTracker(this)

            // check if GPS enabled
            if (isGPSEnabled || isNetworkEnabled) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable { // Your Code
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

                /*alertDialog.setNegativeButton(getResources().getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });*/
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


    }
