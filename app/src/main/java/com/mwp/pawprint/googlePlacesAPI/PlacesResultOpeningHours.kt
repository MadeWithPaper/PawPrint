package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class PlacesResultOpeningHours {
    @SerializedName("open_now")
    @Expose
    var openNow: Boolean = false
}