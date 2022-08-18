package com.example.myruns2

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException
import java.util.*

class UserInputViewModel: ViewModel() {
   var date = MutableLiveData<String> ()
    var time = MutableLiveData<String> ()
    var duration = MutableLiveData<Int> ()
    var distance = MutableLiveData<Float> ()
    var calories = MutableLiveData<Float> ()
    var heartRate = MutableLiveData<Int> ()
    var comment = MutableLiveData<String> ()
 var distanceUnit = MutableLiveData<Int> ()

}

//    val userImg = MutableLiveData<Bitmap>()
//    val userText = MutableLiveData<String>()
//    val userName = MutableLiveData<String>()
//    val userEmail = MutableLiveData<String>()
//    val userPhone = MutableLiveData<String>()
//}//for now, just remember thsi viewModel thing is