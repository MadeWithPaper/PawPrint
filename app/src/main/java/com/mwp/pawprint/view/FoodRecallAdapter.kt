package com.mwp.pawprint.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mwp.pawprint.R
import com.mwp.pawprint.fdaRecall.RecallData
import kotlinx.android.synthetic.main.food_recall_cell.view.*


class FoodRecallAdapter (private val context: Context, private val recalls : List<RecallData>) : RecyclerView.Adapter<RecallViewHolder> () {

    private val BASE_URL = "https://www.fda.gov"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecallViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.food_recall_cell, parent, false)
        return RecallViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return recalls.count()
    }

    override fun onBindViewHolder(holder: RecallViewHolder, position: Int) {
        val recall = recalls.get(position)
        holder.bindRecall(recall, context)

        holder.view.setOnClickListener {
            val url = BASE_URL + recall.path
            Log.i("FoodRecallAdapter.kt", url)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        }
    }
}

class RecallViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindRecall(recall: RecallData, context: Context) {
        view.recallDescription_TV.text = "Description: ${recall.description}"
        view.recallBrand_TV.text = "Brand: ${recall.brand}"
        if (recall.recallReason == ""){
            view.recallProblem_TV.text = "Problem: Not Stated"
        } else {
            view.recallProblem_TV.text = "Problem: ${recall.recallReason}"
        }
        view.recallDate_TV.text = recall.date
    }
}