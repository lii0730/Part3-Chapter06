package com.example.aop_part3_chapter06.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aop_part3_chapter06.Activity.AddArticleActivity
import com.example.aop_part3_chapter06.ChatList.ChatListItem
import com.example.aop_part3_chapter06.DBKEY.Companion.ARTICLES
import com.example.aop_part3_chapter06.DBKEY.Companion.CHAT
import com.example.aop_part3_chapter06.DBKEY.Companion.USERS
import com.example.aop_part3_chapter06.R
import com.example.aop_part3_chapter06.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB : DatabaseReference

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
    }

    private var binding: FragmentHomeBinding? = null

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private lateinit var articleAdapter: ArticleAdapter

    private lateinit var articleList: MutableList<ArticleModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
//        binding = fragmentHomeBinding

        articleDB = FirebaseDatabase.getInstance().reference.child(ARTICLES)
        userDB = FirebaseDatabase.getInstance().reference.child(USERS)

        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->

            if (auth.currentUser != null) {
                if (auth.currentUser?.uid != articleModel.sellerId) {
                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser!!.uid,
                        sellerId = articleModel.sellerId,
                        itemTitle = articleModel.title,
                        key = Date().time
                    )

                    userDB.child(auth.currentUser!!.uid).child(CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(articleModel.sellerId).child(CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(view, "채팅 방이 생성되었습니다. 채팅 탭에서 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(view, "내가 올린 아이템입니다.", Snackbar.LENGTH_SHORT).show()
                }

            } else {
                Snackbar.make(view, "로그인 후 이용해주세요", Snackbar.LENGTH_SHORT).show()
            }

        })
        articleList = mutableListOf()
        articleList.clear()

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        articleDB.addChildEventListener(listener)

        fragmentHomeBinding.addArticleButton.setOnClickListener {
            if (auth.currentUser != null) {
                //TODO: 물품 등록 Activity 표출
                startActivity(Intent(requireContext(), AddArticleActivity::class.java))
            } else {
                Snackbar.make(view, "로그인 후 이용해 주세요", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //TODO: View가 Destroy 될 때 listener 제거
        articleDB.removeEventListener(listener)
    }
}