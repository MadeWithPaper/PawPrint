package com.mwp.pawprint.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mwp.pawprint.R
import com.mwp.pawprint.fdaAPI.FoodRecallEndpointInterface
import com.mwp.pawprint.fdaAPI.RecallData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_food_recall.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class FoodRecall : AppCompatActivity() {

    //private var recallList : MutableList<RecallData> = mutableListOf()
    private val TAG = "FoodRecall.kt"
    private var compositeDisposable: CompositeDisposable? = null
    private val BASE_URL = "https://api.fda.gov/"
    //https://api.fda.gov/animalandveterinary/event.json?search=animal.species:"Dog"&limit=1 (99max)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_recall)

        compositeDisposable = CompositeDisposable()

        getFoodRecallData()

        foodRecall_RV.layoutManager = LinearLayoutManager(this)
        foodRecall_RV.adapter = FoodRecallAdapter(this, emptyList())
        foodRecall_RV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun getFoodRecallData(){
        val apiKey : String = resources.getString(R.string.fda_api_key)

        val requestInterface = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build().create(FoodRecallEndpointInterface::class.java)

        compositeDisposable?.add(requestInterface.getDogFoodRecall( """animal.species:"Dog"""", apiKey, 5)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::handleResponse))
    }

    private fun handleResponse(recallList : RecallData) {
        Log.i(TAG, "meta data ${recallList.meta}")

        recallList.results!!.forEach {
            Log.i(TAG, it.reportId)
        }
    }
}
