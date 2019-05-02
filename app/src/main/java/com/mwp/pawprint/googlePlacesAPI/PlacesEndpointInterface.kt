package com.mwp.pawprint.googlePlacesAPI

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesEndpointInterface {

    @GET("/maps/api/place/nearbysearch/json")
    fun getNearBy(@Query("location") location : String,
                @Query("radius") radius : Int,
                @Query("key") apiKey : String,
                @Query("type") type : String,
                @Query("opennow") open : Boolean
                ) : Observable<Places>
}