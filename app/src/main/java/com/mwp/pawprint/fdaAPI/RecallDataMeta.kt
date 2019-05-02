package com.mwp.pawprint.fdaAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecallDataMeta {

    @SerializedName("disclaimer")
    @Expose
    var disclaimer: String? = null
    @SerializedName("terms")
    @Expose
    var terms: String? = null
    @SerializedName("license")
    @Expose
    var license: String? = null
    @SerializedName("last_updated")
    @Expose
    var lastUpdated: String? = null
    @SerializedName("results")
    @Expose
    var results: RecallDataResult? = null

}