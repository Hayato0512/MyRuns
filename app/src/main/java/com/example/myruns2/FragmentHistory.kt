package com.example.myruns2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.ArrayList

//Fragment for History tab
class FragmentHistory : Fragment(){

    private lateinit var database: RunHistoryDatabase
    private lateinit var runHistoryStorageArray: List<RunHistory>
    private lateinit var databaseDao: RunHistoryDatabaseDao
    private lateinit var repository: RunHistoryRepository
    private lateinit var viewModel: RunHistoryViewModel
    private lateinit var factory: RunHistoryViewModelFactory
    private lateinit var sp: SharedPreferences
    private lateinit var myListView: ListView
    private  var unit: String =""
    private lateinit var handler: Handler
    private lateinit var arrayList: ArrayList<String>
    private lateinit var unitViewModel :ViewModelBetweenFragments
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private  lateinit var bundleArray:ArrayList<Bundle>
    private  lateinit var locationList:ByteArray
    override fun onCreateView(
        inflater:LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the xml, and then return it.



        return inflater.inflate(R.layout.fragment_history, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myListView = view.findViewById(R.id.runHistoryListView)
        arrayList = ArrayList()
        arrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, arrayList)
        myListView.adapter = arrayAdapter

        database =  RunHistoryDatabase.getInstance(requireActivity())
        databaseDao = database.runHistoryDatabaseDao
        repository = RunHistoryRepository(databaseDao)
        factory = RunHistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory).get(RunHistoryViewModel::class.java)



        unitViewModel =
            ViewModelProvider(requireActivity()).get(ViewModelBetweenFragments::class.java)
        unitViewModel.Unit.observe(viewLifecycleOwner) {

            val unitInt2 = unitViewModel.Unit.value
            if (unitInt2 == 0) {
                unit = "KiloMeters"
            } else {
                unit = "Miles"
            }


            handler = Handler(Looper.getMainLooper())
            val runnable = Runnable{
                println("debug: from FragmentHistory. I observe that the prefered unit has changed")
                println("debug: let's see if we can change the UI at the same time as this units change")

                val newArray: ArrayList<String> = ArrayList()
                bundleArray = ArrayList()
                if (!runHistoryStorageArray.isEmpty()) {//enmtpy, dont do anything
                    println("debug: in SP changeListener, I got runHistoryStorageArray like ${runHistoryStorageArray}")
                    for (i in 0..runHistoryStorageArray.size - 1) {
                        var bundle = Bundle()

                        val item = runHistoryStorageArray[i]
//                val inputType = item.inputType
                        //why is item.inputType null??
                        val inputType = convertInputType(item.inputType)
//                val activityType = item.activityType
                        val activityType = convertActivityType(item.activityType)
                        val time = item.time
                        val date = item.date
                        var distance = item.distance
                        val distanceUnit = item.distanceUnit

                        if(unitInt2==distanceUnit){
                            distance = item.distance
                            println("debug; the unit matches. so , no change on the value of the distance")
                        }
                        else{
                            println("debug: the unit of the history is ${distanceUnit}, but the unitFrom Setting is ${unitInt2}")
                            if(distanceUnit==0){

                                var convertedDistance = convertFromKiloToMile(item.distance).toFloat()
                                distance = convertedDistance
                                //convert  k->m
                            }
                            else{
                                //convert  m->k, (userinput Unit is miles, but setting is kilo. so , take distance(miles) and convert intoKIlo return Long
                                var convertedDistance = convertFromMileToKilo(item.distance).toFloat()
                                distance = convertedDistance

                            }
                        }
                        val duration = item.duration
                        var durationMin = 0
                        var durationSec = 0
                        val calories = item.calories
                        val heartRate = item.heartRate
                        val comment = item.comment
                        locationList = item.locationList
                        //if sp returns 0, unit = Miles
                        //if sp returns 1, unit = Kiko
                        var stringToAdd =
                            "${inputType} : ${activityType}, ${time} ${date} ${distance} ${unit}, ${duration}mins  0 secs"

                        if(inputType=="GPS"){
                            durationMin = duration /60
                            durationSec = duration % 60
                            stringToAdd = "${inputType} : ${activityType}, ${time} ${date} ${distance} ${unit}, ${durationMin}mins  ${durationSec} secs"
                        }
                        println("debug: unit is this  in this RUnnable. what is going on?" + unit)
                        println("debug: stringToAdd is like this" + stringToAdd)
                        bundle.putLong("id", item.id)
                        bundle.putInt("inputType", item.inputType)
                        bundle.putInt("activityType", item.activityType)
                        bundle.putString("time", time)
                        bundle.putString("date", date)
                        bundle.putFloat("distance", distance)
                        bundle.putInt("duration", duration)
                        bundle.putFloat("calories", calories)
                        bundle.putInt("heartRate", heartRate)
                        bundle.putString("comment", comment)
                        bundle.putByteArray( "locationList", locationList)
                        bundle.putInt("distanceUnit", distanceUnit)
//                println("debug:${item}")
                        ///before add, get each, and put them together and pass to the array
                        //make bundle , put everything together, and then store that into newArray2, so that onclick we can access by index
                        bundleArray.add(bundle)
                        newArray.add(stringToAdd)

                    }
                    arrayAdapter.clear()
                    arrayAdapter.addAll(newArray)
                    println("debug: arrayAdapter is just updated. so that means the UI changes now")


                }
            }
            handler.post(runnable)


        }
//        unitViewModel.Unit.observe(requireActivity()){
//            println("debug: from FragmentHistory. I observe that the prefered unit has changed")
//        }

