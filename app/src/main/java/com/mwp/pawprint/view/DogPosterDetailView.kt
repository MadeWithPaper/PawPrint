package com.mwp.pawprint.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
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
import com.mwp.pawprint.model.User
import android.net.Uri
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_new_lost_dog_post.*

class DogPosterDetailView : AppCompatActivity() {

    private val TAG = "DogPosterDetailView"
    private val cropHeight = 700
    private val cropWidth = 800

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_poster_detail_view)

        val post = intent.extras?.getSerializable("dogPoster") as DogPoster
        val user = FirebaseAuth.getInstance().currentUser
        //dogPosterDetailFAB_found.visibility = View.INVISIBLE
        //dogPosterDetailFAB_found.isVisible = false

        if (user != null) {
            // User is signed in
            if (post.owner == user.uid) {
               // dogPosterDetailFAB_found.visibility = View.VISIBLE
                dogPosterDetail_foundFAB.isVisible = true
            }
        } else {
            // No user is signed in
            Log.d(TAG, "user null, should never happen")
        }
        dogPosterDetailNameTV.text = "Name: ${post.name}"
        dogPosterDetailNumberTV.text = "Contact: ${post.contactNumber}"
        dogPosterDetailDescTV.text = post.details

        if (post.lastSeen == "") {
            dogPosterDetailLocTV.visibility = View.INVISIBLE
        } else {
            dogPosterDetailLocTV.text = "Last seen at: ${post.lastSeen}"
        }
        dogPosterDetailDescTV.movementMethod = ScrollingMovementMethod()

        if (post.picURLs.isNotEmpty()){
           // Picasso.with(this@DogPosterDetailView).load(post.picURLs).fit().into(dogPosterDetail_pic)
            post.picURLs.forEach {
                detailImageGallery.addView(getImageView(it))
            }
        } else {
            //user did not provide pic, use default
            setDefaultImage()
            Log.d("DogPosterDetailView", "pic empty or not set")
        }

        dogPosterDetail_foundFAB.setOnClickListener {
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

        dogPosterDetail_phoneFAB.setOnClickListener{
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", post.contactNumber, null))
            startActivity(intent)
        }

        dogPosterDetail_messageFAB.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", post.contactNumber, null)))
        }

        dogPosterDetail_back.setOnClickListener {
            this.onBackPressed()
        }
    }

    private fun setDefaultImage(){
        val defaultDogIV = ImageView(applicationContext)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.gravity = Gravity.CENTER
        defaultDogIV.layoutParams = lp
        defaultDogIV.setImageResource(R.drawable.default_dog)
        detailImageGallery.addView(defaultDogIV)
    }

    private fun getImageView(url: String) : View{
        val iv = ImageView(applicationContext)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, cropHeight)
        lp.gravity = Gravity.CENTER
        iv.layoutParams = lp
        Picasso
            .with(this)
            .load(url)
            .resize(cropWidth, cropHeight)
            .centerInside()
            .into(iv)
        return iv
    }

    private fun removePoster(poster : DogPoster) {
        //remove dog poster from history by id
        getHistoryListByID(poster.postID)
        //remove dog poster data
        FirebaseDatabase.getInstance().reference.child(resources.getString(R.string.dog_poster_firebase_path)).child(poster.postID).removeValue()
        //remove geofire entry
        FirebaseDatabase.getInstance().reference.child(resources.getString(R.string.dog_poster_geofire_path)).child(poster.postID).removeValue()
        //remove pic from storage
        poster.picURLs.forEach{
            val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(it)
            photoRef.delete().addOnSuccessListener{
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file")
            }.addOnFailureListener{
                // Uh-oh, an error occurred!
                Log.d(TAG, "onFailure: did not delete file")
            }
            Log.i(TAG, "removing post ${poster.postID}")
        }
    }

    private fun getHistoryListByID (postID : String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children){
                    //Log.d(TAG, "user detail: $data")
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
