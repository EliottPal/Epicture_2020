package com.example.epicture.ui.favorites

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.MainActivity
import com.example.epicture.R
import com.example.epicture.ui.browse.BrowseFragmentAdapter
import com.example.epicture.ui.home.HomeFragmentAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_card.*
import kotlinx.android.synthetic.main.image_card.view.*
import okhttp3.*
import java.io.IOException

class FavoritesFragmentAdapter(var pictures : ArrayList<PictureInfos>) :
    RecyclerView.Adapter<FavoritesFragmentAdapter.FavoritesPageViewHolder>(){

    var pictureList = pictures
    private val hostValue = "api.imgur.com"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_card, parent, false)
        return FavoritesPageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    // Favorite an album
    fun favoriteAlbum(picture: PictureInfos, holder: FavoritesPageViewHolder) {
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
                if (picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_off)
                    picture.isFav = false
                } else if (!picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
                    picture.isFav = true
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to add album to favorites")
            }
        })
    }

    fun favoritePost(picture: PictureInfos, holder: FavoritesPageViewHolder) {
        val client = OkHttpClient()
        val imageHash = picture.link
        var requestVote = "https://api.imgur.com/3/image/$imageHash/favorite"
        val requestBody = RequestBody.create(null, "")
        var request = Request.Builder()
                .url(requestVote)
                .post(requestBody)
                .header("Authorization", "Bearer ${MainActivity.accessToken}")
                .build()
        // Make request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("ERROR: Failed to execute the request")
            }
            // Update votes count and display
            override fun onResponse(call: Call, response: Response) {
                println("Favored!")
                println(response)
                // if picture already fav ? setting yellow star icon on : setting yellow star icon off
                if (picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_off)
                    picture.isFav = false
                } else if (!picture.isFav) {
                    holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
                    picture.isFav = true
                }
            }
        })
    }

    // Upvote request
    fun upvotePost(picture: PictureInfos, holder: FavoritesPageViewHolder) {
        holder.view.imageCardIconUp.setColorFilter(Color.BLACK)
        val client = OkHttpClient()
        val galleryHash = picture.link
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
    fun downvotePost(picture: PictureInfos, holder: FavoritesPageViewHolder) {
        holder.view.imageCardIconDown.setColorFilter(Color.BLACK)
        val client = OkHttpClient()
        val galleryHash = picture.link
        var requestVote = "https://api.imgur.com/3/gallery/$galleryHash/vote/down"
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
                println("Downvoted!")
                println(response)
            }
        })
    }

    override fun onBindViewHolder(holder: FavoritesPageViewHolder, position: Int) {
        holder.view.imageCardTitle.text = pictureList[position].title
        holder.view.imageCardDownvotes.text = pictureList[position].downs
        holder.view.imageCardUpvotes.text = pictureList[position].ups
        holder.view.imageCardComments.text = pictureList[position].commentNumber
        holder.view.imageCardStar.setImageResource(android.R.drawable.btn_star_big_on)
        Picasso.get().load("https://i.imgur.com/" + pictureList[position].link + ".jpg").into(holder.view.imageCardPicture)

        holder.view.imageCardStar.setOnClickListener {
            if (pictureList[position].isAlbum) {
                favoriteAlbum(pictureList[position], holder)
            }
            if (!pictureList[position].isAlbum) {
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

    class FavoritesPageViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}