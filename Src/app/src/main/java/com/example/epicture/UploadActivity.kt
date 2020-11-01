package com.example.epicture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class UploadActivity : AppCompatActivity() {

    // Variables
    private val GALLERY= 1
    private val CAMERA = 2
    private val PERMISSION_CODE = 1001
    private val client = OkHttpClient()
    private var switch = false
    private var imageSelected = false
    private var imageBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        loadingCircle.visibility = View.GONE;


        // CHOOSE FROM GALLERY button
        uploadButtonGallery.setOnClickListener {
            // check runtime permission
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                // show popup to request permission
                switch = false
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else {
                // permission already granted
                pickImageFromGallery()
            }
        }

        // TAKE PICTURE FROM CAMERA button
        uploadButtonCamera.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                // permission denied
                val permissions = arrayOf(Manifest.permission.CAMERA)
                // show popup to request permission
                switch = true
                requestPermissions(permissions, CAMERA)
            }
            else {
                // permission already granted
                takePictureFromCamera()
            }
        }

        // UPLOAD TO IMGUR button
        uploadButtonPost.setOnClickListener {
            uploadImageToImgur()
        }
    }

    // Intent to open camera and take a pic
    private fun takePictureFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(intent, CAMERA)
    }

    // Intent to pick image in the gallery
    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)

    }

    // Choose what to do if the permission is allowed/denied
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from pop-up granted
                    if (!switch) {
                        pickImageFromGallery()
                    } else {
                        takePictureFromCamera()
                    }
                } else {
                    // permission from pop-up denied
                    ToastPrinter().print("Permission denied", this)
                }
            }
        }
    }

    // Change displayed image if permission is ok & image has been selected
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY) {
            val contentURI = data?.data
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI)
            imageToUpload?.setImageBitmap(bitmap)
            imageSelected = true
            imageBitmap = bitmap

        }
        else if (resultCode == Activity.RESULT_OK && requestCode == CAMERA) {
            var bitmap = data?.extras?.get("data") as Bitmap
            imageToUpload.setImageBitmap(bitmap)
            imageSelected = true
            imageBitmap = bitmap
        }
    }

    // Upload picture to imgur
    private fun uploadImageToImgur() {
        var postTitle = inputPostTitle.text.toString()

        // Error management
        if (!imageSelected) {
            ToastPrinter().print("Please select an image to upload before", this)
            return
        }
        if (postTitle == "") {
            ToastPrinter().print("Please give a title to your post", this)
            return
        }
        // Upload picture
        loadingCircle.visibility = View.VISIBLE
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title",postTitle)
            .addFormDataPart("image", encodeTobase64(imageBitmap!!))
            .build()

        val url = "https://api.imgur.com/3/image"
        var request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", "Bearer " + (MainActivity).accessToken)
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val nextIntent: Intent = Intent(this@UploadActivity, MainActivity::class.java)
                startActivity(nextIntent)
            }
        })
    }

    // Encode bitmap image to string
    private fun encodeTobase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

}