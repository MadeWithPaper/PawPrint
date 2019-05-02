package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class PlacesResultAltId {
    @SerializedName("place_id")
    @Expose
    var placeId: String? = null
    @SerializedName("scope")
    @Expose
    var scope: String? = null
}