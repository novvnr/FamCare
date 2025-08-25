package com.app.famcare.view.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.databinding.ItemMessageBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val messages: MutableList<ChatMessage> = mutableListOf()
    private val firestore = FirebaseFirestore.getInstance()

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMessageBinding.bind(itemView)

        fun bind(message: ChatMessage) {
            GlobalScope.launch(Dispatchers.Main) {
                val latestUserInfo = getLatestUserInfo(message.senderId)
                val displayName = latestUserInfo?.first ?: message.senderName
                val profileImageUrl = latestUserInfo?.second

                binding.tvMessenger.text = displayName
                if (profileImageUrl != null) {
                    Glide.with(binding.ivMessenger.context).load(profileImageUrl).circleCrop()
                        .into(binding.ivMessenger)
                } else {
                    Glide.with(binding.ivMessenger.context).load(R.drawable.user).circleCrop()
                        .into(binding.ivMessenger)
                }
            }
            binding.tvMessage.text = message.message
            binding.tvTimestamp.text = formatTime(message.timestamp)
        }

        private fun formatTime(timestamp: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }

        private suspend fun getLatestUserInfo(senderId: String): Pair<String?, String?>? {
            return try {
                val document = firestore.collection("User").document(senderId).get().await()
                val fullName = document.getString("fullName")
                val profileImageUrl = document.getString("profileImageUrl")
                Pair(fullName, profileImageUrl)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}