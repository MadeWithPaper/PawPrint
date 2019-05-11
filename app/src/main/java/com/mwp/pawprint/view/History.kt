package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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

    private var currHistoryList : MutableList<DogPoster> = mutableListOf()
    private val TAG = "History.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        fetchHistory()

        historyRV.layoutManager = LinearLayoutManager(this@History)
        historyRV.adapter = HistoryEntryAdapter(this@History, emptyList())
        historyRV.addItemDecoration(DividerItemDecoration(this@History, DividerItemDecoration.VERTICAL))

        setSupportActionBar(historyToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun fetchHistory() {
        getHistoryListByID(object : CustomCallBack {
            override fun onCallBack(value: Any) {
                val history = value as List<*>
                history.forEach {
                    getDogByPostID(it.toString(), object : CustomCallBack {
                        override fun onCallBack(value: Any) {
                            currHistoryList.add(value as DogPoster)
                            Log.d("History", "currlist $currHistoryList")
                            historyRV.adapter = HistoryEntryAdapter(this@History, currHistoryList)
                            historyRV.adapter!!.notifyDataSetChanged()
                        }
                    })
                }
            }
        })
    }

    private fun getHistoryListByID (callback: CustomCallBack) {
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
