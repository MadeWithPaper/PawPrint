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
import com.mwp.pawprint.R
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class NewLostDogPost : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var loc : Location
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private val fileName: String = "output.png"
    private var filePath: Uri? = null


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

        val newPoster = DogPoster("Not Set", dogName, lastSeen, contactNumber, details, loc.latitude, loc.longitude)

        Log.i("NewLostPoster", "made new lost dog poster" + newPoster)
        return newPoster
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                lostDog_pic.setImageBitmap(bitmap)
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

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("NewLostDogPost", "upload failed")
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("NewLostDogPost", "upload success: ${imageRef.downloadUrl}")
        }
    }
}