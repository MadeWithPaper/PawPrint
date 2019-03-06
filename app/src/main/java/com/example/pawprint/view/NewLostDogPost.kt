package com.example.pawprint.view

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pawprint.model.DogPoster
import com.example.pawprint.model.User
import com.example.pawprint.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_lost_dog_post.*

class NewLostDogPost : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var loc : Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_lost_dog_post)

        val currUser = intent.extras?.getSerializable("currUser") as User
        loc = intent.extras!!.get("loc") as Location
        val currUid = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().reference

        lostDog_postButton.setOnClickListener {
            postToDB(currUid)
        }
    }

    var mCompletionListener : GeoFire.CompletionListener = object : GeoFire.CompletionListener {
        override fun onComplete(key: String?, error: DatabaseError?) {
            if (error != null) {
                Toast.makeText(this@NewLostDogPost, "geo fire upload error" + error, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@NewLostDogPost, "geo fire upload success", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun postToDB(currUid : String) {
        val newPost = makeNewDogPoster()
        val key = database.push().key!!
        newPost.postID = key
        database.child("LostDogs").child(key).setValue(newPost)

        val posterLocation = FirebaseDatabase.getInstance().getReference("GeoFireDog")
        val geoFire = GeoFire(posterLocation)
        geoFire.setLocation(key, GeoLocation(loc.latitude, loc.longitude), mCompletionListener)
//        val posterLocation = FirebaseDatabase.getInstance().getReference("GeoFireDog")
//        val geoFire = GeoFire(posterLocation)

        //geoFire.setLocation("dog by apartment", GeoLocation(35.292985, -120.675861), mCompletionListener)
        //post success back to home screen
        val intent = Intent(this, HomeScreen::class.java)
        //intent.putExtra("currUser", currUser)
        intent.putExtra("currUid", currUid)
        startActivity(intent)
        finish()
    }

    private fun makeNewDogPoster() : DogPoster{
        val dogName = lostDog_name.text.toString()
        val contactNumber = lostDog_contact.text.toString().toInt()
        val lastSeen = lostDog_lastSeen.text.toString()
        val details = lostDog_desc.text.toString()

        val newPoster = DogPoster("Not Set", dogName, lastSeen, contactNumber, details, loc.latitude, loc.longitude)

        Log.i("NewLostPoster", "made new lost dog poster" + newPoster)
        return newPoster
    }
}
