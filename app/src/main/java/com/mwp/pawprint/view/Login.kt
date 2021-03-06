package com.mwp.pawprint.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.mwp.pawprint.model.User
import com.mwp.pawprint.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private  lateinit var currUser : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        signUp_button.setOnClickListener {
            signUpNewUser()
        }

        login_button.setOnClickListener {
            loginProgressBar.visibility = View.VISIBLE
            logIn()
        }

        login_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
    }

    private fun getUser (currUid : String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.child(currUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currUser = dataSnapshot.getValue(User::class.java)!!
                Log.i("Login", "got user " + currUser)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Login", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
    private fun signUpNewUser () {
        login_email_et.text!!.clear()
        login_password_et.text!!.clear()
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }

    private fun logIn(){
        //TODO remove in release
        val testMode = false
        var email = ""
        var password = ""
        if (testMode) {
            email = "test1@gmail.com"
            password = "123456"
        } else {
            if (!validateForm()) {
                //one or more fileds are empty
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                return
            }
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            email = login_email_et.text.toString()
            password = login_password_et.text.toString()
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("SignInResult", "Successfully sign in for user with uid: ${it.result?.user?.uid}")
                    login_email_et.text!!.clear()
                    login_password_et.text!!.clear()

                    val intent = Intent(this, HomeScreen::class.java)
                    intent.putExtra("currUid", it.result!!.user.uid)
                    startActivity(intent)
                } else {
                    Log.e("SignInFailed", "failed to sign in with $email, $password")
                }

            }
            .addOnFailureListener {
                //sho user log in error message
                loginProgressBar.visibility = View.INVISIBLE
                when(it.message){
                    "The password is invalid or the user does not have a password." -> login_error.text = resources.getString(R.string.login_error_wrong_password)
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> login_error.text = resources.getString(R.string.login_error_wrong_email)
                    else -> login_error.text = resources.getString(R.string.login_error_text)
                }
                login_error.visibility = View.VISIBLE
                Log.d("SignInFailed", "Failed to sign in ${it.message}")
            }
    }

    private fun validateForm() : Boolean {
        if (login_email_et.text.toString().isEmpty()) {
            login_email_til.error = "Email is a required field."
            loginProgressBar.visibility = View.INVISIBLE
            return false
        }

        if (login_password_et.text.toString().isEmpty()) {
            login_password_til.error = "Password is a required field."
            loginProgressBar.visibility = View.INVISIBLE
            return false
        }

        if (!login_email_et.text.toString().contains("@")) {
            login_email_til.error = "Invalid Email"
            loginProgressBar.visibility = View.INVISIBLE
            return false
        }

        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        loginProgressBar.visibility = View.INVISIBLE
    }
}
