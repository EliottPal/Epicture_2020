package com.example.epicture

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.browse_page.*
import kotlinx.android.synthetic.main.image_card.*
import kotlinx.android.synthetic.main.profile_page.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Variables
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appContext = applicationContext
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var extras = intent.extras

        if (extras != null) {
            accessToken = extras.getString("accessToken")
            refreshToken = extras.getString("refreshToken")
            accountUsername = extras.getString("accountUsername")
            expiresIn = extras.getString("expiresIn")
        }

        println("access = $accessToken")
        println("refresh = $refreshToken")
        println("username = $accountUsername")
        println("expires in = $expiresIn")
    }

    // Load user infos and display them
    override fun onStart() {
        super.onStart()

        (findViewById<View>(R.id.textUsername) as TextView).text = "$accountUsername"
    }

    override fun onResume() {
        super.onResume()

        (findViewById<View>(R.id.textUsername) as TextView).text = "$accountUsername"
    }

    // Navigate to Upload Page
    fun goToUploadPage(v: View?) {
        startActivity(Intent(this@MainActivity, UploadActivity::class.java))
    }

    // Logout from app
    fun logoutApp(v: View?) {
        val cookieManager = CookieManager.getInstance()

        cookieManager.removeAllCookies { aBoolean ->
            Log.d("Cookies removed: ", aBoolean.toString())
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
            finish()
        }
    }

    public companion object {
        var accessToken : String? = null
        var refreshToken: String? = null
        var accountUsername: String? = null
        var expiresIn: String? = null
        var appContext: Context? = null
            private set
    }

}