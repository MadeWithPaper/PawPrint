package com.mwp.pawprint.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.mwp.pawprint.model.User
import com.mwp.pawprint.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*
import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.MotionEvent

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //init firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        registerButton.setOnClickListener {
            addNewUser()
        }

        signup_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
    }

    private fun addNewUser(){

        if (!validateForm()) {
            //one or more fileds are empty
            return
        }

        val name = signUp_name_et.text.toString()
        val email = signUp_email_et.text.toString()
        val password = signUp_password_et.text.toString()

        Log.i("SignUp", "Creating new user with the following info:")
        Log.i("SignUp", "Name: " + name)
        Log.i("SignUp", "Email: " + email)
        //Log.i("SignUp", "Password: " + password)

        //Firebase auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    Log.d("SignUpResult", "Successfully created user with uid: ${it.result!!.user.uid}")

                    val currUser = saveUserToDB(it.result!!.user.uid, name, email)

//                    val intent = Intent(this, HomeScreen::class.java)
//                    intent.putExtra("currentUser", currUser)
//                    startActivity(intent)
                    loginNewUser(email, password)

                } else {
                    //sign up failed
                    Toast.makeText(this, "Failed to create new user.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.d("SignUpFailed", "Failed to create user: ${it.message}")
            }
    }

    private fun loginNewUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("SignInResult", "Successfully sign in for user with uid: ${it.result?.user?.uid}")
                    signUp_name_et.text!!.clear()
                    signUp_email_et.text!!.clear()
                    signUp_password_et.text!!.clear()

                    val intent = Intent(this, HomeScreen::class.java)
                    intent.putExtra("currUid", it.result!!.user.uid)
                    startActivity(intent)

                } else {
                    //Toast.makeText(this, "Failed to sign In", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {
                Log.d("SignInFailed", "Failed to create user: ${it.message}")
            }
    }

    private fun validateForm() : Boolean {
        if (signUp_name_et.text.toString().isEmpty()) {
            signUp_name_til.error = "Name is a required field."
            return false
        }

        if (signUp_email_et.text.toString().isEmpty()){
            signUp_email_til.error = "Email is a required field."
            return false
        }

        if (!(signUp_email_et.text.toString().contains("@"))) {
            signUp_email_til.error = "Ensure to enter a valid email account"
            return false
        }

        if (signUp_password_et.text.toString().isEmpty()) {
            signUp_password.error = "Password is a required field."
            return false
        }

        if (signUp_password_et.text.toString().length < 6){
            signUp_password.error = "Password length must be greater than 6."
            return false
        }

        return true
    }

    private fun saveUserToDB(uid : String, name : String, email : String) : User {
        val user = User(name, email, emptyList())
        database.child("users").child(uid).setValue(user)
        return user
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
