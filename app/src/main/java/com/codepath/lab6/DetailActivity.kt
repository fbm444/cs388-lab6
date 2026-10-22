package com.codepath.lab6

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val detailImageView: ImageView = findViewById(R.id.detailImage)
        val detailTitleTextView: TextView = findViewById(R.id.detailTitle)
        val detailDescriptionTextView: TextView = findViewById(R.id.detailDescription)
        val detailLatLongTextView: TextView = findViewById(R.id.detailLatLong)

        // Retrieve data passed via Intent
        val park = IntentCompat.getSerializableExtra(intent, PARK_EXTRA, Park::class.java)
        val campground = IntentCompat.getSerializableExtra(intent, CAMPGROUND_EXTRA, Campground::class.java)

        // Populate UI based on type
        when {
            park != null -> {
                detailTitleTextView.text = park.fullName
                detailDescriptionTextView.text = park.description
                detailLatLongTextView.visibility = View.GONE
                Glide.with(this).load(park.imageUrl).into(detailImageView)
            }
            campground != null -> {
                detailTitleTextView.text = campground.name
                detailDescriptionTextView.text = campground.description
                detailLatLongTextView.text = campground.latLong
                Glide.with(this).load(campground.imageUrl).into(detailImageView)
            }
        }
    }
}
