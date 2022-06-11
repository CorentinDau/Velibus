package com.example.velibus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.velibus.database.AppDatabase
import com.example.velibus.model.Station

class SavedStationActivity  : AppCompatActivity(){

    var listFavRecyclerview : RecyclerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fav)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
        val position = intent.getIntExtra("position",9999)
        val staionDao = db.stationDao()
        val listStation: MutableList<Station> = staionDao.getAll().toMutableList()

        findViewById<Button>(R.id.Retour)
            .setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }

            listFavRecyclerview = findViewById<RecyclerView>(R.id.list_fav_recyclerview)

            listFavRecyclerview?.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        if (position != 9999){
            val del = staionDao.loadById(listStation[position].station_id)
            staionDao.deleteStation(del)
            listStation.removeAt(position)
        }
        if (listStation.isNotEmpty()){
            val adapter = FavAdapter(listStation)
            adapter.notifyDataSetChanged()
            listFavRecyclerview?.adapter = adapter
        }else{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }


}