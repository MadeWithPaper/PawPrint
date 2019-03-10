package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mwp.pawprint.R
import com.mwp.pawprint.model.CustomCallBack
import com.mwp.pawprint.model.DogPoster
import com.mwp.pawprint.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_history.*

class History : AppCompatActivity() {

    private var historyList : MutableList<DogPoster> = mutableListOf()
    //TODO not sync up bug
    //Possible fix constantly update histroy list on firebase based on near by instead of querying when entering history page
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val currUser = intent.extras?.getSerializable("currUser") as User
        fetchHistory(currUser)

        historyRV.layoutManager = LinearLayoutManager(this@History)
        historyRV.adapter = HistoryEntryAdapter(this@History, emptyList())
        historyRV.addItemDecoration(DividerItemDecoration(this@History, DividerItemDecoration.VERTICAL))

        //updateHistoryUI(currUser)
    }

    private fun fetchHistory(user : User) {
        for (postID in user.historyList) {
            Log.d("History", "PostID in History : $postID")
            getDogByPostID(postID, object : CustomCallBack {
                override fun onCallBack(value: Any) {
                        historyList.add(value as DogPoster)
                        historyRV.adapter = HistoryEntryAdapter(this@History, historyList)
                        historyRV.adapter!!.notifyDataSetChanged()
                    }

            })
        }
    }

    private fun getDogByPostID (postKey : String, callback: CustomCallBack) {
        val dbRef = FirebaseDatabase.getInstance().getReference("LostDogs")
        dbRef.child(postKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currDog = dataSnapshot.getValue(DogPoster::class.java)!!
                Log.i("HomeScreen", "Found " + currDog)
                callback.onCallBack(currDog)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("HomeScreen", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
}
