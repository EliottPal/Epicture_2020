package com.example.epicture.ui.favorites

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.MainActivity
import kotlinx.android.synthetic.main.favorites_page.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.example.epicture.R
import com.example.epicture.services.Setup


class PictureInfos {
    var id : String? = null
    var isAlbum : Boolean = false
    var cover : String? = null
    var title : String? = null
    var description : String? = null
    var link : String? = null
    var ups : String? = null
    var downs : String? = null
    var commentNumber : String? = null
    var isFav : Boolean = true
}

class FavoritesFragment : Fragment() {
    private lateinit var favoritesViewModel : FavoritesViewModel
    private val hostValue = "api.imgur.com"
    private val version = "3"

    private var allPhotos : ArrayList<PictureInfos> = ArrayList()

    private var adapter: FavoritesFragmentAdapter = FavoritesFragmentAdapter(allPhotos)

    private val _property = MutableLiveData<PictureInfos>()

    val property: LiveData<PictureInfos>
        get() = _property

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favoritesViewModel = ViewModelProviders.of(this).get(FavoritesViewModel::class.java)
        val root = inflater.inflate(R.layout.favorites_page, container, false)
        favoritesViewModel.text.observe(viewLifecycleOwner, Observer {
        })

        // Retrieve access token and username from MainActivity
        val accessToken = ((MainActivity).accessToken)
        val username = ((MainActivity).accountUsername)

        // Get recyclerView from the View
        val recyclerView = root.findViewById<RecyclerView>(R.id.favorites_grid)
        recyclerView.adapter = adapter

        getFavoritesPics(accessToken!!, username!!)
        return root
    }

    // Launch Runnable task to display pics
    private fun displayPic(task: Runnable) {
        Handler(Looper.getMainLooper()).post(task)
    }

    // Fetch favorite pics
    private fun getFavoritesPics(accessToken: String, username: String) {
        // Build url
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment(version)
                .addPathSegment("account")
                .addPathSegment(username)
                .addPathSegment("favorites")
                .build()

        // Build request
        val request = Request.Builder()
                .url(queryUrl)
                .header("Authorization", "Bearer $accessToken")
                .build()

        val client : OkHttpClient = OkHttpClient.Builder().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val data = JSONObject(response.body?.string())
                val pics = data.getJSONArray("data")

                if (pics.length() == 0)
                    noFavorites.setTextColor((Color.parseColor("#A9A9A9")))

                println(pics)
                // Get every information and stock them into a PictureInfos element
                for (elems in 0 until pics.length()) {
                    val elem = pics.getJSONObject(elems)
                    val item = PictureInfos()
                    /*item.id = elem.getString("id")*/
                    item.title = elem.getString("title")
                    item.description = elem.getString("description")
                    if (elem.getBoolean("is_album")) {
                        item.link = elem.getString("cover")
                        item.cover = elem.getString("cover")
                        item.isAlbum = true
                    } else if (!elem.getBoolean("is_album")) {
                        item.link = elem.getString("id")
                    }
                    item.id = elem.getString("id")
                    if (item.title == "null") {
                        item.title = "No title"
                    }
                    if (item.description == "null") item.description = "No description"
                    item.ups = elem.getString("ups")
                    item.downs = elem.getString("downs")
                    item.commentNumber = elem.getString("comment_count")
                    item.isFav = true
                    // add the item to the pictures array
                    allPhotos.add(item)
                }
                adapter.pictureList = allPhotos
                displayPic(Runnable {
                    adapter.notifyDataSetChanged()
                })
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }
        })
    }
}