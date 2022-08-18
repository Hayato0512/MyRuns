package com.example.myruns2


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File



class Profile: AppCompatActivity() {



    private lateinit var imageView: ImageView
    //private lateinit var textView: TextView
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editClass: EditText
    private lateinit var editMajor: EditText
    private lateinit var saveButton: Button
    private lateinit var imgUri: Uri
    //lateinit means , initiate in a later time.
    private val imgFileName= "hayato_img.jpg"
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var myViewModel: MyViewModel
    private lateinit var sp:SharedPreferences
    private var line:String?= "..."
    private var nameLine:String? = ""
    private var emailLine:String? = ""
    private var phoneLine:String? = ""
    private var majorLine:String? = ""
    private var classLine:String? = ""
    private val TEXT_KEY = "key"
    private val NAME_KEY = "key"
    private val EMAIL_KEY = "key"
    private val PHONE_KEY = "key"
    private val pickImage = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        imageView = findViewById(R.id.imageProfile)
        //textView = findViewById(R.id.text_view)
        editName = findViewById(R.id.text_name)
        editEmail = findViewById(R.id.text_email)
        editPhone = findViewById(R.id.text_phone)
        editClass = findViewById(R.id.text_class)
        editMajor = findViewById(R.id.text_major)
        saveButton = findViewById(R.id.saveButton)
        Util.checkPermissions(this)

        sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)

        val imgFile = File(getExternalFilesDir(null),imgFileName)

        imgUri = FileProvider.getUriForFile(this, "com.example.myruns2", imgFile)


        if(!::myViewModel.isInitialized){

            myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
            //so the reason why I was getting kotlin.UninitializedPropertyAccessException
            // is because I mistypo get.(myViewModel....) since myViewModel is a variable which class is
            // MyViewModel. so, it is just a typo , but also, I made sure that I include this line in the if statement block.

        }

        myViewModel.userImg.observe(this ){
            val bitmap = Util.getBitmap(this,imgUri)
            imageView.setImageBitmap(bitmap)
            //so this line is to update when new photo took.
        }
        myViewModel.userName.observe(this){

            editName?.setText(nameLine)
        }
        /*
    myViewModel.userText.observe(this ){
        textView.text = line
    }*/
        //parameter is the owner of this viewModel, what the hell does that mean?

        if(imgFile.exists()){
            val bitmap = Util.getBitmap(this,imgUri)
            imageView.setImageBitmap(bitmap)
        }
        //line = imgUri.path.toString()
        if(savedInstanceState != null){
            line = savedInstanceState.getString(TEXT_KEY)
            nameLine = savedInstanceState.getString(NAME_KEY)
            emailLine = savedInstanceState.getString(EMAIL_KEY)
            phoneLine = savedInstanceState.getString(PHONE_KEY)
        }


        nameLine = sp.getString("name","")
        emailLine = sp.getString("email","")
        phoneLine = sp.getString("phone","")
        classLine = sp.getString("class","")
        majorLine = sp.getString("major","")
        editName?.setText(nameLine)
        editEmail?.setText(emailLine)
        editPhone?.setText(phoneLine)
        editClass?.setText(classLine)
        editMajor?.setText(majorLine)
        //editEmail.text = emailLine
        //editPhone.text = phoneLine
        // textView.text = line



        //cuz this cameraResult will be called when we take a camera. and then line will be assigned the url.
        //but, just bring those line outside so that even when we don't take puctures, the picture we took
        //has still url info on the screen.
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult() ){
                it: ActivityResult ->
            if(it.resultCode== Activity.RESULT_OK){
                val bitmap = Util.getBitmap(this,imgUri)
                myViewModel.userImg.value = bitmap

                line = imgUri.path.toString()
                //      myViewModel.userText.value = line
                //when activity is killed, these text uri is not gonna saved in actiity,
                // but in ViewModel.
                //so we are separating data and userinterface(activity)
                //simply because android likes to kill the activity

                //textView.text = line
                //imageView.setImageBitmap(bitmap)

            }
        }
        //the second parameter is a function, since it is ramda, we don't need to name it.


    }



    fun onSaveClicked(view: View){
        nameLine = editName.getText().toString()
        emailLine = editEmail.getText().toString()
        phoneLine = editPhone.getText().toString()
        classLine = editClass.getText().toString()
        majorLine = editMajor.getText().toString()
        val editor: SharedPreferences.Editor = sp.edit()
        editor.putString("name", nameLine)
        editor.putString("email", emailLine)
        editor.putString("phone", phoneLine)
        editor.putString("class", classLine)
        editor.putString("major", majorLine)
        editor.commit()
        System.exit(0)
    }
    fun onCancelClicked(view:View){
        System.exit(0)
    }

    fun onChangePhotoClicked(view:View){
        println("debug: change Clicked, so will show the dialog here.")
        val dialogBuilder = AlertDialog.Builder(this)
//        dialogBuilder.setMessage("do you wanna take picture or select from Gallery?")
            .setCancelable(false)
            .setPositiveButton("Open Camera", DialogInterface.OnClickListener{
                dialog, id->
                println("debug: ok, i will open the taking photo")
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
                cameraResult.launch(intent)

            })
            .setNegativeButton("Select from Gallery", DialogInterface.OnClickListener{
                    dialog, id->
                println("debug: ok,  I will take you to the Gallery to select")
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if(intent.resolveActivity(packageManager) != null){
                    startActivityForResult(intent, 1)
                }

            })
          val alert = dialogBuilder.create()
        alert.setTitle("Pick Profile Picture")
        alert.show()
//        val dialog = PhotoChangeDialog()
//
//        val bundle = Bundle()
//        dialog.arguments = bundle
//        dialog.show(supportFragmentManager, "tag")
//
//        var cameraFrag = false
//        if(cameraFrag) {
//
//        }
//        else{
////            println("debug: I want to store this image into the page.")
////            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
////            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
////            cameraResult.launch(intent)
////            val bitmap = Util.getBitmap(this,imgUri)
////            imageView.setImageBitmap(bitmap)
//
//        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            imgUri = data?.data!!
            imageView.setImageURI(imgUri)
        }
    }
    //everytime an activity is pushed into background, this onsaveInstanceState is called,
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_KEY, line)
        outState.putString(NAME_KEY, nameLine)
        outState.putString(EMAIL_KEY, emailLine)
        outState.putString(PHONE_KEY, phoneLine)
        //we gonna store line in outState object, and when the new activity is created,
        //the Bundle outState will be passed to the original onCreate function.
    }

