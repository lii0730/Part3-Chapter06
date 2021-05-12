package com.example.aop_part3_chapter06.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.aop_part3_chapter06.DBKEY
import com.example.aop_part3_chapter06.DBKEY.Companion.SELL_ITEM
import com.example.aop_part3_chapter06.Home.ArticleModel
import com.example.aop_part3_chapter06.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

class AddArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddArticleBinding
    private lateinit var baseDB: DatabaseReference
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private var selectedUri: Uri? = null

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

            val title = binding.articleTitle.text.toString()
            val price = binding.articlePrice.text.toString()
            val sellerId = getCurrentUserID()

            showProgress()
            //TODO: 이미지가 있으면 업로드 과정 추가
            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId!!, title, price, uri)
                    },
                    errorHandler = {
                        Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else {
                uploadArticle(sellerId!!, title, price, "")
            }


        }

        binding.addImageButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    showPermissionContextPopUp()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        100
                    )
                }
            }
        }
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${Date().time}"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    //TODO: Upload Complete
                        //업로드가 되면 다운로드URL을 가지고 와야함
                    storage.reference.child("article/photo").child(fileName).downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId: String, title: String, price: String, imageUri: String) {
        val model = ArticleModel(sellerId, title, Date().time, "$price 원", imageUri)
        baseDB.push().setValue(model)
        hideProgress()
        this.finish()
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

    private fun showProgress() {
        binding.progressBar.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBar.isVisible = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            200 -> {
                val uri = data?.data
                if (uri != null) {
                    binding.showArticleImageView.setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopUp() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의", { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
            })
            .create()
            .show()
    }
}