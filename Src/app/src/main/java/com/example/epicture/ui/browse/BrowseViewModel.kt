package com.example.epicture.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epicture.ui.browse.PictureInfos

class BrowseViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _property = MutableLiveData<PictureInfos>()

    val property: LiveData<PictureInfos>
        get() = _property
}