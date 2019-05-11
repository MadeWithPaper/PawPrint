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

        setSupportActionBar(recallAppBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

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

                val filteredRecallList = products.filter {
                    it.description!!.contains("dog") ||
                    it.brand!!.contains("dog") ||
                    it.company!!.contains("dog") ||
                    it.description!!.contains("Dog") ||
                    it.brand!!.contains("Dog") ||
                    it.company!!.contains("Dog")
                }
                val filteredFormattedRecallList : MutableList<RecallData> = mutableListOf()

                filteredRecallList.forEach{
                    filteredFormattedRecallList.add(fixFormat(it))
                }

                runOnUiThread {
                    foodRecall_RV.adapter = FoodRecallAdapter(applicationContext, filteredFormattedRecallList.reversed())
                }
            }
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("ERROR", "Failed to execute GET request to $DATA_URL")
            }
        })
    }

    //utility function used to strip away unnecessary character
    private fun fixFormat(unformatted : RecallData) : RecallData {
        //format brand name
        val unformattedList = unformatted.brand!!.split(">")
        val brandElement = unformattedList.elementAt(unformattedList.size-2)
        var formattedBrand = brandElement.split("<").first().trim()
        formattedBrand = replace(formattedBrand)
        val formattedDescription = replace(unformatted.description!!)
        val formattedCompany = replace(unformatted.company!!)
        val formattedReason = replace(unformatted.recallReason!!)

        return RecallData(unformatted.path, unformatted.date, formattedBrand, formattedDescription, formattedReason, formattedCompany)
    }

    private fun replace(unformatted: String ) : String {
        if (unformatted.contains("&#039;")){
            return unformatted.replace("&#039;", "'", true)
        } else if (unformatted.contains("&amp;")){
            return unformatted.replace("&amp;", "&", true)
        }
        return unformatted
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}