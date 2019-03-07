package com.mwp.pawprint.view

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import kotlinx.android.synthetic.main.history_entry_cell.view.*

class HistoryEntryAdapter (private val context: Context, private val posters : List<DogPoster>) : RecyclerView.Adapter<HistoryViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.history_entry_cell, parent, false)
        return HistoryViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return posters.count()
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val post = posters.get(position)
        holder.bindPost(post, context)

        holder.view.setOnClickListener {
            val intent = Intent(context, DogPosterDetailView::class.java)
            intent.putExtra("dogPoster", post)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

class HistoryViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindPost(post: DogPoster, context: Context) {
        view.historyName.text = post.name
        view.historyLastSeen.text = post.lastSeen
    }
}