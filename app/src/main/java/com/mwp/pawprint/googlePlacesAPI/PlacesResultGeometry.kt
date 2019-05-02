package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class PlacesResultGeometry {
    @SerializedName("location")
    @Expose
    var location: PlacesResultLocation? = null

}