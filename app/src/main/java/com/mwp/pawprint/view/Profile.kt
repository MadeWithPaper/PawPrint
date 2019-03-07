package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mwp.pawprint.model.User
import com.mwp.pawprint.R
import kotlinx.android.synthetic.main.activity_profile.*

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val currUser = intent.extras?.getSerializable("currUser") as User

        profile_toolbar_text.text = currUser.name
    }
}
