package com.example.velibus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.velibus.api.StationVelibService
import com.example.velibus.database.AppDatabase
import com.example.velibus.databinding.ActivityMapsBinding
import com.example.velibus.model.Station
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.UnknownHostException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    val listStation: MutableList<Station> = mutableListOf()
    val savedStation: ArrayList<Station> = ArrayList()

    private val Paris = LatLng(48.8578, 2.3461)
    private val ileDeFranceBounds = LatLngBounds(
        LatLng(48.7721, 2.1634),//Sud-Ouest
        LatLng(48.9384, 2.523713)//Nord-Est
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
        val stationDao = db.stationDao()

        findViewById<Button>(R.id.home_list_stations)
            .setOnClickListener {
                if (!stationDao.getAll().isEmpty()){
                val intent = Intent(this, SavedStationActivity::class.java)
                startActivity(intent)
                } else{
                   Toast.makeText(
                        this@MapsActivity,
                        "Il n'y a pas de Stations enregistrées",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Paris, 13f))
        mMap.setLatLngBoundsForCameraTarget(ileDeFranceBounds)
        mMap.setMinZoomPreference(10.75f)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
        val stationDao = db.stationDao()
        val saved = stationDao.getAll().toMutableList()

        try {
            synchroApi()
        }catch (e: UnknownHostException){
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Pas de connexion. activer le mode hors lignes")
            alertDialogBuilder.setPositiveButton("Yes"){ _, _ ->
            }
            alertDialogBuilder.show()
            listStation.addAll(saved)
            savedStation.addAll(saved)
        }

        for (i in saved){
            listStation.find { it.station_id == i.station_id }?.save = true
        }
        addMarker()
        mMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))

        mMap.setOnMarkerClickListener { marker ->
            saveStation(marker)
            false
        }

    }


    private fun synchroApi() {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(StationVelibService::class.java)

        runBlocking {
            val resultPos = service.getPosStation()
            val resultStatus = service.getStatusStation()
            val stationsPos = resultPos.data.stations
            val stationStatus = resultStatus.data.stations
            for (i in stationsPos) {
                for (j in stationStatus) {
                    if (i.station_id == j.station_id) {
                        val station = Station(
                            i.station_id,
                            i.name,
                            i.lat,
                            i.lon,
                            i.capacity,
                            j.num_bikes_available,
                            j.num_docks_available,
                            false,
                        )
                        listStation.add(station)
                        break
                    }
                }
            }
        }
    }

    private fun addMarker() {
        listStation.map {
            val point = LatLng(it.lat.toDouble(), it.lon.toDouble())
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(it.name)
                    .snippet(
                        "Vélos disponnibles :" + it.num_bikes_available + "/" + it.capacity
                                + "\nDocks disponnibles :" + it.num_docks_available + "/" + it.capacity
                    )
            )
            if(it.save){
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            }else{
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            }
            marker.tag = it.station_id.toString()
        }
    }

    private fun saveStation(marker: Marker) {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()

        val velibDao = db.stationDao()
        mMap.setOnInfoWindowClickListener {
            val markerName = marker.title
            val idStation = marker.tag
            val stationSave = listStation.find { it.station_id.toString() == idStation }
            if (stationSave != null) {
                val index = listStation.indexOf(stationSave)
                if (!stationSave.save) {
                    stationSave.save = true
                    listStation.removeAt(index)
                    listStation.add(index, stationSave)
                    savedStation.add(stationSave)
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))

                    velibDao.insertStation(stationSave)

                    Toast.makeText(
                        this@MapsActivity,
                        "$markerName a été sauvegardé",
                        Toast.LENGTH_SHORT
                    ).show()


                } else {
                    val Save = savedStation.find { it.station_id.toString() == idStation }
                    if (Save != null) {
                        val indexs = savedStation.indexOf(Save)
                        velibDao.deleteStation(Save)
                        savedStation.removeAt(indexs)
                    }
                    stationSave.save = false
                    listStation.removeAt(index)
                    listStation.add(index, stationSave)
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))

                    Toast.makeText(
                        this@MapsActivity,
                        "$markerName a été supprimé des sauvegardes",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }
    }


}

