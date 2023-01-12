package com.equadriga.mapcomponent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar!!.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        btn_location.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        btn_map.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_map_location.setOnClickListener {
            val intent = Intent(this, MultipleLocation::class.java)
            startActivity(intent)
            finish()
        }
        btn_map_change.setOnClickListener {
            val intent = Intent(this, MapLocation::class.java)
            startActivity(intent)
            finish()
        }
        marker.setOnClickListener {
            val intent = Intent(this, PermanentMarker::class.java)
            startActivity(intent)
            finish()
        }
        map_route.setOnClickListener {
            val intent = Intent(this, MapRoute::class.java)
            startActivity(intent)
            finish()
        }
        sample.setOnClickListener {
            val intent = Intent(this, SamplePoly::class.java)
            startActivity(intent)
            finish()
        }
    }
}