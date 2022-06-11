package com.example.velibus.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.velibus.model.Station

@Dao
interface StationDao {
    @Query("SELECT * FROM station")
    fun getAll(): List<Station>

    @Query("SELECT * FROM station WHERE station_id = (:stationId)")
    fun loadById(stationId:Long): Station

    @Insert
    fun insertStation(station:Station)

    @Delete
    fun deleteStation(station:Station)
}