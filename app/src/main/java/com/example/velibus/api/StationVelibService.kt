package com.example.velibus.api

import com.example.velibus.model.Station
import retrofit2.http.GET

interface StationVelibService {
    @GET("station_information.json")
    suspend fun getPosStation():  GetPosStationResult

    @GET("station_status.json")
    suspend fun getStatusStation(): GetStatusStationResult
}


data class GetPosStationResult(val data:StationsPosResult)
data class StationsPosResult(val stations : List<StationPos>)
data class StationPos(val station_id:Long, val name:String, val lat:Float, val lon:Float, val capacity:Int)

data class GetStatusStationResult(val data:StationsStatusResult)
data class StationsStatusResult(val stations:List<StationStatus>)
data class StationStatus(val station_id:Long, val num_bikes_available :Int, val num_docks_available:Int)