package com.mwp.pawprint.model

import com.google.android.gms.maps.model.Marker
import com.mwp.pawprint.googlePlacesAPI.PlacesResult

//singleton object for shared resources
object App {
    //set of all near by lost dog posters, key: post id, value: dog poster
    var nearByDogPoster : MutableMap<String, DogPoster> = mutableMapOf()
    //set of all near by places
    var nearByPlaces : MutableSet<PlacesResult> = mutableSetOf()
}