package com.example.myruns2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class StartButtonActivity :AppCompatActivity() , TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    private val MENUS = arrayOf("Date", "Time", "Duration", "Distance",
        "Calories", "Heart Rate", "Comment")
    private var calendar = Calendar.getInstance()
    private lateinit var myListView: ListView
    private lateinit var textView: TextView
    private lateinit var sp: SharedPreferences
    private lateinit var saveButton:Button
    private lateinit var cancelButton:Button
     lateinit var userInputViewModel: UserInputViewModel
    private lateinit var commentDialog:CommentDialog
    private lateinit var commentEditText:EditText
    private lateinit var selectedTime:String
    private lateinit var selectedMonth:String
//        val commentEditText:EditText = findViewById(R.id.commentDialogInput)

    private lateinit var database: RunHistoryDatabase
    private lateinit var databaseDao: RunHistoryDatabaseDao
    private lateinit var repository: RunHistoryRepository
    private lateinit var viewModel: RunHistoryViewModel
    private lateinit var factory: RunHistoryViewModelFactory
    private  var activityType:Int = 0
    private lateinit var activityTypeString:String
    private lateinit var inputTypeString:String
    private var inputType:Int = 0


override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startbutton)
        myListView = findViewById(R.id.myListView)
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,MENUS)
        myListView.adapter = arrayAdapter
        val timePickerDialog = TimePickerDialog(this, this, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),false)
        val durationDialog = DurationDialog()
        val distanceDialog = DistanceDialog()
        val caloriesDialog = CaloriesDialog()
        val heartRateDialog = HeartRateDialog()
        commentDialog = CommentDialog()

    sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)
    sp.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
        println("debug: in StaretButtonActivity. key of SP is changed. key=> ${key}")
    }
    val extras = intent.extras
        if (extras != null) {
             activityType = extras.getInt("activityType")
            inputType = extras.getInt("inputType")
            val activityTypeInt =extras.getInt("activityType")
            val inputTypeString =extras.getString("activityType").toString()
            println("Activity type is like this => "+activityTypeInt+ " (In StartButtonActivity, it is retrieved)")
            println("Input type is like this => "+inputTypeString+ " (In StartButtonActivity, it is retrieved)")
            //The key argument here must match that used in the other activity
        }
        else{
            println("debug: the extras are null.")
        }

        userInputViewModel = ViewModelProvider(this).get(UserInputViewModel::class.java)


        userInputViewModel.comment.observe(this){
        println("debug: comment just got changed. detected from StartButton Acticitiy." )
        }

        database =  RunHistoryDatabase.getInstance(this)
        databaseDao = database.runHistoryDatabaseDao
        repository = RunHistoryRepository(databaseDao)
        factory = RunHistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(RunHistoryViewModel::class.java)


        myListView.setOnItemClickListener(){ parent: AdapterView<*>, textView: View, position:Int, id:Long->
          if(position==0){
              println("Date is clicked")
//              val dateDialog = DateDialog()
              val bundle = Bundle()
              bundle.putInt(DateDialog.DIALOG_KEY, DateDialog.TEST_DIALOG)
//              dateDialog.arguments = bundle
//              dateDialog.show(supportFragmentManager,"tag");

              val datePickerDialog = DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(
                  Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))

              datePickerDialog.show()
          }
            if(position==1){
                println("Time is clicked")
               timePickerDialog.show()
            }
            if(position==2){
                println("Duration is clicked")
                val bundle = Bundle()
                durationDialog.arguments = bundle
                durationDialog.show(supportFragmentManager, "tag")
            }
            if(position==3){
                println("Distance is clicked")
                val bundle = Bundle()
                distanceDialog.arguments = bundle
                distanceDialog.show(supportFragmentManager, "tag")
            }
            if(position==4){
                println("Calories is clicked")
                val bundle = Bundle()
                caloriesDialog.arguments = bundle
                caloriesDialog.show(supportFragmentManager, "tag")
            }
            if(position==5){
                println("Heart Rate is clicked")
                val bundle = Bundle()
                heartRateDialog.arguments = bundle
                heartRateDialog.show(supportFragmentManager, "tag")
            }
            if(position==6){
                println("Comment is clicked")

                val bundle = Bundle()
                commentDialog.arguments = bundle
                commentDialog.show(supportFragmentManager, "tag")
                println("debug: commentDialog.arguments is like this" + commentDialog.arguments)
//        commentEditText = findViewById(R.id.commentDialogInput)
                //there is something wrong with this line above

            }

            saveButton = findViewById<Button>(R.id.save_button)
            cancelButton = findViewById<Button>(R.id.cancel_button)
            cancelButton.setOnClickListener(){
                println("debug: cancelButton Clicked are you fucking kidding me?" )
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }

        }
    }

