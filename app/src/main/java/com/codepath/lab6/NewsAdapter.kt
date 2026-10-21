package com.codepath.lab6

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter(private val context: Context, private val news: List<NewsRelease>) :
    RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(news[position])
    }

    override fun getItemCount() = news.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val newsImageView: ImageView = itemView.findViewById(R.id.itemNewsImage)
        private val newsTitleTextView: TextView = itemView.findViewById(R.id.itemNewsTitle)
        private val newsParkTextView: TextView = itemView.findViewById(R.id.itemNewsPark)
        private val newsAbstractTextView: TextView = itemView.findViewById(R.id.itemNewsAbstract)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(release: NewsRelease) {
            newsTitleTextView.text = release.title
            newsParkTextView.text = release.parkCode?.uppercase()
            newsAbstractTextView.text = release.abstract

            // Not every news release has a photo
            newsImageView.visibility = if (release.imageUrl != null) View.VISIBLE else View.GONE
            Glide.with(context).load(release.imageUrl).into(newsImageView)
        }

        // Open the full news release on nps.gov
        override fun onClick(v: View?) {
            val url = news[absoluteAdapterPosition].url
            if (url.isNullOrEmpty()) return
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
