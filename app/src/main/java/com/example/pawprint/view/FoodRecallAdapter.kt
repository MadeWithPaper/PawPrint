package com.example.pawprint.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pawprint.R
import com.example.pawprint.model.RecallData
import android.content.Intent
import android.net.Uri
import kotlinx.android.synthetic.main.food_recall_cell.view.*


class FoodRecallAdapter (private val context: Context, private val recalls : List<RecallData>) : RecyclerView.Adapter<RecallViewHolder> () {

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
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(recall.link))
            context.startActivity(browserIntent)
        }
    }
}

class RecallViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindRecall(recall: RecallData, context: Context) {
        view.recallProduct_TV.text = "Product: ${recall.productDesc}"
        view.recallBrand_TV.text = "Brand: ${recall.brandName}"
        view.recallProblem_TV.text = "Problem: ${recall.reason}"
        view.recallDate_TV.text = recall.date
    }
}