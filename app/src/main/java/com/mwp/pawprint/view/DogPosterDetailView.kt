package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*

class DogPosterDetailView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_poster_detail_view)

        val post = intent.extras?.getSerializable("dogPoster") as DogPoster

        dogPosterDetailToolbarTV.text = post.name
        dogPosterDetail_NumberTV.text = "Contact Number: ${post.contactNumber}"
        dogPosterDetail_DescRV.text = post.details
        dogPoster_lastSeentv.text = "Last Seen at: ${post.lastSeen}"

        if (post.picURL != "not set"){
            Picasso.with(this@DogPosterDetailView).load(post.picURL).fit().into(dogPosterDetail_pic)
        } else
        {
            Log.d("DogPosterDetailView", "pic empty or not set ${post.picURL}")
        }
    }

}
