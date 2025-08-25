package com.app.famcare.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.model.Nanny
import com.app.famcare.repository.NannyRepository
import com.bumptech.glide.Glide

class NannyAdapter(
    private val context: Context, var layoutResource: Int, private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NannyAdapter.NannyViewHolder>() {

    private var nannyList: MutableList<Nanny> = mutableListOf()
    private val nannyRepository = NannyRepository()

    init {
        fetchData()
    }

    class NannyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewNanny: ImageView = itemView.findViewById(R.id.imageViewNanny)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NannyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
        return NannyViewHolder(view)
    }

    override fun onBindViewHolder(holder: NannyViewHolder, position: Int) {
        val nanny = nannyList[position]
        Glide.with(context).load(nanny.pict).into(holder.imageViewNanny)
        holder.textViewName.text = nanny.name
        holder.textViewCategory.text = nanny.type
        holder.textViewRating.text = nanny.rate

        holder.itemView.setOnClickListener {
            onItemClick(nanny.id)
        }
    }

    override fun getItemCount(): Int {
        return nannyList.size
    }

    fun fetchData() {
        nannyRepository.getNannies(onSuccess = { nannies ->
            nannyList.clear()
            nannyList.addAll(nannies)
            notifyDataSetChanged()
        }, onFailure = { exception ->
        })
    }

    fun applyFilter(filterCriteria: Map<String, Any>, onFilterApplied: () -> Unit) {
        nannyRepository.getNannies(filterCriteria, onSuccess = { filteredNannies ->
            nannyList.clear()
            nannyList.addAll(filteredNannies)
            notifyDataSetChanged()
            onFilterApplied()
        }, onFailure = { exception ->
        })
    }

    fun updateNannyList(newNannyList: List<Nanny>) {
        nannyList.clear()
        nannyList.addAll(newNannyList)
        notifyDataSetChanged()
    }

    fun updateLayoutResource(newLayoutResource: Int) {
        layoutResource = newLayoutResource
        notifyDataSetChanged()
    }

    fun isListLayout(): Boolean {
        return layoutResource == R.layout.item_list_nanny
    }// Di dalam NannyAdapter.kt

    fun isEmpty(): Boolean {
        return nannyList.isEmpty()
    }

}