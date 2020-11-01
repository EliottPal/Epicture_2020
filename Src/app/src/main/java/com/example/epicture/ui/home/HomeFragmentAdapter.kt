package com.example.epicture.ui.home

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.MainActivity
import com.example.epicture.R
import com.example.epicture.ToastPrinter
import com.example.epicture.ui.browse.BrowseFragmentAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_card.view.*
import kotlinx.android.synthetic.main.image_card.view.imageCardPicture
import kotlinx.android.synthetic.main.image_card.view.imageCardStar
import kotlinx.android.synthetic.main.image_card.view.imageCardTitle
import kotlinx.android.synthetic.main.image_profile.view.*
import kotlinx.android.synthetic.main.profile_page.view.*
import okhttp3.*
import java.io.IOException


class HomeFragmentAdapter(var pictures : ArrayList<PictureInfos>) :
    RecyclerView.Adapter<HomeFragmentAdapter.ProfilePageViewHolder>() {

    var pictureList = pictures

    // Class inheriting from RecyclerView.Adapter : needed function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_profile, parent, false)
        return ProfilePageViewHolder(view)
    }

    // Class inheriting from RecyclerView.Adapter : needed function
    override fun getItemCount(): Int {
        return pictureList.size
    }

    // Favorite a post
    fun favoritePost(picture: PictureInfos, holder: ProfilePageViewHolder) {
        ToastPrinter().print("Can't add your own photos to favorites", MainActivity.appContext!!)
    }

    // called during list rendering, takes the view and the position of the current picture.
    // Will load the picture with Picasso to display it on screen
    override fun onBindViewHolder(holder: ProfilePageViewHolder, position: Int) {
        // Setting imageCard title parameter
        holder.view.imageCardTitle.text = pictureList[position].title
        holder.view.imageDescription.text = pictureList[position].description
        holder.view.imageCardViews.text = pictureList[position].views
        // Loading the picture in Picasso using the url -> into the view
        Picasso.get().load(pictureList[position].link).into(holder.view.imageCardPicture)

        // Listening to events when clicking on fav star on the picture
        // Fav an image or unfav an image
        holder.view.imageCardStar.setOnClickListener {
            if (!pictureList[position].isFav) {
                favoritePost(pictureList[position], holder)
                pictureList[position].isFav = true
            } else if (pictureList[position].isFav) {
                favoritePost(pictureList[position], holder)
                pictureList[position].isFav = false
            }
        }
    }

    class ProfilePageViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}
