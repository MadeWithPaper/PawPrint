package com.mwp.pawprint.view

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.mwp.pawprint.R
import com.mwp.pawprint.fdaRecall.RecallData
import kotlinx.android.synthetic.main.activity_food_recall.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import com.mwp.pawprint.model.AsyncResponse
import kotlinx.io.IOException
import okhttp3.*


class FoodRecall : AppCompatActivity() {

    private var recallList : MutableList<RecallData> = mutableListOf()
    private val DATA_URL = "https://www.fda.gov/datatables-json/cvm-recalls-withdrawals-json"
    private val TAG = "FoodRecall.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_recall)

        fetchData()

        foodRecall_RV.layoutManager = LinearLayoutManager(this)
        foodRecall_RV.adapter = FoodRecallAdapter(this, recallList)
        foodRecall_RV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun fetchData(){
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(DATA_URL)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                println(body)
                val gson = GsonBuilder().create()
                val products : List<RecallData> = gson.fromJson(body,  object : TypeToken<List<RecallData>>() {}.type)
                products.forEach{
                    it.brand = formatBrandName(it.brand!!)
                    //Log.i(TAG, "$it")
                }
                runOnUiThread {
                    foodRecall_RV.adapter = FoodRecallAdapter(applicationContext, products)
                }
            }
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("ERROR", "Failed to execute GET request to $DATA_URL")
            }
        })
    }

    //utility function used to strip away unnecessary character
    private fun formatBrandName(unformatted : String) : String {
        val unformattedList = unformatted.split(">")
        val brandElement = unformattedList.elementAt(unformattedList.size-2)
        return brandElement.split("<").first().trim()
    }
}