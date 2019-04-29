package com.mwp.pawprint.view

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mwp.pawprint.R
import com.mwp.pawprint.model.RecallData
import kotlinx.android.synthetic.main.activity_food_recall.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import com.mwp.pawprint.model.AsyncResponse


class FoodRecall : AppCompatActivity() {

    private var recallList : MutableList<RecallData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_recall)

        foodRecall_RV.layoutManager = LinearLayoutManager(this)
        foodRecall_RV.adapter = FoodRecallAdapter(this, recallList)
        foodRecall_RV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        FetchData(object : AsyncResponse {
            override fun processFinish(output: Any) {
                recallList = output as MutableList<RecallData>
                Log.d("FoodRecall.kt", "post fetch $recallList")
                foodRecall_RV.adapter = FoodRecallAdapter(this@FoodRecall, recallList)
            }

        }).execute()
    }
}

class FetchData(asyncResponse : AsyncResponse) : AsyncTask<Void, Void, MutableList<RecallData>>() {
    private val mRecallList : MutableList<RecallData> = mutableListOf()
    private val TAG = "FetchData Async"
    private val dataURL = "https://www.fda.gov/animal-veterinary/safety-health/recalls-withdrawals"
    private val prefixURL = "https://www.fda.gov"
    var delegate: AsyncResponse = asyncResponse

    //private val contextRef = WeakReference<Context>(context)
    override fun onProgressUpdate(vararg values: Void?) {
        super.onProgressUpdate(*values)
        Log.d(TAG, "onProgressUpdate: ")
    }

    override fun onPostExecute(result: MutableList<RecallData>) {
        super.onPostExecute(result)
        Log.d(TAG, "onPostExecute: $mRecallList")
        delegate.processFinish(result)
        //TODO stop progress spinner and update ui
        //do stuff with ui data is done loading
    }

    override fun doInBackground(vararg params: Void?): MutableList<RecallData> {
        Log.d(TAG, "doInBackground: ")

        val doc : Document
        try {
            doc = Jsoup.connect(dataURL).get()
            Log.i(TAG, "$doc")
            val table = doc.select("table[class=table table-bordered table-striped footable toggle-square-filled tablesorter]").first()
            val rows = table.select("tr")
            for (row in rows) {
               // System.out.println("===")
                val cols = row.select("td")
                var ind = 0
                val newRecall = RecallData()
                for (col in cols) {
                    when (ind) {
                        0 -> newRecall.date = col.text()
                        1 -> {
                            val p = getLinkText(col)
                            newRecall.brandName = p.first
                            newRecall.link = prefixURL + p.second}
                        2 -> newRecall.productDesc = col.text()
                        3 -> newRecall.reason = col.text()
                        4 -> newRecall.company = col.text()
                        5 -> mRecallList.add(newRecall)
                    }
                    ind += 1
                }
            }
        } catch (e : Exception) {
            Log.e(TAG, e.toString())
        }

        return mRecallList
    }

    private fun getLinkText(v : Element) : Pair<String, String> {
        val link = v.select("a")
        //Log.d(TAG,"Company Name: ${link.text()} Link: ${link.attr("href")}")
        return Pair(link.text(), link.attr("href"))
    }

    override fun onCancelled(result: MutableList<RecallData>) {
        super.onCancelled(result)
        Log.d(TAG, "onCancelled: ")
    }

    override fun onCancelled() {
        super.onCancelled()
        Log.d(TAG, "onCancelled: ")
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(TAG, "onPreExecute: ")
        //TODO add progress spinner fpr loading data
    }

}
