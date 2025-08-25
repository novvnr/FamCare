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
import com.bumptech.glide.Glide

class BookmarkAdapter(
    private val context: Context,
    private val nannyList: List<Nanny>,
    private val clickListener: OnBookmarkItemClickListener
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    interface OnBookmarkItemClickListener {
        fun onBookmarkItemClick(nanny: Nanny)
        fun onBookmarkIconClick(nanny: Nanny)
    }

    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewNanny: ImageView = itemView.findViewById(R.id.imageViewNanny)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        val imageViewBookmark: ImageView = itemView.findViewById(R.id.iv_bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val nanny = nannyList[position]

        Glide.with(context).load(nanny.pict).placeholder(R.drawable.placeholder_image)
            .into(holder.imageViewNanny)

        holder.textViewName.text = nanny.name
        holder.textViewCategory.text = nanny.type
        holder.textViewRating.text = nanny.rate

        if (nanny.isBookmarked) {
            holder.imageViewBookmark.setImageResource(R.drawable.baseline_bookmark_24)
        } else {
            holder.imageViewBookmark.setImageResource(R.drawable.outline_bookmark_24)
        }

        holder.itemView.setOnClickListener {
            clickListener.onBookmarkItemClick(nanny)
        }

        holder.imageViewBookmark.setOnClickListener {
            clickListener.onBookmarkIconClick(nanny)
        }
    }

    override fun getItemCount(): Int {
        return nannyList.size
    }
}