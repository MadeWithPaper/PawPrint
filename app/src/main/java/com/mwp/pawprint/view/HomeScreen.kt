package com.mwp.pawprint.view

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import android.widget.Toast
import android.os.Looper
import android.content.pm.PackageManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mwp.pawprint.model.User
import com.mwp.pawprint.R
import com.mwp.pawprint.model.CustomCallBack
import com.mwp.pawprint.model.DogPoster
import com.firebase.geofire.*
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.homescreen_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class HomeScreen : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private var mLocationManager: LocationManager? = null
    val ZOOM_LEVEL = 18f
    private lateinit var mMap: GoogleMap
    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null
    private val SEARCH_RADIUS = 0.0965
    private lateinit var currUser : User
    private var nearByList : MutableList<DogPoster> = mutableListOf()
    private var markers : HashMap<String, Marker> = HashMap()
    private var nearByMap : HashMap<String, DogPoster> = HashMap()
    private var nearByMarkerMap :  HashMap<String, DogPoster> = HashMap()
    private lateinit var currUid: String
    private val TAG = "HomeScreen"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        setSupportActionBar(homScreen_toolbar)
        currUid = intent.getStringExtra("currUid")
        //Maps and firebase
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.homeScreenMap) as SupportMapFragment
//        database = FirebaseDatabase.getInstance().reference
        mapFragment.getMapAsync(this)
        mLocationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //Nav menu
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        loadUser(currUid)

        homeScreen_addPoster.setOnClickListener {
            navToAddNewPost()
        }

        homeScreeen_MenuButton.setOnClickListener {
            drawer_layout.openDrawer(Gravity.LEFT)
        }

        homeScreen_RV.layoutManager = LinearLayoutManager(this)
        homeScreen_RV.adapter = DogPostAdapter(this, emptyList())
        homeScreen_RV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun loadUser(currUid : String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.child(currUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currUser = dataSnapshot.getValue(User::class.java)!!
                Log.i("$TAG Login", "got user " + currUser)
                header_name.text = currUser.name
                header_email.text = currUser.email
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("$TAG Login", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun navToAddNewPost() {
        val intent = Intent(this, NewLostDogPost::class.java)
        intent.putExtra("currUser", currUser)
        intent.putExtra("loc", mLastLocation)
        startActivity(intent)
    }

    public override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //val mCircle : Circle? = null
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMarkerClickListener(mMapClickListener)

        mLocationRequest = LocationRequest()
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocationRequest))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL))
        mLocationRequest?.setInterval(1000)
        mLocationRequest?.setFastestInterval(1000)
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.setMyLocationEnabled(true)
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mMap.setMyLocationEnabled(true)
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (applicationContext != null) {
                    mLastLocation = location
                    val latLng = LatLng(location.latitude, location.longitude)
                    setSearchCircle(latLng)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL))
                    getNearBy(latLng)
                }
            }
        }
    }

    private fun setSearchCircle(loc : LatLng) {
        if (mMap != null) {
            mMap.addCircle(CircleOptions()
                    .center(loc)
                    .radius(SEARCH_RADIUS)
                    .fillColor(Color.RED)
                    .strokeColor(Color.RED)
                    .strokeWidth(2f))
        }
    }

    var mCompletionListener : GeoFire.CompletionListener = object : GeoFire.CompletionListener {
        override fun onComplete(key: String?, error: DatabaseError?) {
            if (error != null) {
                Toast.makeText(this@HomeScreen, "geo fire upload error" + error, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@HomeScreen, "geo fire upload success", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDogByPostID (postKey : String, callback: CustomCallBack) {
        val dbRef = FirebaseDatabase.getInstance().getReference("LostDogs")
        dbRef.child(postKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currDog = dataSnapshot.getValue(DogPoster::class.java)!!
                Log.i(TAG, "Found $currDog")
                callback.onCallBack(currDog)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getNearBy(currLatLng : LatLng) {
        val posterLocation = FirebaseDatabase.getInstance().getReference("GeoFireDog")

        val geoFire = GeoFire(posterLocation)
        //geoFire.setLocation("dog by apartment", GeoLocation(35.292985, -120.675861), mCompletionListener)
        val geoQuery = geoFire.queryAtLocation(GeoLocation(currLatLng.latitude, currLatLng.longitude), SEARCH_RADIUS)
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryError(error: DatabaseError?) {
            }

            override fun onGeoQueryReady() {
                //Toast.makeText(this@HomeScreen, "geo fire found", Toast.LENGTH_SHORT).show()
                //TODO remove markers once user leaves area, use hashmap to store location near, each cycle check map to remove
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                getDogByPostID(key!!, object : CustomCallBack {
                    override fun onCallBack(value: Any) {
                        val d = value as DogPoster
                        val marker = mMap.addMarker(MarkerOptions()
                            .position(LatLng(d.lat, d.lon))
                            .title(d.name))
                        marker.showInfoWindow()
                        addToHistory(currUid, d)
                        nearByMap[key] = d
                        nearByMarkerMap[marker.id] = d
                        markers[key] = marker
                        Log.d(TAG, "map " + nearByMap.values)
                        Log.d(TAG, "markers" + markers.values)
                        nearByList.clear()
                        nearByList.addAll(nearByMap.values)
                        homeScreen_RV.adapter = DogPostAdapter(this@HomeScreen, nearByList)
                        homeScreen_RV.adapter!!.notifyDataSetChanged()
                        Log.d(TAG, "list" + nearByList.toString())
                    }
                })

            }

            override fun onKeyExited(key: String?) {
                //Toast.makeText(this@HomeScreen, "Leaving " + key, Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Leaving $key")
                markers.remove(key)
                nearByMap.remove(key)
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
               // Toast.makeText(this@HomeScreen, "found in move" + key, Toast.LENGTH_SHORT).show()
                Log.i(TAG, "key moved $key")

            }
        })
    }

    val mMapClickListener : GoogleMap.OnMarkerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker?): Boolean {
            //marker!!.hideInfoWindow()
            val d = nearByMarkerMap[marker!!.id]
            val intent = Intent(this@HomeScreen, DogPosterDetailView::class.java)
            intent.putExtra("dogPoster", d)
            startActivity(intent)
            return false
        }
    }

    private fun addToHistory(currUid: String, post : DogPoster) {
        val oldHistory = currUser.historyList
        if (oldHistory.contains(post.postID)) {
            //not new
        } else {
            //saw new poster add to list and update data base
            val newHistory = oldHistory.plus(post.postID)
            val dbRef = FirebaseDatabase.getInstance().getReference("users")
            dbRef.child(currUid).child("historyList").setValue(newHistory)
        }
    }
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@HomeScreen,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    })
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap.setMyLocationEnabled(true)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    //nav menu
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_profile -> {
                // Handle the camera action
                Log.i(TAG, "nav item works")
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("currUser", currUser)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }

            R.id.nav_history -> {
                val intent = Intent(this, History::class.java)
                intent.putExtra("currUser", currUser)
                startActivity(intent)
            }

            R.id.nav_foodRecall -> {
                val intent = Intent(this, FoodRecall::class.java)
                //intent.putExtra("currUser", currUser)
                startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }
}