//    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//        println("debug: onCreateView is just called`")
//
//        return super.onCreateView(name, context, attrs)
//    }

    fun saveButtonClicked(view:View){
        Toast.makeText(this, "RunHistory entry saved", Toast.LENGTH_SHORT).show()

        println("debug: the value of userInputViewmodel.commnet is " + userInputViewModel.comment.value)
        val userInputComment = userInputViewModel.comment.value
        //why null??? i changed the owner from this to requireAc() in comment dialog fragment and it fixed

        if(!this::selectedTime.isInitialized){
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY).toString()
            val currentMinite = calendar.get(Calendar.MINUTE).toString()
            selectedTime ="${currentHour} : ${currentMinite}"
            println("debug: selectedTime has not been initialized, so I assign the current time")
        }
        if(!this::selectedMonth.isInitialized){
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val currentMonth = (calendar.get(Calendar.MONTH)+1).toString()
            val currentYear = calendar.get(Calendar.YEAR).toString()
            selectedMonth ="${currentYear} : ${currentMonth} : ${currentDay}"
            println("debug: selectedMonth has not been initialized, so I assign the current day")
        }
        //what would  be the value if user don't put any thing? null or "":?? null. so, i need to check if they are null
        val runHistoryObj = RunHistory()
//        runHistoryObj.activityType = activityType
        val distanceUnit = sp.getInt("unitType",0)
        println("debug: here startButtonClicked(). distanceUnit is ${distanceUnit}")
       runHistoryObj.distanceUnit = distanceUnit
        if(activityType ==0){
            println("debug: activityType is "+ activityType+", so i wll store Running")
            activityTypeString = "Running"
        }
        else if(activityType ==1){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Walking"
        }
        else if(activityType ==2){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Standing"
        }
        else if(activityType ==3){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Cycling"
        }
        else if(activityType ==4){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Hiking"
        }
        else if(activityType ==5){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Downhill Skiing"
        }
        else if(activityType ==6){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Cross-Country Skiing"
        }
        else if(activityType ==7){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Snowboarding"
        }
        else if(activityType ==8){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Skating"
        }
        else if(activityType ==9){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Swimming"
        }
        else if(activityType ==10){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Mountain Biking"
        }
        else if(activityType ==11){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Wheelchair"
        }
        else if(activityType ==12){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Eliptical"
        }
        else if(activityType ==13){
            println("debug: activityType is "+ activityType+", so i wll store Something other than Running")
            activityTypeString = "Other"
        }
        println("debug:Finally,   activityType is like this"+activityTypeString)
        println("debug: inputType is like this"+inputType)
        runHistoryObj.activityType = activityType
        runHistoryObj.inputType = 0
        //why is this null??
        println("debug; runHistoryobj.inputType is like this"+ runHistoryObj.inputType)
        runHistoryObj.date = selectedMonth
        runHistoryObj.time = selectedTime
        if(userInputViewModel.duration.value!=null){
            runHistoryObj.duration = userInputViewModel.duration.value!!
        }
        else{
            runHistoryObj.duration=0

        }
        if(userInputViewModel.distance.value!=null){
            runHistoryObj.distance = userInputViewModel.distance.value!!
            //check
        }
        else{
            runHistoryObj.distance=0f

        }
        if(userInputViewModel.calories.value!=null){
            runHistoryObj.calories = userInputViewModel.calories.value!!
            println("debug:runHistoryObj.calories is like this => ${runHistoryObj.calories}")
        }
        else{
            runHistoryObj.calories=0f

        }
        if(userInputViewModel.heartRate.value!=null){
            runHistoryObj.heartRate = userInputViewModel.heartRate.value!!
            println("debug: runHistortyOBJ.heartRate is like this" + runHistoryObj.heartRate)
        }
        else{
            println("debug: heartRateis null")
            runHistoryObj.heartRate=0

        }
        if(userInputViewModel.comment.value!=null){
            runHistoryObj.comment = userInputViewModel.comment.value!!
            println("debug: runHistortyOBJ.comment is like this" + runHistoryObj.comment)
        }
        else{
            runHistoryObj.comment = ""

        }
//       var commentInput:String = commentEditText.text.toString()
//        println("debug: commentInput is like this" + commentInput)
        println("debug: just inserted new run record")
        viewModel.insert(runHistoryObj)
        //here, we are inserting.
        //so, before this line, I need to check , what is the unit,=> get sp("unit"), and set it as the distanceUnit
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun cancelButtonClicked(view:View){
        Toast.makeText(this, "RunHistory entry cancelled", Toast.LENGTH_SHORT).show()
        println("debug: cancelButtonClicked")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

    override fun onTimeSet(view: TimePicker, hour:Int, minute: Int) {
//        textView.text = "$hour : $minute"
        println("debug: got this time => $hour : $minute"  )
        selectedTime = "$hour : $minute"
    }

    override fun onDateSet(view: DatePicker, year:Int, month: Int, day: Int ) {
        val modifiedMonth = month + 1
//        textView.text = "$year : $month : $day"
        println("debug: got this day => $year : $modifiedMonth : $day" )
        selectedMonth = "$year : $modifiedMonth : $day"

    }


}