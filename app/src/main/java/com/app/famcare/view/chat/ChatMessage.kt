package com.app.famcare.view.chat

data class ChatMessage(
    val senderId: String = "",
    var senderName: String = "",
    var senderPhotoUrl: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val formattedTimestamp: String = "",
    var receiverId: String = ""
)