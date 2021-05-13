package com.example.aop_part3_chapter06.MyPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.aop_part3_chapter06.DBKEY.Companion.ARTICLES
import com.example.aop_part3_chapter06.R
import com.example.aop_part3_chapter06.databinding.FragmentMypageBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding
    private lateinit var baseDB: DatabaseReference
    private lateinit var auth : FirebaseAuth
    private val TAG = "LifeCycle"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "$TAG::FRonCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        Log.e(TAG, "$TAG::FRonCreateView")
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        initBaseDB()
        initBindButton()
        initEmailAndPasswordEditText()
        Log.e(TAG, "$TAG::FROnViewCreated")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.e(TAG, "$TAG::FRonViewStateRestored")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "$TAG::FRonStart")

        if(auth.currentUser == null) {
            binding.emailEditText.text.clear()
            binding.passwordEditText.text.clear()
            binding.emailEditText.isEnabled = true
            binding.passwordEditText.isEnabled = true

            binding.loginButton.isEnabled = false
            binding.signUpButton.isEnabled = false
            binding.loginButton.text = getString(R.string.login)
        } else {
            binding.emailEditText.setText(auth.currentUser!!.email)
            binding.passwordEditText.setText("********")
            binding.emailEditText.isEnabled = false
            binding.passwordEditText.isEnabled = false
            binding.loginButton.text = getString(R.string.logout)
            binding.signUpButton.isEnabled = false
            binding.loginButton.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "$TAG::FRonResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "$TAG::FRonPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "$TAG::FRonStop")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e(TAG, "$TAG::FRonSaveInstanceState")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "$TAG::FRonDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "$TAG::FRonDestroy")
    }


    private fun initBaseDB() {
        val database = FirebaseDatabase.getInstance()
        baseDB = database.reference.child(ARTICLES)
    }


    private fun initBindButton() {
        binding.loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            if (auth.currentUser == null) {
                //TODO : 로그인 되어 있지 않은 상태
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleSuccessLogin()
                            Snackbar.make(it, "로그인 되었습니다.", Snackbar.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                logoutProcess()
                Snackbar.make(it, "로그아웃 되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.signUpButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "회원가입에 성공하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        logoutProcess()
                    } else {
                        Toast.makeText(
                            context,
                            "이미 가입한 이메일이거나, 회원가입에 실패하였습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun logoutProcess() {
        binding.emailEditText.text.clear()
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.text.clear()
        binding.passwordEditText.isEnabled = true
        binding.loginButton.text = getString(R.string.login)
        binding.emailEditText.requestFocus()
        auth.signOut()
    }

    private fun initEmailAndPasswordEditText() {
        binding.emailEditText.addTextChangedListener {
            val enable =
                binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.loginButton.isEnabled = enable
            binding.signUpButton.isEnabled = enable
        }
        binding.passwordEditText.addTextChangedListener {
            val enable =
                binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.loginButton.isEnabled = enable
            binding.signUpButton.isEnabled = enable
        }
    }

    private fun getInputEmail(): String {
        return binding.emailEditText.text.toString()
    }

    private fun getInputPassword(): String {
        return binding.passwordEditText.text.toString()
    }

    private fun handleSuccessLogin() {
        if (auth.currentUser == null) {
            //TODO: 맨 처음에 회원가입 안하고 로그인 한 경우
            Toast.makeText(context, "로그인에 실패하였습니다", Toast.LENGTH_SHORT).show()
            return
        }
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isEnabled = false
        binding.signUpButton.isEnabled = false
        binding.loginButton.setText(getString(R.string.logout))
    }
}