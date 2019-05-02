package com.mwp.pawprint.googlePlacesAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlacesResult {
    @SerializedName("geometry")
    @Expose
    var geometry: PlacesResultGeometry? = null
    @SerializedName("icon")
    @Expose
    var icon: String? = null
    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("opening_hours")
    @Expose
    var openingHours: PlacesResultOpeningHours? = null
    @SerializedName("photos")
    @Expose
    var photos: List<PlacesResultPhoto>? = null
    @SerializedName("place_id")
    @Expose
    var placeId: String? = null
    @SerializedName("scope")
    @Expose
    var scope: String? = null
    @SerializedName("alt_ids")
    @Expose
    var altIds: List<PlacesResultAltId>? = null
    @SerializedName("reference")
    @Expose
    var reference: String? = null
    @SerializedName("types")
    @Expose
    var types: List<String>? = null
    @SerializedName("vicinity")
    @Expose
    var vicinity: String? = null
}