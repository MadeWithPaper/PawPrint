package com.mwp.pawprint.model

import com.google.android.gms.maps.model.Marker
import com.mwp.pawprint.googlePlacesAPI.PlacesResult

//singleton object for shared resources
object App {
    //set of all near by lost dog posters
    var nearByDogPoster : MutableMap<String, DogPoster> = mutableMapOf()
    //near by lost dog poster markers
    var nearByLostDogPostMarkers : MutableMap<String, Marker> = mutableMapOf()
    //set of all near by places
    var nearByPlaces : MutableSet<PlacesResult> = mutableSetOf()
}