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
import com.example.aop_part3_chapter06.DBKEY.Companion.ARTICLES
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
    private lateinit var articleDB: DatabaseReference
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButton()
        articleDB = FirebaseDatabase.getInstance().reference.child(ARTICLES)
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

            //TODO: Title, Price 빈칸 체크
            if(title.isNotEmpty() && price.isNotEmpty()) {
                showProgress()
                //TODO: 이미지가 있으면 업로드 과정 추가
                if (selectedUri != null) {
                    //TODO: PhotoUri가 null일 경우 예외처리
                    val photoUri = selectedUri ?: return@setOnClickListener
                    uploadPhoto(photoUri,
                        successHandler = { uri ->
                            //TODO: uri는 successHandler로 넘어온 String 인자, 지금은 storage에 저장하고 얻은 다운 받을 수 있는 url
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
            } else {
                Toast.makeText(this, "제목과 가격을 모두 입력해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            }

        }

        binding.addImageButton.setOnClickListener {
            when {
                ActivityCompat.checkSelfPermission(
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
        //TODO: Storage에 DateTime을 파일명으로 하여 사진 등록
        val fileName = "${Date().time}"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    //TODO: Upload Complete -> 업로드가 되면 다운로드 URL을 가지고 와야함
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
        //TODO: RealTime Database 물품 등록
        val model = ArticleModel(sellerId, title, Date().time, "$price 원", imageUri)
        articleDB.push().setValue(model)
        hideProgress()
        this.finish()
    }

    private fun getCurrentUserID(): String? {
        //TODO: 현재 로그인된 User ID 가져오기
        val auth = FirebaseAuth.getInstance()
        var currentUserID: String?
        if (auth.currentUser != null) {
            currentUserID = auth.currentUser!!.uid
            return currentUserID
        } else {
            Toast.makeText(this, "로그인을 해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun showProgress() {
        //TODO: 진행상황 표시
        binding.progressBar.isVisible = true
    }

    private fun hideProgress() {
        //TODO: 진행상황 숨김
        binding.progressBar.isVisible = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startContentProvider() {
        //TODO: 이미지 추가를 위한 SAF 접근
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GET_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            //TODO: 예외 처리
            return
        }
        when (requestCode) {
            REQUEST_GET_IMAGE_CODE -> {
                val uri = data?.data // 선택한 이미지에 대한 uri 가 넘어옴
                if (uri != null) {
                    //TODO: 선택한 이미지 이미지뷰에 표시
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
        //TODO: 권한 필요 팝업 표시
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE
                )
            }
            .create()
            .show()
    }

    companion object {
        val REQUEST_GET_IMAGE_CODE = 200
        val REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE = 100
    }
}