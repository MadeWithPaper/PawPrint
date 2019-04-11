package com.mwp.pawprint.view

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mwp.pawprint.model.DogPoster
import com.mwp.pawprint.model.User
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_lost_dog_post.*
import android.provider.MediaStore
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import java.io.IOException
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.mwp.pawprint.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream


class NewLostDogPost : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var loc : Location
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private val fileName: String = "output.png"
    private var filePath: Uri? = null
    private val TAG = "NewLostDogPost"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_lost_dog_post)

       // val IMAGE_UPLOADING_PERMISSION = 3
       // ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_UPLOADING_PERMISSION)

        val currUser = intent.extras?.getSerializable("currUser") as User
        loc = intent.extras!!.get("loc") as Location
        val currUid = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().reference

        lostDog_postButton.setOnClickListener {
            postToDB(currUid)
        }

        lostDog_selectImage.setOnClickListener {
            selectImageInAlbum()
        }

        new_lost_dog_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
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

    private fun selectImageInAlbum() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE)
    }//TODO check for empty image

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

        uploadImage(key)
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

        val user = FirebaseAuth.getInstance().currentUser

        val newPoster = DogPoster("Not Set", dogName, lastSeen, contactNumber, details, loc.latitude, loc.longitude, user!!.uid, "not set")

        Log.i(TAG, "made new lost dog poster" + newPoster)
        return newPoster
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                Picasso.with(this@NewLostDogPost).load(filePath).into(lostDog_pic)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(id: String) {
        val storageRef =  FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("DogPosterPic/$id.jpg")
        val bitmap = (lostDog_pic.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val urlTask = imageRef.putBytes(data).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation imageRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d(TAG, "pic upload complete $downloadUri")
                updatePostPic(downloadUri.toString(), id)
            } else {
                // Handle failures
                Log.e(TAG, "upload pic failed")
            }
        }
    }

    private fun updatePostPic(url : String, key : String) {
        database.child("LostDogs").child(key).child("picURL").setValue(url)
        Log.i(TAG, "pic updated for $key to $url")
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}