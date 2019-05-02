package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class PlacesResultPhoto {
    @Expose
    var height: Long = 0
    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions: List<Any>? = null
    @SerializedName("photo_reference")
    @Expose
    var photoReference: String? = null
    @SerializedName("width")
    @Expose
    var width: Long = 0
}