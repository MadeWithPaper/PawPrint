package com.mwp.pawprint.model

import java.io.Serializable

data class RecallData (
    var date : String,
    var brandName : String,
    var productDesc : String,
    var reason : String,
    var company : String,
    var link : String
    ) : Serializable {
    constructor() : this(date = "", brandName = "", productDesc = "", reason = "", company = "", link = "")
}