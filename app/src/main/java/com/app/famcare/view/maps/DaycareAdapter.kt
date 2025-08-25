package com.app.famcare.view.maps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint

data class Daycare(
    val name: String,
    val location: String,
    val photoURL: String,
    val coordinat: GeoPoint,
    val distanceFromUser: Double,
    val websiteURL: String
)

class DaycareAdapter(
    private var daycares: List<Daycare>, private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<DaycareAdapter.DaycareViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaycareViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_daycare, parent, false)
        return DaycareViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DaycareViewHolder, position: Int) {
        val daycare = daycares[position]
        holder.nameTextView.text = daycare.name
        holder.locationTextView.text = daycare.location
        holder.distanceTextView.text = if (daycare.distanceFromUser == 0.0) {
            "-"
        } else {
            "${daycare.distanceFromUser} km"
        }

        Glide.with(holder.itemView.context).load(daycare.photoURL).into(holder.photoImageView)

        holder.itemView.setOnClickListener {
            itemClickListener(daycare.websiteURL)
        }
    }

    override fun getItemCount() = daycares.size

    fun updateDaycares(newDaycares: List<Daycare>) {
        daycares = newDaycares
        notifyDataSetChanged()
    }

    class DaycareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.daycare_name)
        val locationTextView: TextView = itemView.findViewById(R.id.daycare_location)
        val distanceTextView: TextView = itemView.findViewById(R.id.tvDistancetoUser)
        val photoImageView: ImageView = itemView.findViewById(R.id.daycare_image)
    }
}