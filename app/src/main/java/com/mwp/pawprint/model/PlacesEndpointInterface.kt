package com.mwp.pawprint.model

import com.mwp.pawprint.R
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesEndpointInterface {

    @GET("/maps/api/place/nearbysearch/json")
    fun getData(@Query("location") location : String,
                @Query("radius") radius : Int,
                @Query("key") apiKey : String,
                @Query("type") type : String,
                @Query("opennow") open : Boolean
                ) : Observable<Places>
}


//fun createRetrofit(): Retrofit {
//    val contentType = MediaType.get("application/json")
//    return Retrofit.Builder()
//        .addConverterFactory(serializationConverterFactory(contentType, JSON))
//        .baseUrl(BASE_URL)
//        .client(provideOkhttpClient())
//        .build()
//}