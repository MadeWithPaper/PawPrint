package com.example.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pawprint.R
import com.example.pawprint.model.DogPoster
import com.example.pawprint.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*

class DogPosterDetailView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_poster_detail_view)

        val post = intent.extras?.getSerializable("dogPoster") as DogPoster

        dogPosterDetailToolbarTV.text = post.name
        dogPosterDetail_NumberTV.text = "Contact Number: ${post.contactNumber}"
        dogPosterDetail_DescRV.text = post.details
        dogPosterDetail_lastSeentv.text = "Last Seen at: ${post.lastSeen}"

        //TODO pic

    }

}
