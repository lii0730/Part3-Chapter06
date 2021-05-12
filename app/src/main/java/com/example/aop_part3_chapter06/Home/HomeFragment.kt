package com.example.aop_part3_chapter06.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aop_part3_chapter06.Activity.AddArticleActivity
import com.example.aop_part3_chapter06.DBKEY.Companion.ARTICLES
import com.example.aop_part3_chapter06.R
import com.example.aop_part3_chapter06.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding : FragmentHomeBinding? = null
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var articleAdapter : ArticleAdapter
    private lateinit var articleList : MutableList<ArticleModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding
        articleAdapter = ArticleAdapter()
        articleList = mutableListOf()

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter
        fragmentHomeBinding.addArticleButton.setOnClickListener {
            if(auth.currentUser != null) {
                //TODO: 물품 등록 Activity 표출
                startActivity(Intent(context, AddArticleActivity::class.java))
            } else {
                Snackbar.make(view, "로그인 후 이용해 주세요", Snackbar.LENGTH_SHORT).show()
            }
        }

//        articleList.add(ArticleModel("aaaa", "팝니다~", Date().time, "10000", ""))
//        articleAdapter.submitList(articleList)

    }
}