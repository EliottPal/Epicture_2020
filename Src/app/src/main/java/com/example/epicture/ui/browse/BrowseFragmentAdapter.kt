package com.example.epicture.ui.browse

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.MainActivity
import com.example.epicture.R
import com.example.epicture.services.Setup
import com.example.epicture.ui.browse.PictureInfos
import com.example.epicture.ui.favorites.FavoritesFragmentAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_card.*
import kotlinx.android.synthetic.main.image_card.view.*
import okhttp3.*
import java.io.IOException

class BrowseFragmentAdapter(var pictures: ArrayList<PictureInfos>) :
    RecyclerView.Adapter<BrowseFragmentAdapter.BrowsePageViewHolder>() {

    var pictureList = pictures
    private val hostValue = "api.imgur.com"

    // Class inheriting from RecyclerView.Adapter : needed function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowsePageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_card, parent, false)
        return BrowsePageViewHolder(view)
    }

    // Class inheriting from RecyclerView.Adapter : needed function
    override fun getItemCount(): Int {
        return pictures.size
    }

    // Favorite an album
    fun favoriteAlbum(picture: PictureInfos, holder: BrowsePageViewHolder) {
        val client = OkHttpClient()
        println(MainActivity.accessToken)
        println(picture.id)
        val albumHash : String? = picture.id
        val requestBody = RequestBody.create(null, "")
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment("3")
                .addPathSegment("album")
                .addPathSegment(albumHash!!)
                .addPathSegment("favorite")
                .build()
        println(queryUrl.toString())
        var request = Request.Builder()
                .url(queryUrl)
                .post(requestBody)
                .header("Authorization", "Bearer ${MainActivity.accessToken}")
                .build()
        FormBody.Builder().build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println(response)
                println("Album added to favorites !")
                picture.isFav = !picture.isFav
                if (picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
                } else if (!picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_off)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to add album to favorites")
            }
        })
    }

    // Favorite a post
    fun favoritePost(picture: PictureInfos, holder: BrowsePageViewHolder) {
        println(picture.title)
        val client = OkHttpClient()
        var imageHash : String? = null
        if (picture.isAlbum)
            imageHash = picture.id
        else
            imageHash = picture.id
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment("3")
                .addPathSegment("image")
                .addPathSegment(imageHash!!)
                .addPathSegment("favorite")
                .build()
        val requestBody = RequestBody.create(null, "")
        var request = Request.Builder()
                .url(queryUrl)
                .post(requestBody)
                .header("Authorization", "Bearer ${MainActivity.accessToken}")
                .build()
        FormBody.Builder().build()
        // Make request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("ERROR: Failed to execute the request")
            }
            // Update votes count and display
            override fun onResponse(call: Call, response: Response) {
                println("Favored!")
                println(response)
                picture.isFav = !picture.isFav
                if (picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
                } else if (!picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_off)
                }
            }
        })
    }

    // Upvote request
    fun upvotePost(picture: PictureInfos, holder: BrowsePageViewHolder) {
        holder.view.imageCardIconUp.setColorFilter(Color.BLACK)
        val client = OkHttpClient()
        val galleryHash = picture.id
        var requestVote = "https://api.imgur.com/3/gallery/$galleryHash/vote/up"
        var request = Request.Builder()
                .url(requestVote)
                .header("Authorization", "Bearer ${MainActivity.accessToken}")
                .build()
        // Make request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("ERROR: Failed to execute the request")
            }
            // Update votes count and display
            override fun onResponse(call: Call, response: Response) {
                println("Upvoted!")
                println(response)
            }
        })
    }

    // Downvote request
    fun downvotePost(picture: PictureInfos, holder: BrowsePageViewHolder) {
        holder.view.imageCardIconDown.setColorFilter(Color.BLACK)
        val client = OkHttpClient()
        val galleryHash = picture.id
        var requestVote = "https://api.imgur.com/3/gallery/$galleryHash/vote/down"
        var request = Request.Builder()
                .url(requestVote)
                .addHeader("Authorization", "Bearer ${MainActivity.accessToken}")
                .addHeader("cache-control", "no-cache")
                .build()
        // Make request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("ERROR: Failed to execute the request")
            }
            // Update votes count and display
            override fun onResponse(call: Call, response: Response) {
                println("Downvoted!")
                println(response)
            }
        })
    }

    // called during list rendering, takes the view and the position of the current picture.
    // Will load the picture with Picasso to display it on screen
    override fun onBindViewHolder(holder: BrowsePageViewHolder, position: Int) {
        // Setting every imageCard parameter to be displayed
        holder.view.imageCardTitle.text = pictureList[position].title
        holder.view.imageCardDownvotes.text = pictureList[position].downs
        holder.view.imageCardUpvotes.text = pictureList[position].ups
        holder.view.imageCardComments.text = pictureList[position].commentNumber
        // Loading the picture in Picasso using the url -> into the view
        Picasso.get().load("https://i.imgur.com/" + pictureList[position].link + ".jpg").into(holder.view.imageCardPicture)

        if (pictureList[position].isFav) {
            holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
        }
        // Listening to events when clicking on fav star on the picture
        // Fav an image or unfav an image
        holder.view.imageCardStar.setOnClickListener {
            println(pictureList[position].isAlbum)
            if (pictureList[position].isAlbum) {
                favoriteAlbum(pictureList[position], holder)
            } else if (!pictureList[position].isAlbum) {
                favoritePost(pictureList[position], holder)
            }
        }
        // Upvote an image when clicking on the up icon
        holder.view.imageCardIconUp.setOnClickListener {
            upvotePost(pictureList[position], holder)
        }
        // Down vote an image when clicking on the down icon
        holder.view.imageCardIconDown.setOnClickListener {
            downvotePost(pictureList[position], holder)
        }
    }

    class BrowsePageViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}