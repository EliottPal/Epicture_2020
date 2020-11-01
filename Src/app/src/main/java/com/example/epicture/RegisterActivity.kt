package com.example.epicture

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.epicture.services.ImgurActivity
import kotlinx.android.synthetic.main.activity_upload.*

class RegisterActivity : AppCompatActivity() {
    private val callbackUrl: String = "https://www.getpostman.com/oauth2/callback"
    var accessToken: String? = null
    var refreshToken: String? = null
    var username: String? = null
    var expiresIn: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide notification bar & support bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportActionBar?.hide()
        this.setContentView(R.layout.activity_register);
    }

    private fun intentMainActivity() {
        val goToHomePage = Intent(this, MainActivity::class.java)
        startActivity(goToHomePage)
        finish()
    }

    // Click on Log-in Button
    fun logInApplication(v: View?) {
        loadingCircle.visibility = View.VISIBLE
        ImgurActivity.requestLogIn(this)
    }

    // Function called when user logged in on imgur site -> redirecting to the app here
    override fun onResume() {
        super.onResume()

        // intent.data allows us to access data sent back during callback
        val data = intent.data

        // parsing the url to access the informations needed : access token / refresh token / username / token expiration
        if (data != null && data.toString().startsWith(callbackUrl)) {
            if (data.getQueryParameter("error") == null) {
                val newData = Uri.parse(data.toString().replace('#', '?'))
                accessToken = newData.getQueryParameter("access_token")
                refreshToken = newData.getQueryParameter("refresh_token")
                username = newData.getQueryParameter("account_username")
                expiresIn = newData.getQueryParameter("expires_in")
                val transfer = Intent(this, MainActivity::class.java)
                // putExtra method allows to transfer data from one activity to another using intent
                transfer.putExtra("accessToken", accessToken)
                transfer.putExtra("refreshToken", refreshToken)
                transfer.putExtra("accountUsername", username)
                transfer.putExtra("expiresIn", expiresIn)
                startActivity(transfer)
                finish()
            }
        }
    }
}