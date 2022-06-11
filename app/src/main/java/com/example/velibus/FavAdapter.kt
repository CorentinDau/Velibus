package com.example.velibus

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.velibus.database.AppDatabase
import com.example.velibus.model.Station
import kotlin.system.exitProcess

class FavAdapter(val listFav: List<Station>) :
    RecyclerView.Adapter<FavAdapter.FavViewHolder>() {

    class FavViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favView = inflater.inflate(R.layout.adapter_fav, parent, false)
        return FavViewHolder(favView)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val station = listFav[position]
        listFav as MutableList<Station>


        holder.view.findViewById<Button>(R.id.adapter_fav_unFav).setOnClickListener {
            val context = it.context
            val intent = Intent(context,SavedStationActivity::class.java)
            intent.putExtra("position",position)
            context.startActivity(intent)
        }
        val favTitleview =
            holder.view.findViewById<TextView>(R.id.adapter_fav_titleview)

        favTitleview.text = station.name

        val favTextview =
            holder.view.findViewById<TextView>(R.id.adapter_fav_textview)

        favTextview.text =
            "Vélos disponnibles :" + station.num_bikes_available + "/" + station.capacity + "\nDocks disponnibles :" + station.num_docks_available + "/" + station.capacity

    }

    override fun getItemCount() = listFav.size

}

