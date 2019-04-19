package com.mwp.pawprint.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.log


class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private  lateinit var currUser : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        signUpText.setOnClickListener {
            signUpNewUser()
        }

        login_button.setOnClickListener {
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
            password = "1234567"
        } else {
            if (!validateForm()) {
                //one or more fileds are empty
                return
            }
            email = login_email_et.text.toString()
            password = login_password_et.text.toString()
            //email = login_email.text.toString()
            //password = login_password.text.toString()
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("SignInResult", "Successfully sign in for user with uid: ${it.result?.user?.uid}")
                    login_email_et.text!!.clear()
                    login_password_et.text!!.clear()
                    //login_email.text.clear()
                    //login_password.text.clear()

                    val intent = Intent(this, HomeScreen::class.java)
                    intent.putExtra("currUid", it.result!!.user.uid)
                    startActivity(intent)

                } else {
                    Log.e("SignInFailed", "failed to sign in with $email, $password")
                    //Toast.makeText(this, "Failed to sign In", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {
                Log.d("SignInFailed", "Failed to sign in ${it.message}")
            }
    }

    private fun validateForm() : Boolean {
        if (login_email_et.text!!.isEmpty()) {
            login_email_til.error = "Email is a required field."
            return false
        }

        if (login_password_et.text!!.isEmpty()) {
            login_password_til.error = "Password is a required field."
            return false
        }

        if (!login_email_et.text.toString().contains("@")) {
            login_email_til.error = "Invalid Email"
            return false
        }

        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
