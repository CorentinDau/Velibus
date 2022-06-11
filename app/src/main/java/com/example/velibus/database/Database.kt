package com.example.velibus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.velibus.dao.StationDao
import com.example.velibus.model.Station

@Database(entities = [Station::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao() : StationDao
}