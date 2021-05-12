package com.example.aop_part3_chapter06.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aop_part3_chapter06.DBKEY
import com.example.aop_part3_chapter06.DBKEY.Companion.SELL_ITEM
import com.example.aop_part3_chapter06.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddArticleBinding
    private lateinit var baseDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButton()
        val database = FirebaseDatabase.getInstance()
        baseDB = database.getReference(DBKEY.ARTICLES)
    }

    private fun initButton() {
        binding.closeTextView.setOnClickListener {
            this.finish()
        }

        binding.completeTextView.setOnClickListener {
            //TODO: 등록 내용 RealTime DB 저장 -> User별로 Item 등록현황
            val itemInfo = mutableMapOf<String, Any>()
            itemInfo["Title"] = binding.articleTitle.text.toString()
            itemInfo["Price"] = binding.articlePrice.text.toString()
//            itemInfo["ImageUrl"] = binding.showArticleImageView.drawable
            getCurrentUserID()?.let {
//                    userID -> baseDB.child(userID).child(DBKEY.SELL_ITEM).updateChildren(itemInfo)
                    userID ->
                baseDB.child(userID).child(DBKEY.SELL_ITEM)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            this.finish()
        }
    }

    private fun getCurrentUserID(): String? {
        val auth = FirebaseAuth.getInstance()
        var currentUserID: String?
        if (auth.currentUser != null) {
            currentUserID = auth.currentUser.uid
            return currentUserID
        } else {
            Toast.makeText(this, "로그인을 해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            return null
        }
    }
}