package com.example.aop_part3_chapter06.ChatList

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aop_part3_chapter06.ChatDetail.ChatRoomActivity
import com.example.aop_part3_chapter06.DBKEY.Companion.CHAT
import com.example.aop_part3_chapter06.DBKEY.Companion.USERS
import com.example.aop_part3_chapter06.R
import com.example.aop_part3_chapter06.databinding.FragmentChatlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatListFragment : Fragment(R.layout.fragment_chatlist) {

    private var binding: FragmentChatlistBinding? = null
    private lateinit var chatListAdapter: ChatListAdapter
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val chatRoomList = mutableListOf<ChatListItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChatlistBinding = FragmentChatlistBinding.bind(view)
        binding = fragmentChatlistBinding

        chatListAdapter = ChatListAdapter(onItemClicked = {
            //TODO: 채팅방 이동 코드
            context?.let { context->
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("chatKey", it.key)
                startActivity(intent)
            }
        })

        chatRoomList.clear()

        fragmentChatlistBinding.chatListRecyclerView.adapter = chatListAdapter
        fragmentChatlistBinding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)

        if (auth.currentUser == null) {
            return
        }

        val chatDB = FirebaseDatabase.getInstance().reference
            .child(USERS)
            .child(auth.currentUser!!.uid)
            .child(CHAT)

        chatDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return

                    chatRoomList.add(model)
                }
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    override fun onResume() {
        super.onResume()
        chatListAdapter.notifyDataSetChanged()
    }
}