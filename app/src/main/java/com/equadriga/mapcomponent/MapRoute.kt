package com.equadriga.mapcomponent

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.equadriga.mapcomponent.Utils.GPSTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_map_route.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MapRoute : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var latitude: Double? = null
    var longitude: Double? = null
    var locationVal: Location? = null
    var gpsTracker: GPSTracker? = null
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_route)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Map Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val gd = findViewById<Button>(R.id.search_loc)

      cancel.setOnClickListener {
              marker.remove()
              mMap.clear()
          search_place.text.clear()
          dest_place.text.clear()
      }

        gd.setOnClickListener{
            val startSearch: EditText = findViewById<EditText>(R.id.search_place)
            val destSearch: EditText = findViewById<EditText>(R.id.dest_place)
            val start_search: String = startSearch.text.toString()
            val dest_search: String = destSearch.text.toString()

            var startList: List<Address>? = null
            var destList: List<Address>? = null
            if (start_search == "" || dest_search == "") {
                Toast.makeText(applicationContext,"provide location", Toast.LENGTH_SHORT).show()
            } else{
                val geoCoder = Geocoder(this)
                try {
                    startList = geoCoder.getFromLocationName(start_search, 1)
                    destList = geoCoder.getFromLocationName(dest_search, 1)
                    cancel.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val start = startList!![0]
                val latLngstart = LatLng(start.latitude, start.longitude)
                val markerOptions = MarkerOptions().position(latLngstart)
                marker = mMap.addMarker(markerOptions)
                mMap!!.addMarker(MarkerOptions().position(latLngstart).title(start_search))
                val dest = destList!![0]
                val latLngdest = LatLng(dest.latitude, dest.longitude)
                mMap!!.addMarker(MarkerOptions().position(latLngdest).title(dest_search))
                mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLngdest))
                val urll = getDirectionURL(latLngstart, latLngdest, apiKey)
                GetDirection(urll).execute()

//            Toast.makeText(applicationContext, address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
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
            mMap.addMarker(MarkerOptions().position(mapCoordinate))
            mMap!!.isMyLocationEnabled = true
            mMap!!.moveCamera(cameraUpdate)
            mMap!!.clear()
        }
    }

    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        // Building the parameters to the web service
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        //setting transportation mode
        val mode = "mode=driving"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=$secret"

//        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
//                "&destination=${dest.latitude},${dest.longitude}" +
//                "&sensor=false" +
//                "&mode=driving" +
//                "&key=$secret"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
