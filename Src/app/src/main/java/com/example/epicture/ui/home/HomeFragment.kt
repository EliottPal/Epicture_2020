package com.example.epicture.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.epicture.R
import com.example.epicture.MainActivity
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.*
import java.io.IOException
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_card.*
import kotlinx.android.synthetic.main.profile_page.*
import org.json.JSONObject

class PictureInfos {
  var id : String? = null
  var title : String? = null
  var description : String? = null
  var link : String? = null
  var ups : String? = null
  var downs : String? = null
  var commentNumber : String? = null
  var isFav : Boolean = false
  var views : String? = null
}

class HomeFragment : Fragment() {

  private lateinit var homeViewModel: HomeViewModel
  private val hostValue = "api.imgur.com"
  private val version = "3"

  private var allPhotos : ArrayList<PictureInfos> = ArrayList()

  private var adapter: HomeFragmentAdapter = HomeFragmentAdapter(allPhotos)

  private val _property = MutableLiveData<PictureInfos>()

  val property: LiveData<PictureInfos>
    get() = _property

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    homeViewModel =
    ViewModelProviders.of(this).get(HomeViewModel::class.java)
    val root = inflater.inflate(R.layout.profile_page, container, false)
    homeViewModel.text.observe(viewLifecycleOwner, Observer {
    })

    // Retrieve access token and username from MainActivity
    val accessToken = ((MainActivity).accessToken)
    val username = ((MainActivity).accountUsername)

    // Retrieve username view from the View
    var usernameView = root.findViewById<TextView>(R.id.textUsername)

    // Set the value of username to retrieved username
    usernameView.text = MainActivity.accountUsername

    // Get the recyclerView from the View
    val recyclerView = root.findViewById<RecyclerView>(R.id.pictures_grid)
    recyclerView.adapter = adapter

    // Fetch profile pic
    retrieveProfilePic(accessToken!!, username!!)
    // Get popularity
    getPopularity(accessToken, username)
    // Get number of posts
    getPostsNumber(accessToken, username)
    // Fetch every pictures of the account
    getAllProfilePics(username!!, accessToken!!)
    return root
  }

  // Display profile pic after getting url
  private fun displayPic(task: Runnable) {
    Handler(Looper.getMainLooper()).post(task)
  }

  // Query to get profile pic + display on profile page
  private fun retrieveProfilePic(accessToken: String, username: String) {
    // Build the correct url
    val queryUrl = HttpUrl.Builder()
            .scheme("https")
            .host(hostValue)
            .addPathSegment(version)
            .addPathSegment("account")
            .addPathSegment(username)
            .addPathSegment("avatar")
            .build()

    // Build the request using url + correct authorization
    val request = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()

    // Build new client
    val client : OkHttpClient = OkHttpClient.Builder().build()

    // Launch request and use data
    client.newCall(request).enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        val data = Gson().fromJson<JsonElement>(response.body!!.string()!!, JsonElement::class.java)
        val picUrl : JsonElement = data.asJsonObject["data"]
        val getPic = picUrl.asJsonObject["avatar"].asString
        displayPic(Runnable {
          Picasso.get().load(getPic).into(userProfilePic)
        })
      }

      override fun onFailure(call: Call, e: IOException) {
        Log.e("Profile pic error", "Can't retrieve profile pic")
      }
    })
  }

  // Get number of posts
  private fun getPostsNumber(accessToken: String, username: String) {
    var queryUrl = HttpUrl.Builder()
            .scheme("https")
            .host(hostValue)
            .addPathSegment(version)
            .addPathSegment("account")
            .addPathSegment(username)
            .addPathSegment("images")
            .build()

    var request = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()
    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        TODO("Not yet implemented")
      }

      override fun onResponse(call: Call, response: Response) {

        val data = JSONObject(response.body?.string())
        val item = data.getJSONArray("data")

        displayPic(Runnable {
          if (number != null) {
            number.text = item.length().toString()
          }
        })
      }
    })
  }

  // Get popularity value
  private fun getPopularity(accessToken: String, username: String) {
    var queryUrl = HttpUrl.Builder()
            .scheme("https")
            .host(hostValue)
            .addPathSegment(version)
            .addPathSegment("account")
            .addPathSegment(username)
            .build()

    var request = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()

    val client = OkHttpClient.Builder().build()
    client.newCall(request).enqueue(object: Callback {
      @SuppressLint("SetTextI18n")
      override fun onResponse(call: Call, response: Response) {
        val data = JSONObject(response.body?.string())
        val item = data.getJSONObject("data")

        println(item)
        displayPic(Runnable {
          val reputation = item["reputation"] as Int
          val reputationType: String = item["reputation_name"].toString()

          popularity.text = reputation.toString()
          if (reputation <= 1)
            popularity.text = "internet point"
          popularity.text = reputationType
        })
      }

      override fun onFailure(call: Call, e: IOException) {
        TODO("Not yet implemented")
      }
    })
  }

  // Fetch pictures related to the account
  private fun getAllProfilePics(username: String, accessToken: String) {
    // Build url
    val queryUrl = HttpUrl.Builder()
            .scheme("https")
            .host(hostValue)
            .addPathSegment(version)
            .addPathSegment("account")
            .addPathSegment(username)
            .addPathSegment("images")
            .build()

    // Build request
    val request = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()

    val client : OkHttpClient = OkHttpClient.Builder().build()

    client.newCall(request).enqueue(object: Callback {
      override fun onResponse(call: Call, response: Response) {
        val data = JSONObject(response.body?.string())
        val pics = data.getJSONArray("data")

        println(pics)
        for (elems in 0 until(pics.length())) {
          val elem = pics.getJSONObject(elems)
          val item = PictureInfos()
          // Get needed infos and stock them into a PictureInfos element
          item.id = elem.getString("id")
          item.title = elem.getString("title")
          item.description = elem.getString("description")
          item.link = elem.getString("link")
          item.isFav = elem.getBoolean("favorite")
          item.views = elem.getString("views")
          if (item.title == "null") {
            item.title = "No title"
          }
          if (item.description == "null") {
            item.description = "No description"
          }
          allPhotos.add(item)
        }
        adapter.pictureList = allPhotos
        // Launch the Runnable task to display photos
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