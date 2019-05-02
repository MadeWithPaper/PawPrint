package com.mwp.pawprint.fdaAPI

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodRecallEndpointInterface {

    ///animalandveterinary/event.json?search=animal.species:%22Dog%22&limit=100
    @GET("/animalandveterinary/event.json")
    fun getDogFoodRecall(@Query("search") animalSpecies : String,
                         @Query("api_key") key : String,
                         @Query("limit") limit : Int
    ) : Observable<RecallData>
}