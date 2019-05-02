package com.mwp.pawprint.fdaAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecallData {
    @SerializedName("meta")
    @Expose
    var meta: RecallDataMeta? = null
    @SerializedName("results")
    @Expose
    var results: List<RecallDataResult>? = null
}