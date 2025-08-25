package com.app.famcare.view.chat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.famcare.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ChatAdapter
    private lateinit var messageReference: DatabaseReference
    private var currentUserName: String? = null
    private var currentUserPhotoUrl: String? = null
    private var currentUserId: String? = null
    private var nannyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database =
            FirebaseDatabase.getInstance("https://famcare-mobprog-default-rtdb.asia-southeast1.firebasedatabase.app/")
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        messageReference = database.getReference("messages")

        adapter = ChatAdapter()
        binding.messageRecyclerView.adapter = adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        loadCurrentUserNameAndPhotoUrl()

        loadMessages()

        nannyId = intent.getStringExtra("nannyID")
        if (nannyId != null) {
            loadNannyDetails(nannyId!!)
        } else {
            Toast.makeText(this, "Nanny ID is missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadNannyDetails(nannyID: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val document = firestore.collection("Nanny").document(nannyID).get().await()
                val nannyName = document.getString("name")
                val nannyPhotoUrl = document.getString("pict")

                if (nannyName != null) {
                    binding.tvNannyName.text = nannyName
                } else {
                    binding.tvNannyName.text = "Nanny"
                }

                if (nannyPhotoUrl != null) {
                    Glide.with(this@ChatActivity).load(nannyPhotoUrl).circleCrop()
                        .into(binding.ivNannyPhoto)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChatActivity, "Failed to load nanny details", Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Failed to load nanny details: ${e.message}")
            }
        }
    }

    private fun loadCurrentUserNameAndPhotoUrl() {
        val currentUser = auth.currentUser
        currentUserId = currentUser?.uid

        if (currentUserId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val document =
                        firestore.collection("User").document(currentUserId!!).get().await()
                    currentUserName = document.getString("fullName")
                    currentUserPhotoUrl = document.getString("profileImageUrl")
                    Log.d(
                        TAG, "Current user name: $currentUserName, photo URL: $currentUserPhotoUrl"
                    )
                } catch (exception: Exception) {
                    Log.e(TAG, "Failed to fetch user data: ${exception.message}")
                }
            }
        }
    }

    private fun loadMessages() {
        messageReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                if (message != null && ((message.senderId == currentUserId && message.receiverId == nannyId) || (message.senderId == nannyId && message.receiverId == currentUserId))) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val latestUserInfo = getLatestUserInfo(message.senderId)
                        if (latestUserInfo != null) {
                            message.senderName = latestUserInfo.first ?: message.senderName
                            message.senderPhotoUrl = latestUserInfo.second ?: message.senderPhotoUrl
                        }
                        adapter.addMessage(message)
                        binding.messageRecyclerView.scrollToPosition(adapter.itemCount - 1)
                        Log.d(TAG, "Message added: $message")
                    }
                } else {
                    Log.e(TAG, "Failed to parse message or message is not part of the current chat")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load messages", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "Failed to load messages: ${error.message}")
            }
        })
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

    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val currentUser = auth.currentUser
            val currentUserId = currentUser?.uid ?: ""
            val currentUserName = this.currentUserName ?: "Anonymous"
            val currentUserPhotoUrl = this.currentUserPhotoUrl ?: ""
            val timestamp = System.currentTimeMillis()
            val formattedTimestamp = formatDate(timestamp)
            val message = ChatMessage(
                senderId = currentUserId,
                senderName = currentUserName,
                senderPhotoUrl = currentUserPhotoUrl,
                message = messageText,
                timestamp = timestamp,
                formattedTimestamp = formattedTimestamp,
                receiverId = nannyId!!
            )

            Log.d(TAG, "Trying to send message: $message")

            messageReference.push().setValue(message).addOnSuccessListener {
                binding.messageEditText.text.clear()
                Log.d(TAG, "Message sent successfully: $messageText")
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to send message: ${exception.message}")
            }
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date(timestamp))
    }

    companion object {
        private const val TAG = "ChatActivity"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}