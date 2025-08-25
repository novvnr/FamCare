package com.app.famcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.model.BookingDaily
import com.app.famcare.model.BookingMonthly
import com.app.famcare.model.BookingMonthlyHistory

class BookingAdapter(
    private var bookingList: List<Any>, private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(bookingID: String)
        fun onChatClick(bookingID: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DAILY_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_history_b_d, parent, false)
                DailyViewHolder(view)
            }

            MONTHLY_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_history_b_m, parent, false)
                MonthlyViewHolder(view)
            }

            MONTHLY_HISTORY_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_row_history_b_m, parent, false
                )
                MonthlyHistoryViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val booking = bookingList[position]
        when (holder) {
            is DailyViewHolder -> {
                val bookingDaily = booking as BookingDaily
                holder.bind(bookingDaily)
            }

            is MonthlyViewHolder -> {
                val bookingMonthly = booking as BookingMonthly
                holder.bind(bookingMonthly)
            }

            is MonthlyHistoryViewHolder -> {
                val bookingMonthlyHistory = booking as BookingMonthlyHistory
                holder.bind(bookingMonthlyHistory)
            }
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (bookingList[position]) {
            is BookingDaily -> DAILY_VIEW_TYPE
            is BookingMonthly -> MONTHLY_VIEW_TYPE
            is BookingMonthlyHistory -> MONTHLY_HISTORY_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid booking type")
        }
    }

    inner class DailyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewID: TextView = itemView.findViewById(R.id.textViewID)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewBookHours: TextView = itemView.findViewById(R.id.textViewBookHours)
        private val textViewEndHours: TextView = itemView.findViewById(R.id.textViewEndHours)
        private val chatIcon: ImageView = itemView.findViewById(R.id.chatIcon)

        fun bind(booking: BookingDaily) {
            textViewID.text = booking.bookID
            textViewName.text = booking.nannyName
            textViewDate.text = booking.bookDate
            textViewBookHours.text = booking.bookHours
            textViewEndHours.text = booking.endHours

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(booking.bookID)
                }
            }

            chatIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(booking.bookID)
                }
            }
        }
    }

    inner class MonthlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewID: TextView = itemView.findViewById(R.id.textViewID)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewStartDate: TextView = itemView.findViewById(R.id.textViewStartDate)
        private val textViewEndDate: TextView = itemView.findViewById(R.id.textViewEndDate)
        private val chatIcon: ImageView = itemView.findViewById(R.id.chatIcon)

        fun bind(booking: BookingMonthly) {
            textViewID.text = booking.bookID
            textViewName.text = booking.nannyName
            textViewStartDate.text = booking.startDate
            textViewEndDate.text = booking.endDate

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(booking.bookID)
                }
            }

            chatIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(booking.bookID)
                }
            }
        }
    }

    inner class MonthlyHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewID: TextView = itemView.findViewById(R.id.textViewID)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewStartDate: TextView = itemView.findViewById(R.id.textViewStartDate)
        private val textViewEndDate: TextView = itemView.findViewById(R.id.textViewEndDate)
        private val chatIcon: ImageView = itemView.findViewById(R.id.chatIcon)

        fun bind(booking: BookingMonthlyHistory) {
            textViewID.text = booking.bookID
            textViewName.text = booking.nannyName
            textViewStartDate.text = booking.startDate
            textViewEndDate.text = booking.endDate

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(booking.bookID)
                }
            }

            chatIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(booking.bookID)
                }
            }
        }
    }

    fun setData(newList: List<Any>) {
        bookingList = newList
        notifyDataSetChanged()
    }

    companion object {
        private const val DAILY_VIEW_TYPE = 1
        private const val MONTHLY_VIEW_TYPE = 2
        private const val MONTHLY_HISTORY_VIEW_TYPE = 3
    }
}