        sp = requireActivity().getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)
//       val unitInt = sp.getInt("unitType", 0)
//        if(unitInt==0){
//            unit = "Kilos"
//        }
//        else {
//            unit = "Miles"
//        }
//                sp.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
//
//                    val unitInt = sp.getInt("unitType", 0)
//                    if(unitInt==0){
//                        unit = "Kilos"
//                        println("debug: inFragmentHistory. unit will be Kilos")
//                    }
//                    else{
//                        unit = "Miles"
//                        println("debug: inFragmentHistory. unit will be Miles")
//                    }
//
//                    val newArray: ArrayList<String> = ArrayList()
//                    bundleArray = ArrayList()
//                    if(!runHistoryStorageArray.isEmpty()){
//                        println("debug: in SP changeListener, I got runHistoryStorageArray like ${runHistoryStorageArray}")
//                    for( i in 0..runHistoryStorageArray.size-1){
//                        var bundle = Bundle()
//
//                        val item = runHistoryStorageArray[i]
////                val inputType = item.inputType
//                        //why is item.inputType null??
//                        val inputType = convertInputType(item.inputType)
////                val activityType = item.activityType
//                        val activityType = convertActivityType(item.activityType)
//                        val time = item.time
//                        val date = item.date
//                        val distance = item.distance
//                        val duration = item.duration
//                        val calories = item.calories
//                        val heartRate = item.heartRate
//                        val comment = item.comment
//                        val distanceUnit = item.distanceUnit
//                        //if sp returns 0, unit = Miles
//                        //if sp returns 1, unit = Kiko
//                        val stringToAdd = "${inputType} : ${activityType}, ${time} ${date} ${distance} ${unit}, ${duration} secs"
//                        println("debug: stringToAdd is like this"+stringToAdd)
//                        bundle.putLong("id", item.id)
//                        bundle.putInt( "inputType", item.inputType)
//                        bundle.putInt( "activityType", item.activityType)
//                        bundle.putString( "time", time)
//                        bundle.putString( "date", date)
//                        bundle.putFloat( "distance", distance)
//                        bundle.putInt( "duration", duration)
//                        bundle.putFloat( "calories", calories)
//                        bundle.putInt( "heartRate", heartRate)
//                        bundle.putString( "comment", comment)
//                        bundle.putInt( "distanceUnit", distanceUnit)
////                println("debug:${item}")
//                        ///before add, get each, and put them together and pass to the array
//                        //make bundle , put everything together, and then store that into newArray2, so that onclick we can access by index
//                        bundleArray.add(bundle)
//                        newArray.add(stringToAdd)
//
//                    }
//                        arrayAdapter.clear()
//                        arrayAdapter.addAll(newArray)
//
//                    }
////                    var allRunHistoryData = viewModel.allRunHistoryLiveData
////                    println("debug: allRunHistoryDta is this ${allRunHistoryData}")
//                }



        viewModel.allRunHistoryLiveData.observe(requireActivity()){
            println("debug: this is only called when I insert new RunHistory.")
            val newArray: ArrayList<String> = ArrayList()
            bundleArray = ArrayList()
            runHistoryStorageArray = it
            println("debug: it is like this ${it}")

            val unitType = sp.getInt("unitType", 0)
            println("debug: unitType  is like this from SP ${unitType}")
//            val unitInt2 = unitViewModel.Unit.value
//            println("debug: unit is like this from viewModel ${unitInt2}")
            //this is null at first. what is sp doing???
            if (unitType == 0) {
                unit = "KiloMeters"
                println("debug: so the unit will be this ${unit}")
            } else {
                unit = "Miles"
                println("debug: so the unit will be this ${unit}")
            }


            for( i in 0..it.size-1){
                var bundle = Bundle()

                val item = it[i]
                val distanceUnit = item.distanceUnit
//                val inputType = item.inputType
                //why is item.inputType null??
                val inputType = convertInputType(item.inputType)
//                val activityType = item.activityType
                val activityType = convertActivityType(item.activityType)
                val time = item.time
                val date = item.date
                var distance = item.distance
                var avgSpeed = item.avgSpeed
                locationList = ByteArray(1)
                locationList = item.locationList
                val unitFromSp = sp.getInt("unitType",0)
                if(unitFromSp==distanceUnit){
                    distance = item.distance
                    println("debug; the unit matches. so , no change on the value of the distance")
                }
                else{
                    println("debug: the unit of the history is ${distanceUnit}, but the unitFrom Setting is ${unitFromSp}")
                    if(distanceUnit==0){

                        var convertedDistance = convertFromKiloToMile(item.distance).toFloat()
                        distance = convertedDistance
                        //convert  k->m
                    }
                    else{
                        //convert  m->k, (userinput Unit is miles, but setting is kilo. so , take distance(miles) and convert intoKIlo return Long
                        var convertedDistance = convertFromMileToKilo(item.distance).toFloat()
                        distance = convertedDistance

                    }
                }
                val duration = item.duration
                var durationMin = 0
                var durationSec = 0
                var stringToAdd = "${inputType} : ${activityType}, ${time} ${date} ${distance} ${unit}, ${duration}mins  0 secs"
                if(inputType=="GPS" || inputType == "Automatic"){
                    durationMin = duration /60
                    durationSec = duration % 60
                    stringToAdd = "${inputType} : ${activityType}, ${time} ${date} ${distance} ${unit}, ${durationMin}mins  ${durationSec} secs"
                }
                val calories = item.calories
                val heartRate = item.heartRate
                val comment = item.comment
                //if sp returns 0, unit = Miles
                //if sp returns 1, unit = Kiko
                //right here

                println("debug: stringToAdd is like this"+stringToAdd)
                println("debug: hahahahahah. the unit is "+unit)
                bundle.putLong("id", item.id)
                bundle.putInt( "inputType", item.inputType)
                bundle.putInt( "activityType", item.activityType)
                bundle.putString( "time", time)
                bundle.putString( "date", date)
                bundle.putFloat( "distance", distance)
                bundle.putInt( "duration", duration)
                bundle.putFloat( "calories", calories)
                bundle.putInt( "heartRate", heartRate)
                bundle.putString( "comment", comment)
                bundle.putInt( "distanceUnit", distanceUnit)
                bundle.putByteArray( "locationList", locationList)
                bundle.putFloat( "avgSpeed", avgSpeed)
//                println("debug:${item}")
                ///before add, get each, and put them together and pass to the array
                //make bundle , put everything together, and then store that into newArray2, so that onclick we can access by index
                bundleArray.add(bundle)
                newArray.add(stringToAdd)
                arrayAdapter.clear()
                arrayAdapter.addAll(newArray)
                println("debug: cheliss ${it.size}")

            }
//            for( item in it){
//                println("debug: yoisyo yoisyo")
//                println("debug: item.inputType is =>" +  item.inputType)
//                val inputType = item.inputType
//                val activityType = item.activityType
//                val time = item.time
//                val date = item.date
//                val distance = item.distance
//                val duration = item.duration
//                val stringToAdd = "${inputType} : ${activityType}, ${time} ${date} ${distance} Miles, ${duration} secs"
//                println("debug:${item}")
//                ///before add, get each, and put them together and pass to the array
//                newArray.add(stringToAdd)
//            }
        }

        myListView.setOnItemClickListener{ parent, view, position, id ->
            println("debug: list view position ${position} clicked (id: ${id})")
            val chosenBundle = bundleArray[position]

            chosenBundle.putString( "unitToShowDetail", unit)
            println("debug: onItemClickListener, the unitToShowDetail is this ${unit}")
            println("debug: onItemClickListener, distance is like ${chosenBundle.getFloat("distance")}")
            println("debug: onItemClickListener, duration is like ${chosenBundle.getInt("duration")}")

            println("debug: clicked spots returns this bundle => ${chosenBundle}")
            println("debug: the unit to pass to detailHistoryActivity is like this=> ${unit}")
            if(bundleArray[position].getInt("inputType")==0){
                println("debug: yes this entry is manual")
                val intent = Intent(requireActivity(),DetailHistoryActivity::class.java)
                intent.putExtras(chosenBundle)
                startActivity(intent)
            }
            else if(bundleArray[position].getInt("inputType")==1){
                println("debug: yes this entry is GPS")
                println("debug: print locationList ${String(locationList!!)}")
                val intent = Intent(requireActivity(),DetailHistoryGpsActivity::class.java)
                intent.putExtras(chosenBundle)
                startActivity(intent)
            }
            else{
                println("debug: yes this entry is automatic")
                val intent = Intent(requireActivity(),DetailHistoryGpsActivity::class.java)
                intent.putExtras(chosenBundle)
                startActivity(intent)

            }


            //then wehn intent.putExtras(chosenBundle)
//            val intent = Intent(requireActivity(), StartButtonActivity::class.java)
//            intent.putExtras(bundleToPass)
//            startActivity(intent)

            //start a newActivity with the given record
            //how do I do taht??
            //when I create those listView, I can assign the position too. maybe?yes, becuase this gets updated everytime we add, or delte .
            //that is the good idea , let's just try.


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
    fun convertFromKiloToMile(value:Float):Double{
        //take kilo and convert that into Mile
        val returnValue = value * 0.62137119223733
        return returnValue
    }

    fun convertFromMileToKilo(value:Float):Double{
        //take kilo and convert that into Mile
        val returnValue = value * 1.609344
        return returnValue
    }
}
