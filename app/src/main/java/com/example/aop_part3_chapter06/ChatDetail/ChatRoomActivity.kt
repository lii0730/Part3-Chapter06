package com.example.aop_part3_chapter06.ChatDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aop_part3_chapter06.DBKEY.Companion.MESSAGES
import com.example.aop_part3_chapter06.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class ChatRoomActivity : AppCompatActivity() {

    private val auth : FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val chatList = mutableListOf<ChatItem>()
    private val adapter = ChatItemAdapter(this)
    private var chatDB : DatabaseReference? = null

    private val chatRecyclerView : RecyclerView by lazy {
        findViewById(R.id.chatRecyclerView)
    }

    private val sendButton : Button by lazy {
        findViewById(R.id.sendButton)
    }

    private val messageEditText : EditText by lazy {
        findViewById(R.id.messageEditText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatKey = intent.getLongExtra("chatKey", -1)
        chatDB = FirebaseDatabase.getInstance().reference.child(MESSAGES).child("$chatKey")
        chatDB!!.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatList.add(chatItem)
                    adapter.submitList(chatList)
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.i("ChatRoomActivity", e.toString())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })


        chatRecyclerView.adapter = adapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val chatItem = ChatItem(
                senderId = auth.currentUser!!.uid,
                message = messageEditText.text.toString()
            )
            chatDB?.push()?.setValue(chatItem)
            messageEditText.text.clear()
        }
    }
}