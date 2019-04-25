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
import android.provider.SettingsSlicesContract.KEY_LOCATION
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mwp.pawprint.R
import com.firebase.geofire.*
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mwp.pawprint.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.homescreen_content.*
import kotlinx.android.synthetic.main.nav_header_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class HomeScreen : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private var mLocationManager: LocationManager? = null
    private val ZOOM_LEVEL = 17.5f
    private lateinit var mMap: GoogleMap
    private var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val SEARCH_RADIUS = 100.0
    private lateinit var currUser : User
    private var nearByList : MutableList<DogPoster> = mutableListOf()
    private var markers : HashMap<String, Marker> = HashMap()
    private var nearByMap : HashMap<String, DogPoster> = HashMap()
    private var nearByMarkerMap :  HashMap<String, DogPoster> = HashMap()
    private lateinit var currUid: String
    private val TAG = "HomeScreen"
    private val PET_STORE = "pet_store"
    private val VETERINARY_CARE = "veterinary_care"
    private val PARK = "park"
    private val BASE_URL = "https://maps.googleapis.com/"
    private var compositeDisposable: CompositeDisposable? = null
    private var lastKnownLoc : Location? = null
    private var initKnownLoc = true
    private var mapCircle : Circle? = null
    private var mapDot : Circle? = null

    //TODO optimize map for activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        setSupportActionBar(homScreen_toolbar)
        currUid = intent.getStringExtra("currUid")
        //Maps and firebase
        mLocationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.homeScreenMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        //recycler list view
        homeScreen_RV.layoutManager = LinearLayoutManager(this)
        homeScreen_RV.adapter = DogPostAdapter(this, emptyList())
        homeScreen_RV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //places api
        compositeDisposable = CompositeDisposable()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent) : Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            logOut()
        }
        return super.onKeyDown(keyCode, event);
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
        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@HomeScreen, R.raw.style_json))
        mMap.setOnMarkerClickListener(mMapClickListener)
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = 1000
        mLocationRequest?.fastestInterval = 1000
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = false
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mMap.isMyLocationEnabled = false
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
                if (applicationContext != null) {
                    mLastLocation = p0.lastLocation
                    if (initKnownLoc){
                        lastKnownLoc = mLastLocation
                    }
                    val latLng = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
                    setSearchCircle(latLng)

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL))
                    getNearBy(latLng)
                    getNearByPlaces()
                }
        }
    }

    private fun setSearchCircle(loc : LatLng) {
        if (mMap != null) {
            if(mapCircle != null){
                mapCircle!!.remove()
            }

            mapCircle = mMap.addCircle(CircleOptions()
                    .center(loc)
                    .radius(SEARCH_RADIUS)
                    .fillColor(447997695)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(3f))

            if(mapDot != null){
                mapDot!!.remove()
            }

            mapDot = mMap.addCircle(CircleOptions()
                .center(loc)
                .radius(5.0)
                .fillColor(Color.CYAN)
                .strokeColor(Color.WHITE)
                .strokeWidth(5f))
        }
    }

    var mCompletionListener : GeoFire.CompletionListener = object : GeoFire.CompletionListener {
        override fun onComplete(key: String?, error: DatabaseError?) {
            if (error != null) {
                //Toast.makeText(this@HomeScreen, "geo fire upload error" + error, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "GeoFire upload error $error")
            } else {
                //Toast.makeText(this@HomeScreen, "geo fire upload success", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "GeoFire upload success")
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
        val geoQuery = geoFire.queryAtLocation(GeoLocation(currLatLng.latitude, currLatLng.longitude), SEARCH_RADIUS/1000.0)
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryError(error: DatabaseError?) {
                Log.d(TAG, "geo query error on $currLatLng")
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
                            .title(d.name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.paw_icon)))

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
                        Log.d(TAG, "list $nearByList")
                    }
                })

            }

            override fun onKeyExited(key: String?) {
                //Toast.makeText(this@HomeScreen, "Leaving " + key, Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Leaving $key")
                markers.remove(key)
                nearByMap.remove(key)
                //mMap.clear()
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
            if (nearByMarkerMap.containsKey(marker!!.id)) {
                val d = nearByMarkerMap[marker!!.id]
                val intent = Intent(this@HomeScreen, DogPosterDetailView::class.java)
                intent.putExtra("dogPoster", d)
                startActivity(intent)
            } else {
                //near by place clicked on do nothing as of now
            }
            return false
        }
    }

    private fun addToHistory(currUid: String, post : DogPoster) {
        val currUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.child(currUserID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currUser = dataSnapshot.getValue(User::class.java)!!
                val oldHistoryList = currUser.historyList
                //callback.onCallBack(historyList)
                //Log.i(TAG, "Found $currDog")
                Log.d(TAG, "to add  ${post.postID}")
                if (oldHistoryList.contains(post.postID)) {
                    //not new
                } else {
                    //saw new poster add to list and update data base
                    val newHistory = oldHistoryList.plus(post.postID)
                    //Log.d(TAG, "oldlist $oldHistoryList")
                    //Log.d(TAG, "newlist $newHistory")
                    val dbRef = FirebaseDatabase.getInstance().getReference("users")
                    dbRef.child(currUid).child("historyList").setValue(newHistory)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getNearByPlaces(){
        val radius = 200 //search radius within 200 meters
        val apiKey : String = resources.getString(R.string.browser_key)

        if (queryUpdateByDistance()){
            //do update
            val requestInterface = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(PlacesEndpointInterface::class.java)

            //google places api call for parks
            compositeDisposable?.add(requestInterface.getData("${mLastLocation!!.latitude}, ${mLastLocation!!.longitude}", radius, apiKey, PARK, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse))

            //google places api call for vet
            compositeDisposable?.add(requestInterface.getData("${mLastLocation!!.latitude}, ${mLastLocation!!.longitude}", radius, apiKey, VETERINARY_CARE, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse))

            //google places api call for pet store
            compositeDisposable?.add(requestInterface.getData("${mLastLocation!!.latitude}, ${mLastLocation!!.longitude}", radius, apiKey, PET_STORE, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse))

        } else {
            //distance displaced not enough to trigger update
            //do nothing
        }
    }

    private fun handleResponse(placesList : Places) {
        Log.i(TAG, "html_attributions ${placesList.htmlAttributions}")
        placesList.results!!.forEach {
            val marker = mMap.addMarker(MarkerOptions()
                .position(LatLng(it.geometry!!.location!!.lat, it.geometry!!.location!!.lng))
                .title(it.name))

            when(it.types!![0]) {
                PET_STORE -> marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.store_icon))
                VETERINARY_CARE -> marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.vet_icon))
                PARK -> marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.park_icon))
            }
            marker.showInfoWindow()
            Log.i(TAG,"name: ${it.name} loc(lat, lng): ${it.geometry!!.location!!.lat}, ${it.geometry!!.location!!.lng}")
        }
        Log.i(TAG, "status ${placesList.status}")
    }

    private fun queryUpdateByDistance() : Boolean {
        val distanceDifference = 50
        val difference = mLastLocation!!.distanceTo(lastKnownLoc)

        if (difference >= distanceDifference || initKnownLoc){
            lastKnownLoc = mLastLocation
            initKnownLoc = false
            return true
        }
        return false
    }

    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                        ActivityCompat.requestPermissions(
                            this@HomeScreen,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    })
                    .create()
                    .show()
            } else {
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
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
        when (item.itemId) {
            R.id.nav_profile -> {
                Log.i(TAG, "nav item works")
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("currUser", currUser)
                startActivity(intent)
            }

            R.id.nav_foodRecall -> {
                val intent = Intent(this, FoodRecall::class.java)
                startActivity(intent)
            }

            R.id.nav_history -> {
                val intent = Intent(this, History::class.java)
                intent.putExtra("currUserID", currUid)
                startActivity(intent)
            }

            R.id.nav_logout -> {
                logOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun logOut(){
        val builder = AlertDialog.Builder(this@HomeScreen)
        builder.setTitle("Log out")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("YES"){_,_ ->
            val intent = Intent(this, Login::class.java)
            finishAffinity()
            startActivity(intent)
        }

        builder.setNegativeButton("No"){_,_ ->
            //do nothing
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}