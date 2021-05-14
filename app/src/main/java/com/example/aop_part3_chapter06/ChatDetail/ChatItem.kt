package com.example.aop_part3_chapter06.ChatDetail

data class ChatItem(
    val senderId: String,
    val message: String
) {
    constructor() : this("", "")
}
