package com.example.aop_part3_chapter06.MyPage

import android.os.Bundle
import android.util.Log
import android.view.View
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
    private lateinit var articleDB: DatabaseReference
    private lateinit var auth : FirebaseAuth
    private val TAG = "LifeCycle"

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.e(TAG, "$TAG::FRonCreate")
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
//        Log.e(TAG, "$TAG::FRonCreateView")
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        initArticleDB()
        initBindButton()
        initEmailAndPasswordEditText()
        Log.e(TAG, "$TAG::FROnViewCreated")
    }

//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//        Log.e(TAG, "$TAG::FRonViewStateRestored")
//    }

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

//    override fun onResume() {
//        super.onResume()
//        Log.e(TAG, "$TAG::FRonResume")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.e(TAG, "$TAG::FRonPause")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.e(TAG, "$TAG::FRonStop")
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.e(TAG, "$TAG::FRonSaveInstanceState")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.e(TAG, "$TAG::FRonDestroyView")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.e(TAG, "$TAG::FRonDestroy")
//    }


    private fun initArticleDB() {
        val database = FirebaseDatabase.getInstance()
        articleDB = database.reference.child(ARTICLES)
    }


    private fun initBindButton() {
        binding.loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            if (auth.currentUser == null) {
                //TODO : ????????? ?????? ?????? ?????? ??????
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleSuccessLogin()
                            Snackbar.make(it, "????????? ???????????????.", Snackbar.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "???????????? ??????????????????. ????????? ?????? ??????????????? ??????????????????",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                logoutProcess()
                Snackbar.make(it, "???????????? ???????????????.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.signUpButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // ??????????????? ???????????? currentUser ??? ???????????? ????????? ???
                        Toast.makeText(
                            context,
                            "??????????????? ?????????????????????.",
                            Toast.LENGTH_SHORT
                        ).show()
                        logoutProcess()
                    } else {
                        Toast.makeText(
                            context,
                            "?????? ????????? ??????????????????, ??????????????? ?????????????????????",
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
            //TODO: ??? ????????? ???????????? ????????? ????????? ??? ??????
            Toast.makeText(context, "???????????? ?????????????????????", Toast.LENGTH_SHORT).show()
            return
        }
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isEnabled = false
        binding.signUpButton.isEnabled = false
        binding.loginButton.setText(getString(R.string.logout))
    }
}