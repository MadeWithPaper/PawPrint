package com.mwp.pawprint.fdaRecall

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecallData (
    @SerializedName("path")
    @Expose
    var path: String? = null,
    @SerializedName("field_company_announcement_date")
    @Expose
    var date: String? = null,
    @SerializedName("field_brand_name")
    @Expose
    var brand: String? = null,
    @SerializedName("field_product_description")
    @Expose
    var description: String? = null,
    @SerializedName("field_recall_reason")
    @Expose
    var recallReason: String? = null,
    @SerializedName("field_company_name")
    @Expose
    var company: String? = null ) : Serializable


