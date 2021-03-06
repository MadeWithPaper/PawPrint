package com.mwp.pawprint.view

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mwp.pawprint.R
import com.mwp.pawprint.model.DogPoster
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dog_post_cell.view.*


class DogPostAdapter (private val context: Context, private val posters : List<DogPoster>) : RecyclerView.Adapter<PosterViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.dog_post_cell, parent, false)
        return PosterViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return posters.count()
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
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

class PosterViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindPost(post: DogPoster, context: Context) {
        view.postCell_name.text = post.name
        if (post.picURLs.isNotEmpty()){
            Picasso.with(context).load(post.picURLs.first()).fit().into(view.postCell_Pic)
        } else
        {
            view.postCell_Pic.setImageResource(R.drawable.default_dog)
            Log.d("DogPostAdapter", "pic empty or not set")
        }
    }
}


