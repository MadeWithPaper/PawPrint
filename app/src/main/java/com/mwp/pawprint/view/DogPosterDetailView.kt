package com.mwp.pawprint.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*
import com.google.firebase.storage.FirebaseStorage
import com.mwp.pawprint.model.CustomCallBack
import com.mwp.pawprint.model.User


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
        dogPosterDetail_DescTV.text = post.details
        dogPoster_lastSeentv.text = "Last Seen at: ${post.lastSeen}"
        dogPosterDetail_DescTV.movementMethod = ScrollingMovementMethod()

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
        //remove dog poster data
        FirebaseDatabase.getInstance().reference.child("LostDogs").child(poster.postID).removeValue()
        //remove geofire entry
        FirebaseDatabase.getInstance().reference.child("GeoFireDog").child(poster.postID).removeValue()
        //remove dog poster from history by id
        getHistoryListByID(poster.postID, object : CustomCallBack {
            override fun onCallBack(value: Any) {
                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("historyList").removeValue()
                val history = value as ArrayList<*>
                history.remove(poster.postID)
                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("historyList").setValue(history)
            }
        })
        //remove pic from storage
        val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(poster.picURL)
        photoRef.delete().addOnSuccessListener{
            // File deleted successfully
            Log.d(TAG, "onSuccess: deleted file")
        }.addOnFailureListener{
            // Uh-oh, an error occurred!
            Log.d(TAG, "onFailure: did not delete file")
        }
        Log.i(TAG, "removing post ${poster.postID}")
    }

    private fun getHistoryListByID (postID : String, callback: CustomCallBack) {
        val currUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.child(currUserID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currUser = dataSnapshot.getValue(User::class.java)!!
                val historyList = currUser.historyList
                callback.onCallBack(historyList)
                //Log.i(TAG, "Found $currDog")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
}
