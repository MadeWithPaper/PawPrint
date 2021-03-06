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
import kotlinx.android.synthetic.main.activity_new_lost_dog_post.*
import android.provider.MediaStore
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import java.io.IOException
import android.net.Uri
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.mwp.pawprint.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dog_poster_detail_view.*
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI


class NewLostDogPost : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var loc : Location
    private val GALLERY_REQUEST_CODE = 13
    private val TAG = "NewLostDogPost"
    private var initIvList = mutableListOf<ImageView>()
    private var uriSet = mutableSetOf<String>()
    private val uriList = mutableListOf<Uri>()
    private var picUrls = mutableListOf<String>()
    private val cropHeight = 700
    private val cropWidth = 800
    private val SCALE_FACTOR = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_lost_dog_post)

        val currUser = intent.extras?.getSerializable("currUser") as User
        loc = intent.extras!!.get("loc") as Location
        val currUid = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().reference
        setDefaultImages()

        addPostFloatingButton.setOnClickListener {
            Log.d(TAG, "clicked")
            checkPostImage(currUid)
        }

        openGalleryFloatingActionButton.setOnClickListener {
            selectImageInAlbum()
        }

        new_lost_dog_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })

//        val phoneNumberFormattingTextWatcher = PhoneNumberFormattingTextWatcher()
//        phoneNumberFormattingTextWatcher.onTextChanged("-", 1, 5, 3)

        lostDog_contact_et.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        lostDog_back.setOnClickListener {
            this.onBackPressed()
        }
    }

    private var mCompletionListener : GeoFire.CompletionListener =
        GeoFire.CompletionListener { key, error ->
            if (error != null) {
                //Toast.makeText(this@NewLostDogPost, "geo fire upload error" + error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "geo fire upload error $error")
            } else {
                //Toast.makeText(this@NewLostDogPost, "geo fire upload success", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "geo fire upload success")
            }
        }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_PICK
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select One or More Picture"), GALLERY_REQUEST_CODE)
    }

    private fun checkPostImage(currUid : String) {

        if(!validateForm()){
            //invalid inputs detected
            newPostProgressBar.visibility = View.INVISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            return
        }

        if (initIvList.size != 0){
            //image not set
            Log.d(TAG, "dog post image not set")
            val builder = AlertDialog.Builder(this@NewLostDogPost)
            builder.setTitle("Warning!")
            builder.setIcon(R.drawable.warning)
            builder.setMessage("Do you want to proceed without attaching a picture for this post?")
            builder.setPositiveButton("YES"){_,_ ->
                newPostProgressBar.visibility = View.VISIBLE
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                postToDB(currUid, true)
            }

            builder.setNegativeButton("NO"){_,_ ->

            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)

        }else{
            //image set
            newPostProgressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            postToDB(currUid)
        }
    }

    private fun postToDB(currUid : String, notSet : Boolean = false) {
        val newPost = makeNewDogPoster()
        val key = database.push().key!!
        newPost.postID = key
        database.child("LostDogs").child(key).setValue(newPost)

        val posterLocation = FirebaseDatabase.getInstance().getReference("GeoFireDog")
        val geoFire = GeoFire(posterLocation)
        geoFire.setLocation(key, GeoLocation(loc.latitude, loc.longitude), mCompletionListener)

        if(notSet){
            val intent = Intent(this, HomeScreen::class.java)
            intent.putExtra("currUid", currUid)
            startActivity(intent)
            finish()
        }

        for(i in 0 until uriList.size){
            uploadImage(uriList[i], key, i, currUid)
        }
    }

    private fun validateForm() : Boolean {
        if (lostDog_contact_et.text.toString().isEmpty()) {
            lostDog_contact.error = "Please enter a contact number."
            return false
        }

        return true
    }

    private fun makeNewDogPoster() : DogPoster{
        var dogName = lostDog_name_et.text.toString()
        var contactNumber = lostDog_contact_et.text.toString()
        val lastSeen = lostDog_lastSeen_et.text.toString()
        var details = lostDog_desc_et.text.toString()

        val user = FirebaseAuth.getInstance().currentUser

        if (dogName == "") {
            dogName = "Lost Dog"
        }

        if (details.isEmpty()) {
            details = "No additional information provided."
        }

        val newPoster = DogPoster("Not Set", dogName, lastSeen, contactNumber, details, loc.latitude, loc.longitude, user!!.uid, listOfNotNull())

        Log.i(TAG, "made new lost dog poster $newPoster")
        return newPoster
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.clipData != null) {
            //remove place holder iv
            if (initIvList.size > 0){
                imageGallery.removeAllViews()
                initIvList.clear()
            }
            try {
                val imageCount = data.clipData.itemCount
                Log.i(TAG, "item count from Gallery request $imageCount")
                for (i in 0 until imageCount){
                    val uri = data.clipData.getItemAt(i).uri
                    Log.i(TAG, "uri at index $i is ${uri}")
                    val uriSplit = uri.toString().splitToSequence("/").toList()
                    Log.i(TAG, "uri split ${uriSplit[5]}")

                    if (!uriSet.contains(uriSplit[5])){
                        Log.d(TAG, "not in set adding $uri")
                        uriList.add(uri)
                    }
                    uriSet.add(uriSplit[5])
                }

                uriList.forEach {
                    imageGallery.addView(getImageView(it))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (resultCode == Activity.RESULT_CANCELED){
            Log.e(TAG, "$requestCode result canceled")
        } else if (data == null) {
            Log.e(TAG, "data from gallery request is null")
        } else if (data.clipData == null){
            Log.e(TAG, "clipData from gallery request is null")
        }
    }

    private fun getImageView(uri: Uri) : View{
        val iv = ImageView(applicationContext)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, cropHeight)
        lp.gravity = Gravity.CENTER
        iv.layoutParams = lp
        Picasso
            .with(this)
            .load(uri)
            .resize(cropWidth, cropHeight)
            .centerInside()
            .into(iv)
        return iv
    }

    private fun setDefaultImages(){
        val defaultDogIV = ImageView(applicationContext)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.gravity = Gravity.CENTER
        defaultDogIV.layoutParams = lp
        defaultDogIV.setImageResource(R.drawable.default_dog)
        initIvList.add(defaultDogIV)
        imageGallery.addView(defaultDogIV)
    }

    private fun uploadImage(uri: Uri, id: String, index: Int, currUid: String) {
        val storageRef =  FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("DogPosterPic/$id/$id$index.jpeg")
        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, SCALE_FACTOR, baos)
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
                updatePostPic(downloadUri.toString(), id, currUid)
            } else {
                // Handle failures
                Log.e(TAG, "upload pic failed")
            }
        }
    }

    private fun updatePostPic(url : String, key : String, currUid: String) {
        Log.i(TAG, "adding pic url: $url for $key")
        picUrls.add(url)
        Log.i(TAG, "picurl size: ${picUrls.size}")

        if (picUrls.size == uriList.size){
            FirebaseDatabase.getInstance().reference.child(resources.getString(R.string.dog_poster_firebase_path)).child(key).child("picURLs").setValue(picUrls)
            val intent = Intent(this, HomeScreen::class.java)
            intent.putExtra("currUid", currUid)
            startActivity(intent)
            finish()
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}