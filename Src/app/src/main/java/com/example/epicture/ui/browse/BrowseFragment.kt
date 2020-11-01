package com.example.epicture.ui.browse

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.MainActivity
import com.example.epicture.Photo
import com.example.epicture.R
import com.example.epicture.ToastPrinter
import com.example.epicture.services.Setup
import kotlinx.android.synthetic.main.browse_page.*
import kotlinx.android.synthetic.main.browse_page.loadingCircle
import kotlinx.android.synthetic.main.favorites_page.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

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
    var isFav : Boolean = false
}

class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: BrowseViewModel
    private val hostValue = "api.imgur.com"
    private val version = "3"

    // val requestUrlViral = "https://api.imgur.com/3/gallery/hot/viral/"

    private var allPhotos : ArrayList<PictureInfos> = ArrayList()

    private var adapter: BrowseFragmentAdapter = BrowseFragmentAdapter(allPhotos)

    private val _property = MutableLiveData<PictureInfos>()

    val property: LiveData<PictureInfos>
        get() = _property

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        browseViewModel = ViewModelProviders.of(this).get(BrowseViewModel::class.java)
        val root = inflater.inflate(R.layout.browse_page, container, false)
        browseViewModel.text.observe(viewLifecycleOwner, Observer {
        })
        val accessToken = ((MainActivity).accessToken)
        val username = ((MainActivity).accountUsername)
        val recyclerView = root.findViewById<RecyclerView>(R.id.browse_grid)
        recyclerView.adapter = adapter

        setSearchEventListener(root, accessToken)
        // Get imgur viral pictures
        fetchViralPictures(accessToken!!, username!!)
        return root
    }

    private fun setSearchEventListener(root: View, accessToken: String?) {
        val btn = root.findViewById<AppCompatImageButton>(R.id.searchButton)

        btn.setOnClickListener {
            searchTopic(accessToken!!)
        }
    }

    // Task handler : used to launch Runnable (will call the adapter when used for picture display)
    private fun displayPic(task: Runnable) {
        Handler(Looper.getMainLooper()).post(task)
    }

    // Imgur API request to get all actual viral pictures
    private fun fetchViralPictures(accessToken: String, username: String) {
        // Build url
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment(version)
                .addPathSegment("gallery")
                .addPathSegment("hot")
                .addPathSegment("viral")
                .build()

        // Build request wuth headers
        val request = Request.Builder()
                .url(queryUrl)
                .header("Authorization", "Client-ID " + Setup.clientID)
                .header("User-Agent", "Epicture")
                .build()

        val client : OkHttpClient = OkHttpClient.Builder().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val data = JSONObject(response.body?.string())
                println(data)

                val pics = data.getJSONArray("data")
                // Loop on every picture returned by the request
                for (elems in 0 until pics.length()) {
                    val elem = pics.getJSONObject(elems)
                    val item = PictureInfos()
                    // If it is an album, the link of the returned photo is the one on the cover
                    // Else, the link is the id returned
                    if (elem.getBoolean("is_album")) {
                        item.link = elem.getString("cover")
                        item.cover = elem.getString("cover")
                        item.isAlbum = true
                    } else if (!elem.getBoolean("is_album"))
                        item.link = elem.getString("id")
                    item.id = elem.getString("id")
                    // Get title and description
                    item.title = elem.getString("title")
                    item.description = elem.getString("description")
                    if (item.description == "null") {
                        item.description = "No description"
                    }
                    if (item.title == "null") {
                        item.title = "No title"
                    }
                    // Get ups and downs + comments count
                    item.ups = elem.getString("ups")
                    item.downs = elem.getString("downs")
                    item.commentNumber = elem.getString("comment_count")
                    // Check if image is fav or not
                    item.isFav = elem.getBoolean("favorite")
                    allPhotos.add(item)
                }
                // Set the value of the picture array in the browse adapter
                adapter.pictureList = allPhotos
                // Launch action
                displayPic(Runnable {
                    adapter.notifyDataSetChanged()
                })
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }
        })
    }

    // Search to a specific topic -> will display the images related
    public fun searchTopic(accessToken: String) {
        loadingCircle.visibility = View.VISIBLE
        // Get the string entered by the user
        var toSearch = searchInput.text.toString()
        println(toSearch)
        val client : OkHttpClient = OkHttpClient.Builder().build()
        val queryUrl = "https://api.imgur.com/3/gallery/search/top?q=$toSearch"

        val request = Request.Builder()
                .url(queryUrl)
                .header("Authorization", "Bearer $accessToken")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val data = JSONObject(response.body?.string())
                println(data)
                val pics = data.getJSONArray("data")
                println(pics)
                if (pics.length() == 0) {
                    displayPic(Runnable {
                        ToastPrinter().print("No posts found", MainActivity.appContext!!)
                    })
                    return
                }
                // Clear the array filled with virals
                allPhotos.clear()
                // Clear the screen with empty picture array
                adapter.pictureList = allPhotos
                displayPic(Runnable {
                    adapter.notifyDataSetChanged()
                })
                for (elems in 0 until pics.length()) {
                    val elem = pics.getJSONObject(elems)
                    val item = PictureInfos()
                    if (elem.getBoolean("is_album")) {
                        item.link = elem.getString("cover")
                        item.cover = elem.getString("cover")
                        item.isAlbum = true
                    } else if (!elem.getBoolean("is_album"))
                        item.link = elem.getString("id")
                    item.id = elem.getString("id")
                    item.title = elem.getString("title")
                    if (item.title == "null")
                        item.title = "No title"
                    item.ups = elem.getString("ups")
                    item.downs = elem.getString("downs")
                    item.commentNumber = elem.getString("comment_count")
                    item.isFav = elem.getBoolean("favorite")
                    allPhotos.add(item)
                }
                adapter.pictureList = allPhotos
                displayPic(Runnable {
                    adapter.notifyDataSetChanged()
                })
                loadingCircle.visibility = View.GONE
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }
        })
    }
}