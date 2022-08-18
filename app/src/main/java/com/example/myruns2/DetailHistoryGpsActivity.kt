package com.example.myruns2


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.*
import com.google.android.gms.maps.R
import com.google.android.gms.maps.model.*
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class DetailHistoryGpsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private var calendar = Calendar.getInstance()
    private var currentHour:String =""
    private var totalDistance:Double = 0.0
    private var currentMinute:String =""
    private var timeToPassToDB:String =""
    private var currentDay:String =""
    private var currentMonth:String =""
    private var currentYear:String =""
    private var dateToPassToDB:String =""
    private lateinit var sp: SharedPreferences
    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var  latLngs: ArrayList<Double>
    private lateinit var  latLngsForDistanceCalculation: ArrayList<LatLng>
    private  var  duration: Long = 0
    private  var activityType:Int = 0
    private lateinit var activityTypeString:String
    private lateinit var database: RunHistoryDatabase
    private lateinit var databaseDao: RunHistoryDatabaseDao
    private lateinit var repository: RunHistoryRepository
    private lateinit var viewModel: RunHistoryViewModel
    private lateinit var factory: RunHistoryViewModelFactory
    private  var distanceInMeter: Double = 0.0
    private lateinit var recievedBundle: Bundle
    private lateinit var textView: TextView
    private lateinit var latLngPointsList: ArrayList<LatLng>
    private var stringFromByteArray:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.myruns2.R.layout.activity_detail_gps_history)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        println("debug: hey we just came into gpsACtivity")
        sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)
        recievedBundle = intent.extras!!

        val activityTypeString = convertActivityType(recievedBundle?.getInt("activityType"))
        val unitType = recievedBundle?.getString("unitToShowDetail")
        var avgSpeed = recievedBundle?.getFloat("avgSpeed")
        println("debug: avgSpeed that I got from recievedBundle is this ${avgSpeed}")
        val locationListByteArray = recievedBundle?.getByteArray("locationList")
        val distance = recievedBundle?.getFloat("distance")
        val duration = recievedBundle?.getInt("duration")
        try{
            println("debug; onCreate in DetailHistory,  locationListByteArray is ${locationListByteArray!=null}")
            println("debug; onCreate in DetailHistory, unitType is ${unitType}")
            println("debug; onCreate in DetailHistory,  distance is ${distance}")
            println("debug; onCreate in DetailHistory, duration  is ${duration}")
            println("debug; onCreate in DetailHistory, let's calculate the average speed out of it")
            avgSpeed = getAvgSpeedFromBundle(recievedBundle)
            /// if the unit changes, locationListByteArray is null. WHY??? if I don't change unit, then this is true;
            //is it only the BYteAray?? let's check if the other recieveBundel is also null
             stringFromByteArray = String(locationListByteArray!!)
        }catch (err:Exception){
            println("debug: err is like this ${err}")
        }
        latLngPointsList = ArrayList()
        println("debug: stringFromByteArray look like this ${stringFromByteArray}")
        var latLngsRetrievedFromDB = stringFromByteArray.split(",").toList().toMutableList()
        val theLastIndex = latLngsRetrievedFromDB.size-1
        latLngsRetrievedFromDB[0] = latLngsRetrievedFromDB[0].replace("[","")
        latLngsRetrievedFromDB[theLastIndex] = latLngsRetrievedFromDB[theLastIndex].replace("]","")
        var i = 0
        while(i<latLngsRetrievedFromDB.size-1)
        {
            println("debug: fromDB, the List from the String  consist of  ${latLngsRetrievedFromDB[i]} :${i}  ")
            println("debug: [${i}]th item and [${i+1}]th item is set like this  lat:${latLngsRetrievedFromDB[i]}, lng; ${latLngsRetrievedFromDB[i+1]}")
            val lat = latLngsRetrievedFromDB[i].toDouble()
            val lng = latLngsRetrievedFromDB[i+1].toDouble()
            val point = LatLng(lat, lng)
            latLngPointsList.add(point)
            i = i+2
        }
        //THIS IS TRIAL FOR CALCULATING THE CLIMB, BUT NOT PRIORITY
//        println("debug: after while loop, latLngPointsList is ${latLngPointsList}")
//        println("debug: after while loop, latLngPointsList[0] is ${latLngPointsList[0]}")
//        println("debug: after while loop, latLngPointsList.size is ${latLngPointsList.size}")
//        val lastIndex = latLngPointsList.size-1
//        val startLatLng = latLngPointsList[0]
//        println("debug: after while loop, altitude of start point  is ${}")
//        val finishLatLng = latLngPointsList[lastIndex]


        textView = findViewById(com.example.myruns2.R.id.detailGpsText)
        textView.text = "Type: ${activityTypeString}\nAvg speed: ${avgSpeed} ${unitType}/h \n Climb: 0 ${unitType} \n Calorie: 0 \n Distance: ${distance} ${unitType}"
