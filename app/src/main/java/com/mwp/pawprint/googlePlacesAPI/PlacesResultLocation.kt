package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class PlacesResultLocation {
    @SerializedName("lat")
    @Expose
    var lat: Double = 0.toDouble()
    @SerializedName("lng")
    @Expose
    var lng: Double = 0.toDouble()
}