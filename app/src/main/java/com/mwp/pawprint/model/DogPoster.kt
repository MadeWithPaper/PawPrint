package com.mwp.pawprint.model

import java.io.Serializable

data class DogPoster (
    var postID : String,
    var name : String,
    var lastSeen : String,
    var contactNumber : String,
    var details : String,
    var lat : Double,
    var lon : Double,
    var owner : String,
    var picURLs : List<String>

) : Serializable {
    constructor() : this(postID = "Not Specify", name = "Not Specify", lastSeen = "Not Specify", contactNumber = "0000000", details = "Not Specify", lat = 0.0, lon = 0.0, owner = "no one", picURLs = listOfNotNull())

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "postID" to postID,
            "owner" to owner,
            "name" to name,
            "lastSeen" to lastSeen,
            "contactNumber" to contactNumber,
            "details" to details,
            "picURLs" to picURLs,
            "lat" to lat,
            "lon" to lon
        )
    }
}