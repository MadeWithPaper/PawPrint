package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mwp.pawprint.model.User
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import kotlinx.android.synthetic.main.activity_profile.*

class Profile : AppCompatActivity() {

    private var postList : MutableList<DogPoster> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val currUser = intent.extras?.getSerializable("currUser") as User
        val currUid = intent.extras?.getString("currUserID")
        profile_email.text = currUser.email
        profile_name.text = currUser.name

        setSupportActionBar(profileToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        fetchPosterForOwner(currUid!!)
        //recycler list view
        profileRV.layoutManager = LinearLayoutManager(this)
        profileRV.adapter = DogPostAdapter(this, postList)
        profileRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun fetchPosterForOwner(uid: String) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("LostDogs")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapShot in dataSnapshot.children) {
                    val currDog = snapShot.getValue(DogPoster::class.java)!!
                    Log.i("Profile.kt", "currDog $currDog")
                    if (currDog.owner == uid){
                        postList.add(currDog)
                        Log.i("Profile.kt", "adding to list $currDog")
                        profileRV.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Profile.kt", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