//paste here

//    fun isPermissionsAllowed(): Boolean {
//        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            false
//        } else true
//    }
//
//    fun askForPermissions(): Boolean {
//        if (!isPermissionsAllowed()) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                showPermissionDeniedDialog()
//            } else {
//                ActivityCompat.requestPermissions(this as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE)
//            }
//            return false
//        }
//        return true
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
//        when (requestCode) {
//            REQUEST_CODE -> {
//                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission is granted, you can perform your operation here
//                } else {
//                    // permission is denied, you can ask for permission again, if you want
//                    //  askForPermissions()
//                }
//                return
//            }
//        }
//    }
//
//    private fun showPermissionDeniedDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("Permission Denied")
//            .setMessage("Permission is denied, Please allow permissions from App Settings.")
//            .setPositiveButton("App Settings",
//                DialogInterface.OnClickListener { dialogInterface, i ->
//                    // send to app settings if permission is denied permanently
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts("package", getPackageName(), null)
//                    intent.data = uri
//                    startActivity(intent)
//                })
//            .setNegativeButton("Cancel",null)
//            .show()
//    }
//
//



}







/*MyRuns1
* Author: Hayato Koyama
* StdID: 301423217
* create date: May 25th, 2022
*
* */



//class MainActivity : AppCompatActivity() {
//
//    // url object to use to intent to show palce to save trhe picture??
//
////everytime the orientation changes, this acvitity is killed and birth again.
//    //so, if we need to keep showing the url on the sided screen, how do we do that?
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
