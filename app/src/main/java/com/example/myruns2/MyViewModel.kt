package com.example.myruns2

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class MyViewModel: ViewModel() {
    val userImg = MutableLiveData<Bitmap>()
    val userText = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()
    val userPhone = MutableLiveData<String>()
}//for now, just remember thsi viewModel thing is just sth useful
