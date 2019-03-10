package com.mwp.pawprint.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*

class DogPosterDetailView : AppCompatActivity() {

    private val TAG = "DogPosterDetailView"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_poster_detail_view)

        val post = intent.extras?.getSerializable("dogPoster") as DogPoster
        val user = FirebaseAuth.getInstance().currentUser
        dogPosterDetail_found.visibility = View.INVISIBLE

        if (user != null) {
            // User is signed in
            if (post.owner == user.uid) {
                dogPosterDetail_found.visibility = View.VISIBLE
            }
        } else {
            // No user is signed in
            Log.d(TAG, "user null, should never happen")
        }
        dogPosterDetailToolbarTV.text = post.name
        dogPosterDetail_NumberTV.text = "Contact Number: ${post.contactNumber}"
        dogPosterDetail_DescRV.text = post.details
        dogPoster_lastSeentv.text = "Last Seen at: ${post.lastSeen}"

        if (post.picURL != "not set"){
            Picasso.with(this@DogPosterDetailView).load(post.picURL).fit().into(dogPosterDetail_pic)
        } else {
            Log.d("DogPosterDetailView", "pic empty or not set ${post.picURL}")
        }

        dogPosterDetail_found.setOnClickListener {
            removePoster(post)
            val intent = Intent(this, HomeScreen::class.java)
            intent.putExtra("currUid", user!!.uid)
            startActivity(intent)
            finish()
        }
    }

    private fun removePoster(poster : DogPoster) {
        FirebaseDatabase.getInstance().reference.child("LostDogs").child(poster.postID).removeValue()
        FirebaseDatabase.getInstance().reference.child("GeoFireDog").child(poster.postID).removeValue()
        Log.i(TAG, "removing post ${poster.postID}")
    }
}
