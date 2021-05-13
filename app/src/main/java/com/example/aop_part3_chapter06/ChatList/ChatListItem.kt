package com.example.aop_part3_chapter06.ChatList

data class ChatListItem(
    val buyerId: String,
    val sellerId: String,
    val itemTitle: String,
    val key: Long
) {
    constructor() : this("", "","", 0)
}
