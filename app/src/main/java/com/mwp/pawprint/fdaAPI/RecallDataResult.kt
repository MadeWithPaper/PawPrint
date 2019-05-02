package com.mwp.pawprint.fdaAPI

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecallDataResult {

    @SerializedName("treated_for_ae")
    @Expose
    var treatedForAe: String? = null
//    @SerializedName("reaction")
//    @Expose
//    var reaction: List<Reaction>? = null
//    @SerializedName("receiver")
//    @Expose
//    var receiver: Receiver? = null
    @SerializedName("unique_aer_id_number")
    @Expose
    var uniqueAerIdNumber: String? = null
    @SerializedName("original_receive_date")
    @Expose
    var originalReceiveDate: String? = null
    @SerializedName("number_of_animals_affected")
    @Expose
    var numberOfAnimalsAffected: String? = null
    @SerializedName("primary_reporter")
    @Expose
    var primaryReporter: String? = null
    @SerializedName("number_of_animals_treated")
    @Expose
    var numberOfAnimalsTreated: String? = null
//    @SerializedName("drug")
//    @Expose
//    var drug: List<Drug>? = null
//    @SerializedName("duration")
//    @Expose
//    var duration: Duration? = null
    @SerializedName("onset_date")
    @Expose
    var onsetDate: String? = null
//    @SerializedName("health_assessment_prior_to_exposure")
//    @Expose
//    var healthAssessmentPriorToExposure: HealthAssessmentPriorToExposure? = null
    @SerializedName("report_id")
    @Expose
    var reportId: String? = null
//    @SerializedName("animal")
//    @Expose
//    var animal: Animal? = null
    @SerializedName("serious_ae")
    @Expose
    var seriousAe: String? = null
    @SerializedName("type_of_information")
    @Expose
    var typeOfInformation: String? = null
//    @SerializedName("outcome")
//    @Expose
//    var outcome: List<Outcome>? = null

}