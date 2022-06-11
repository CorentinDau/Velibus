package com.example.velibus.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Station (
    @PrimaryKey val station_id:Long,
    val name:String,
    val lat:Float,
    val lon:Float,
    val capacity:Int,
    val num_bikes_available :Int,
    val num_docks_available:Int,
    var save:Boolean=false
):Serializable