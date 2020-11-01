package com.example.epicture

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Useful tool to easily print Toasts
class ToastPrinter : AppCompatActivity() {

    fun print(message : String, where : Context) {
        Toast.makeText(where, message, Toast.LENGTH_SHORT).show()
    }
}