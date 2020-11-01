package com.example.epicture.services

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat.startActivity
import com.example.epicture.MainActivity
import okhttp3.*
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.example.epicture.RegisterActivity
import com.google.gson.internal.`$Gson$Types`.resolve
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull

fun success() {
    println("success")
}

object ImgurActivity {
    private val infos : List<String> = listOf(
        "account_name",
        "account_id",
        "refresh_token"
    )

    private const val clientId : String = Setup.clientID
    private const val clientSecret : String = Setup.clientSecret
    private const val responseType : String = "token"

    private var isLogged = false
    private const val hostValue = "api.imgur.com"
    private const val version = "3"
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private var refresh : String = "2fe9c83811a0c2a7ffe7eb52e0f9d369d016c6a2"
    private var accessToken : String? = null
    private var refreshToken : String? = null
    private var username : String? = null
    private val credentials : HashMap<String, String> = HashMap()
    private var profilePicUrl : String? = null

    // Function redirecting to imgur site to authorize connection
    fun requestLogIn(context: Context) {
        val queryUrl: String = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=$responseType"
        val navIntent = Intent(Intent.ACTION_VIEW,
                               Uri.parse((queryUrl)))
        startActivity(context, navIntent, null)
    }

    fun getProfilePic(username: String?, accessToken: String?) {
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment(version)
                .addPathSegment("account")
                .addPathSegment(username!!)
                .build()

        val request = Request.Builder()
                .url(queryUrl)
                .header("Authorization", "Client-ID $clientId")
                .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val data = Gson().fromJson<JsonElement>(response.body!!.string()!!, JsonElement::class.java)
                val picUrl : JsonElement = data.asJsonObject["data"]
                profilePicUrl = picUrl.asJsonObject["avatar"].asString
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("yes")
            }
        })
    }

    fun retrieveProfilePicUrl() : String? {
        return profilePicUrl
    }

    /*fun loadInformations(success: () -> Unit, failure: () -> Unit) {
        if (!checkSavedInformations()) {
            return failure()
        }
        newRefreshToken(success, failure)
    }

    private fun checkSavedInformations(): Boolean {
        val saved: SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(MainActivity.appContext)

        for (elem in infos) {
            val info = saved.getString(elem, "")!!
            if (info.isEmpty()) {
                credentials.clear()
                return false
            }
            credentials[elem] = info
        }
        return true
    }

    private fun newRefreshToken(success: () -> Unit, failure: () -> Unit) {
        val queryUrl = HttpUrl.Builder()
                .scheme("https")
                .host(hostValue)
                .addPathSegment("oauth2")
                .addPathSegment("token")
                .build()

        val bodyContent = FormBody.Builder()
                .add("refresh_token", credentials["refresh_token"]!!)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "refresh_token")
                .build()

        val query = Request.Builder()
                .url(queryUrl)
                .post(bodyContent)
                .header("Authorization", "Bearer $clientId")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

        client.newCall(query).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val data = Gson().fromJson<JsonElement>(response.body!!.string()!!, JsonElement::class.java)
                for (elem in listOf("access_token", "refresh_token", "expires_in")) {
                    credentials[elem] = data.asJsonObject[elem].asString
                }
                saveInformations()
                isLogged = true
                return success()
            }

            override fun onFailure(call: Call, e: IOException) {
                return failure()
            }
        })
    }

    private fun saveInformations() {
        val saver = androidx.preference.PreferenceManager.getDefaultSharedPreferences(MainActivity.appContext).edit()

        for (elem in infos) {
            saver.putString(elem, credentials[elem])
        }
        saver.apply()
    }*/
}

data class Avatar (
        val avatar : String?
)

data class ImgurResponse<T> (
        val data : T,
        val success : Boolean
)

class returnData<T> (
        val data : T
)