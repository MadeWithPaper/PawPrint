package com.example.pawprint.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.pawprint.model.User
import com.example.pawprint.R
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

        signUpText.setOnClickListener {
            signUpNewUser()
        }

        login_button.setOnClickListener {
            logIn()
        }
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
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }

    private fun logIn(){
        val testMode = true
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
            email = login_email.text.toString()
            password = login_password.text.toString()
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("SignInResult", "Successfully sign in for user with uid: ${it.result?.user?.uid}")
                    login_email.text.clear()
                    login_password.text.clear()

                    val intent = Intent(this, HomeScreen::class.java)
                    intent.putExtra("currUid", it.result!!.user.uid)
                    startActivity(intent)

                } else {
                    Toast.makeText(this, "Failed to sign In", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {
                Log.d("SignInFailed", "Failed to create user: ${it.message}")
            }
    }

    private fun validateForm() : Boolean {
        if (TextUtils.isEmpty(login_email.text.toString())) {
            login_email.setError("Email is empty.")
            return false
        }

        if (TextUtils.isEmpty(login_password.text.toString())) {
            login_password.setError("Password is empty.")
            return false
        }

        if (!login_email.text.toString().contains("@")) {
            login_email.setError("Ensure to enter a valid email account")
            return false
        }

        return true
    }
}
