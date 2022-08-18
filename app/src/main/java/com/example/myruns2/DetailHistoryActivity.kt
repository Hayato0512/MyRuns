package com.example.myruns2

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class DetailHistoryActivity: AppCompatActivity() {
    private lateinit var detailHistoryListView: ListView
    private lateinit var detailHistoryDeleteButton: Button
    private lateinit var detailHistoryListAdapter: ArrayAdapter<String>
    private lateinit var database: RunHistoryDatabase
    private lateinit var databaseDao: RunHistoryDatabaseDao
    private lateinit var repository: RunHistoryRepository
    private lateinit var factory: RunHistoryViewModelFactory
    private lateinit var runHistoryViewModel: RunHistoryViewModel
    private lateinit var recievedBundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)
        //we don't come here. why??

        supportActionBar?.setTitle("DetailHistoryPage")
        println("debug:hey hey, this is the detail history page")
         recievedBundle = intent.extras!!
        println("debug: recieved ${recievedBundle} in DetailHistoryActivity")

        val inputTypeString = convertInputType(recievedBundle?.getInt("inputType"))
        val activityTypeString = convertActivityType(recievedBundle?.getInt("activityType"))
        val unitType = recievedBundle?.getString("unitToShowDetail")
        println("debug: inputTypeString is like this"+ inputTypeString)
        println("debug: activityTypeString is like this"+ activityTypeString)
        val arrayFromBundle = ArrayList<String>()
        arrayFromBundle.add("Input Type : ${inputTypeString}")
        arrayFromBundle.add("Activity Type : ${activityTypeString}")
        arrayFromBundle.add("Date and time: ${recievedBundle?.getString("time")} ${recievedBundle?.getString("date")}")
        arrayFromBundle.add("Duration : ${recievedBundle?.getInt("duration")} mins 0secs")
        arrayFromBundle.add("Distance : ${recievedBundle?.getFloat("distance")} ${unitType}")
        arrayFromBundle.add("Calories : ${recievedBundle?.getFloat("calories")} cals")
        arrayFromBundle.add("Heart Rate : ${recievedBundle?.getInt("heartRate")} bpm")
        arrayFromBundle.add("Comment: ${recievedBundle?.getString("comment")}")
//        arrayFromBundle.add("distanceUnit: ${recievedBundle?.getInt("distanceUnit")}")


        detailHistoryListView = findViewById(R.id.detailHistoryListView)
        detailHistoryDeleteButton = findViewById(R.id.detailHistoryDeleteButton)
        database =  RunHistoryDatabase.getInstance(this)
        databaseDao = database.runHistoryDatabaseDao
        repository = RunHistoryRepository(databaseDao)
        factory = RunHistoryViewModelFactory(repository)
       runHistoryViewModel = ViewModelProvider(this, factory).get(RunHistoryViewModel::class.java)

        detailHistoryListAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayFromBundle)
        detailHistoryListView.adapter = detailHistoryListAdapter


//        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,MENUS)

    }

    fun deleteButtonClicked(view: View){
        println("debug: deleteBUtton is clicked. here, I will delete the chosen entry from the DB.")
//ok, let's take a small break and then work on deletion from the DB. let's look at how we insert. we can just do the same thing
        //very easy.
//        viewModel.insert(runHistoryObj)
        val id = recievedBundle?.getLong("id")
        runHistoryViewModel.deleteOne(id)
        println("debug: successfully deleted the entry with id ${id}")
        //how do I get the id of this??
        finish()

    }

    fun convertInputType(input:Int):String{
        if(input==0){
            return "Manual Entry"
        }
        else if(input==1){
            return "GPS"
        }
        else{
            return "Automatic"
        }

    }
    fun convertActivityType(activityType:Int):String{
        var activityTypeString = "default"
        if(activityType ==0){
            println("debug: activityType is "+ activityType+", so i wll store Running")
            activityTypeString = "Running"
            return activityTypeString
        }
        else if(activityType ==1){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Walking"
            return activityTypeString
        }
        else if(activityType ==2){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Standing"
            return activityTypeString
        }
        else if(activityType ==3){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Cycling"
            return activityTypeString
        }
        else if(activityType ==4){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Hiking"
            return activityTypeString
        }
        else if(activityType ==5){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Downhill Skiing"
            return activityTypeString
        }
        else if(activityType ==6){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Cross-Country Skiing"
            return activityTypeString
        }
        else if(activityType ==7){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Snowboarding"
            return activityTypeString
        }
        else if(activityType ==8){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Skating"
            return activityTypeString
        }
        else if(activityType ==9){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Swimming"
            return activityTypeString
        }
        else if(activityType ==10){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Mountain Biking"
            return activityTypeString
        }
        else if(activityType ==11){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Wheelchair"
            return activityTypeString
        }
        else if(activityType ==12){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Eliptical"
            return activityTypeString
        }
        else if(activityType ==13){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Other"
            return activityTypeString
        }
        return "return haha"
    }
}