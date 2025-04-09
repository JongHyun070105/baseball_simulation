package com.example.baseball_simulation_app.ui.main

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.baseball_simulation_app.R

// ImageView용 확장 함수
fun ImageView.loadImage(url: String?) {
    val imageUrl = url.takeIf { !it.isNullOrEmpty() } ?: R.drawable.placeholder_logo
    Glide.with(this.context)
        .load(imageUrl)
        .apply(RequestOptions().centerCrop())
        .into(this)
}
