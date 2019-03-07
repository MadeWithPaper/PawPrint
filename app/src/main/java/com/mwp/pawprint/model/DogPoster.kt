package com.mwp.pawprint.model

import java.io.Serializable

data class DogPoster (
    var postID : String,
    var name : String,
    var lastSeen : String,
    var contactNumber : Int,
    var details : String,
    var lat : Double,
    var lon : Double

) : Serializable {
    constructor() : this(postID = "Not Specify", name = "Not Specify", lastSeen = "Not Specify", contactNumber = "0000000".toInt(), details = "Not Specify", lat = 0.0, lon = 0.0)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "postID" to postID,
            "name" to name,
            "lastSeen" to lastSeen,
            "contactNumber" to contactNumber,
            "details" to details,
            "lat" to lat,
            "lon" to lon
        )
    }
}