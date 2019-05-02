package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Places {
    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions: List<Any>? = null
    @SerializedName("results")
    @Expose
    var results: List<PlacesResult>? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
}