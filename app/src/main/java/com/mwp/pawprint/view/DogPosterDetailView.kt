package com.mwp.pawprint.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
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
        if (post.contactNumber == 0) {
            dogPosterDetail_NumberTV.visibility = View.INVISIBLE
        } else {
            dogPosterDetail_NumberTV.text = "Contact Number: ${post.contactNumber}"
        }
        dogPosterDetail_DescTV.text = post.details

        if (post.lastSeen == "") {
            dogPoster_lastSeentv.visibility = View.INVISIBLE
        } else {
            dogPoster_lastSeentv.text = "Last Seen at: ${post.lastSeen}"
        }
        dogPosterDetail_DescTV.movementMethod = ScrollingMovementMethod()

        if (post.picURL != "not set"){
            Picasso.with(this@DogPosterDetailView).load(post.picURL).fit().into(dogPosterDetail_pic)
        } else {
            Log.d("DogPosterDetailView", "pic empty or not set ${post.picURL}")
        }

        dogPosterDetail_found.setOnClickListener {
            val builder = AlertDialog.Builder(this@DogPosterDetailView)
            builder.setTitle("Remove Post")
            builder.setMessage("Are you sure you want to remove this lost dog poster?")
            builder.setIcon(R.drawable.warning)
            builder.setPositiveButton("YES"){_,_ ->
                removePoster(post)
                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra("currUid", user!!.uid)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton("No"){_,_ ->
                //do nothing
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }

    private fun removePoster(poster : DogPoster) {
        //remove dog poster data
        FirebaseDatabase.getInstance().reference.child("LostDogs").child(poster.postID).removeValue()
        //remove geofire entry
        FirebaseDatabase.getInstance().reference.child("GeoFireDog").child(poster.postID).removeValue()
        //remove dog poster from history by id
        getHistoryListByID(poster.postID)
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

    private fun getHistoryListByID (postID : String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children){
                    Log.d(TAG, "user detail: $data")
                    val currUser = data.getValue(User::class.java)!!
                    val historyList = currUser.historyList as ArrayList<*>
                    if (historyList.contains(postID)){
                        //remove post from history
                        historyList.remove(postID)
                        FirebaseDatabase.getInstance().reference.child("users").child(data.key!!).child("historyList").setValue(historyList)
                    } else {
                        //do nothing
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
}