//        if (extras != null) {
//            activityType = extras.getInt("activityType")
//            val activityTypeInt = extras.getInt("activityType")
//            val inputTypeString = extras.getString("activityType").toString()
//            println("Activity type is like this => " + activityTypeInt + " (In StartButtonActivity, it is retrieved)")
//            println("Input type is like this => " + inputTypeString + " (In StartButtonActivity, it is retrieved)")
//            //The key argument here must match that used in the other activity
//        } else {
//            println("debug: the extras are null.")
//        }
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.myruns2.R.id.mapGps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        latLngsForDistanceCalculation = ArrayList()

        database =  RunHistoryDatabase.getInstance(this)
        databaseDao = database.runHistoryDatabaseDao
        repository = RunHistoryRepository(databaseDao)
        factory = RunHistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(RunHistoryViewModel::class.java)

    }

    override fun onDestroy() {
        super.onDestroy()
//        if (locationManager != null)
//            locationManager.removeUpdates(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        println("debug: the map is ready now")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()
        latLngs = ArrayList()

        println("debug: will ask checkPermission()")
        val middlePointIndex = (latLngPointsList.size /2).toInt()
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngPointsList[middlePointIndex], 11f)
        mMap.animateCamera(cameraUpdate)
        for( point in latLngPointsList){
            //write a line on the point on a map
            polylineOptions.add(point)
            polylines.add(mMap.addPolyline(polylineOptions))
        }
        val startPoint = latLngPointsList[0]
        val finishPoint = latLngPointsList[latLngPointsList.size-1]
        markerOptions.position(startPoint).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMap.addMarker(markerOptions)
        markerOptions.position(finishPoint).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(markerOptions)
//        checkPermission()
    }

    fun initLocationManager() {
        try {
            println("debug: came in to initLocationManager()")
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    // lat long for SFU camput
                    val latSfu = 49.276788
                    val lngSfu = -122.916465
                    val latLng = LatLng(latSfu, lngSfu)
                    // lat long for my home
                    val latHome = 49.281765
                    val lngHome = -123.013919
                    //lat long for downtown west
                    val latStanley = 49.297118
                    val lngStanley = -123.135878
                    var result  = FloatArray(1)
                    Location.distanceBetween(latSfu, lngSfu, latStanley, lngStanley, result)
                    //Yes. This is working. this Location.distanceBetween is working, so I can get the distance at last.
                    //the result should be like 14000
                    //how is this stored in result?? visit every element of result to see.
                    for(item in result){
                        println("debug: item in result look like this ${item}")
                    }
                    println("debug: the distance between the two point is ${result}")
                    println("debug:  will call onLocationChanged")
                    onLocationChanged(location)
                    println("debug:  after calling  onLocationChanged")
                }
//                locationManager.requestLocationUpdates(provider, 1000, 0f, this)
                //ok now, the onLocationChanged is called every 5 seconds
            }
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        duration++;
        println("debug: duration is  ${duration} sec now")
        println("debug: onlocationchanged()")
        val lat = location.latitude
        val lng = location.longitude
        latLngs.add(lat)
        latLngs.add(lng)
        val latLng = LatLng(lat, lng)
        latLngsForDistanceCalculation.add(latLng)
        println("debug: latLng is like this now ${latLng} in onLocationChanged")
        println("debug: so let's put this into the array")
        if (!mapCentered) {
            println("debug: mapCentered is not true")
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        for (i in polylines.indices) polylines[i].remove()
        polylineOptions.points.clear()
    }

    override fun onMapLongClick(latLng: LatLng) {
//        markerOptions.position(latLng!!)
//        mMap.addMarker(markerOptions)
        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))
    }

    //if permitted, call initLocationManager()
    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else {
            println("debug: the permission OK. so initialize location Maager()")
//            initLocationManager()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}



    fun getAvgSpeedFromBundle(bundle: Bundle):Float{
        val durationInSec = bundle.getInt("duration")
        val distance = bundle.getFloat("distance")
        println("debug: getAvgSpeedFromBundle, duration is ${durationInSec}, distance ->${distance}")
        var distancePerSecond = distance / durationInSec
        println("debug: since you ran ${distance} in ${durationInSec} sec, so per second should be ${distancePerSecond}")
        var distancePerMin = distancePerSecond * 60
        println("debug: since the distancePerSecond is ${distancePerSecond}, times 60 is distancePerMinite ${distancePerMin}")
        var distancePerHour = distancePerMin * 60
        println("debug: since the distancePerMin is ${distancePerMin}, times 60 is distancePerHour ${distancePerHour}")
        return distancePerHour

    }

    fun deleteButtonClicked(view: View){
        println("debug: deleteBUtton is clicked. here, I will delete the chosen entry from the DB.")
//ok, let's take a small break and then work on deletion from the DB. let's look at how we insert. we can just do the same thing
        //very easy.
//        viewModel.insert(runHistoryObj)
        val id = recievedBundle?.getLong("id")
        viewModel.deleteOne(id)
        println("debug: successfully deleted the entry with id ${id}")
        //how do I get the id of this??
        finish()

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
