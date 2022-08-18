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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.*
import com.google.android.gms.maps.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class gpsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private var calendar = Calendar.getInstance()
    private var currentHour:String =""
    private var totalDistanceNow:Float =0f
    private lateinit var distanceArray: ArrayList<LatLng>
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.myruns2.R.layout.activity_gps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        println("debug: hey we just came into gpsACtivity")
        sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)
        val extras = intent.extras
        if (extras != null) {
            activityType = extras.getInt("activityType")
            val activityTypeInt = extras.getInt("activityType")
            val inputTypeString = extras.getString("activityType").toString()
            println("Activity type is like this => " + activityTypeInt + " (In StartButtonActivity, it is retrieved)")
            println("Input type is like this => " + inputTypeString + " (In StartButtonActivity, it is retrieved)")
            //The key argument here must match that used in the other activity
        } else {
            println("debug: the extras are null.")
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.myruns2.R.id.mapGps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHour();
        setDate();
        distanceArray = ArrayList()
        latLngsForDistanceCalculation = ArrayList()

        database =  RunHistoryDatabase.getInstance(this)
        databaseDao = database.runHistoryDatabaseDao
        repository = RunHistoryRepository(databaseDao)
        factory = RunHistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(RunHistoryViewModel::class.java)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
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
        checkPermission()
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
                locationManager.requestLocationUpdates(provider, 1000, 0f, this)
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
        //how to keep the previous point, so thta we can calculate the distance from it?
        //put this latLng into an array.
        distanceArray.add(latLng)
        addToTotalDistance()
        // and if the size of rray is more than 2, get array[size-1] and array[size-2]
        //size-1 is the element we just added. the size-2 is the previous one.
        latLngsForDistanceCalculation.add(latLng)
        println("debug: latLng is like this now ${latLng} in onLocationChanged")
        println("debug: so let's put this into the array")
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        mMap.animateCamera(cameraUpdate)
        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))
        if (!mapCentered) {
            println("debug: mapCentered is not true")
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            mapCentered = true
        }
    }
    fun addToTotalDistance(){
        if(distanceArray.size<2)
            return
        else{
            val currentLatLng = distanceArray[distanceArray.size-1]
            val previousLatLng = distanceArray[distanceArray.size-2]

            val latCurrent = currentLatLng.latitude
            val lngCurrent = currentLatLng.longitude
            val latPrevious = previousLatLng.latitude
            val lngPrevious = previousLatLng.longitude
            var result  = FloatArray(1)
            Location.distanceBetween(latCurrent, lngCurrent, latPrevious, lngPrevious, result)
            println("debug: this just moved this much. ${result[0]}, so , I will add this to totalDistanceNow, to show it on the screen.")
//            get the distance between this two point.
//                then add that to totalDistanceNow
//            totalDistanceNow = totalDistanceNow+ theDistancebetweenarray[size-1]And[size-2]
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
            initLocationManager()
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

    fun onSaveClicked(view: View){
        Toast.makeText(this, "RunHistory entry saved", Toast.LENGTH_SHORT).show()
        locationManager.removeUpdates(this)
        println("debug: total duration is ${duration} sec. ")
        println("debug: latLngsForDistance is ${latLngsForDistanceCalculation}.  so, when user click save(done), ill go throught this array and get the total distance")

        val tempArray: ArrayList<LatLng> = getTempArray() //has SFU, HOME, STANLEYPARK
        //what should I put here instead of tempArray? ArrayList<LatLng>
        distanceInMeter = calculateTotalDistance(latLngsForDistanceCalculation)
        println("debug: dustanceResult is this ${distanceInMeter}")
        //then I can set distance as this one.
        val latLngsStringVersion = latLngs.toString()
        insertHistoryIntoDB(latLngsStringVersion)
        finish()
    }

    fun calculateTotalDistance(array: ArrayList<LatLng>): Double{
        var i = 0
        // how do I get the size??
//        var result = arrayOf<Float>()
//        Location.distanceBetween(latSfu, lngSfu, latStanley, lngStanley, result)
        while(i<array.size-1){
            println("debug: while loop ${i}th time")
            var result  = FloatArray(1)
            val lat1 = array[i].latitude
            val lng1 = array[i].longitude
            val lat2 = array[i+1].latitude
            val lng2 = array[i+1].longitude
            Location.distanceBetween(lat1, lng1, lat2, lng2, result)
            totalDistance = totalDistance+result[0]
            i++
        }
        return totalDistance

    }
    fun getTempArray(): ArrayList<LatLng>{
        val latSfu = 49.276788
        val lngSfu = -122.916465
        val latLngSfu = LatLng(latSfu, lngSfu)
        // lat long for my home
        val latHome = 49.281765
        val lngHome = -123.013919
        val latLngHome = LatLng(latHome, lngHome)
        //lat long for downtown west
        val latStanley = 49.297118
        val lngStanley = -123.135878
        val latLngStanley = LatLng(latStanley, lngStanley)
        //lat long for northVan
        val latNorthVan= 49.325115
        val lngNorthVan= -123.128333
        val latLngNorthVan= LatLng(latNorthVan, lngNorthVan)
        var arrayToReturn: ArrayList<LatLng> = ArrayList()
        arrayToReturn.add(latLngSfu)
        arrayToReturn.add(latLngHome)
        arrayToReturn.add(latLngStanley)
        arrayToReturn.add(latLngNorthVan)
        return arrayToReturn
    }


    fun onCancelClicked(view: View){
        println("debug: cancel button clicked, so ill discard the record and go back to the main activity")
        finish()
    }
    fun insertHistoryIntoDB(latLngsString:String){
        val runHistoryObj = RunHistory()
        val distanceUnit = sp.getInt("unitType",0)
        runHistoryObj.distanceUnit = distanceUnit
        runHistoryObj.activityType = activityType
        runHistoryObj.inputType = 1//means GPS
        runHistoryObj.date = dateToPassToDB
        runHistoryObj.duration = duration.toInt()
        var avgSpeedPerSecond = distanceInMeter / duration //meter/ second
        var avgSpeedPerMinute = avgSpeedPerSecond *60
        var avgSpeedPerHour = avgSpeedPerMinute *60
//        runHistoryObj.avgSpeed =
        if(distanceUnit==0){
            var totalDistanceAfterKiloUnitConversion:Float = convertFromMeterToKilo(totalDistance)//let's round up to 2 dicimal point
            val df = DecimalFormat("#.##")
            val totalDistanceAfterKiloUnitConversionFinal = df.format(totalDistanceAfterKiloUnitConversion)
            //(this one above / duration) * 360 = kilometer per hour
            var avgSpeedKiloPerHour = (totalDistanceAfterKiloUnitConversionFinal.toFloat() / duration) *360
            println("debug: avgSpeedKiloPerHour is like this ${avgSpeedKiloPerHour}")
            println("debug: I converted ${totalDistance} to ${totalDistanceAfterKiloUnitConversionFinal}")
            runHistoryObj.distance = totalDistanceAfterKiloUnitConversionFinal.toFloat()
            runHistoryObj.avgSpeed = avgSpeedKiloPerHour
            //convert totalDistance into kilo(meter to kilo meter) and then
//            runHistoryObj.distance = totalDistance
        }
        else{
            var totalDistanceAfterKiloUnitConversion:Float = convertFromMeterToKilo(totalDistance)//let's round up to 2 dicimal point
            var totalDistanceAfterMileUnitConversion:Float = convertFromKiloToMile(totalDistanceAfterKiloUnitConversion)
            val df = DecimalFormat("#.##")
            val totalDistanceAfterMileUnitConversionFinal = df.format(totalDistanceAfterMileUnitConversion)
            //(this one above / duration) * 360 = miles per hour
            var avgSpeedMilePerHour = (totalDistanceAfterMileUnitConversionFinal.toFloat() / duration) *360
            println("debug: avgSpeedMilePerHour is like this ${avgSpeedMilePerHour}")

            runHistoryObj.distance = totalDistanceAfterMileUnitConversionFinal.toFloat()
            runHistoryObj.avgSpeed = avgSpeedMilePerHour
            println("debug: I converted ${totalDistance} to ${totalDistanceAfterKiloUnitConversion}, and then to ${totalDistanceAfterMileUnitConversionFinal}")
            //ok!!!!!!!!!
        }
        println("debug: totalDistance.toFloar() is liek this ${totalDistance.toFloat()}")
//        selectedMonth ="${currentYear} : ${currentMonth} : ${currentDay}"
        runHistoryObj.time = timeToPassToDB
        println("debug: runHistoryObj is like this now ${runHistoryObj}")
        println("debug: latLngsStringVersion is like this ${latLngsString}")
        val latLngsByteArrayVersion = latLngsString.toByteArray()
        runHistoryObj.locationList = latLngsByteArrayVersion
        println("debug: in insertHistoryDB, duration is ${duration}")
        val stringFromByteArray = String(latLngsByteArrayVersion)
        println("debug: size of the ByteArray is ${latLngsByteArrayVersion.size}")
        println("debug: I converted byteArray back to String, and here it is ${stringFromByteArray}")
        var latLngsRetrievedFromDB = latLngsString.split(",").toList().toMutableList()
        val theLastIndex = latLngsRetrievedFromDB.size-1
        latLngsRetrievedFromDB[0] = latLngsRetrievedFromDB[0].replace("[","")
        latLngsRetrievedFromDB[theLastIndex] = latLngsRetrievedFromDB[theLastIndex].replace("]","")
        println("debug: first element is ${latLngsRetrievedFromDB[0].replace("[", "")}")
        println("debug: last element is ${latLngsRetrievedFromDB[latLngsRetrievedFromDB.size-1].replace("]","")}")
        for( i in 0..latLngsRetrievedFromDB.size-1){
            println("debug: fromDB, the List from the String  consist of  ${latLngsRetrievedFromDB[i]} :${i}  ")
        }
        viewModel.insert(runHistoryObj)
        println("debug: successfully inserted the runHistoryObj.")
    }
    fun setDate(){
        currentDay = calendar.get(Calendar.DAY_OF_MONTH).toString()
        currentMonth = (calendar.get(Calendar.MONTH)+1).toString()
        currentYear = calendar.get(Calendar.YEAR).toString()
        dateToPassToDB ="${currentYear} : ${currentMonth} : ${currentDay}"

    }

    fun setHour(){
        currentHour = calendar.get(Calendar.HOUR_OF_DAY).toString()
        currentMinute = calendar.get(Calendar.MINUTE).toString()
        timeToPassToDB = "${
            currentHour} : ${currentMinute}"

    }
    fun convertFromMeterToKilo(meterDistance:Double): Float{
        println("debug: I will convert this ${meterDistance} meter to kilometer.")
        return (meterDistance/1000).toFloat()
    }
    fun convertFromKiloToMile(kiloDistance:Float):Float{

        val returnValue = kiloDistance * 0.62137119223733
        return returnValue.toFloat()
    }
}
//class gpsActivity : AppCompatActivity() {
////    private lateinit var saveButton: Button
////    private lateinit var cancelButton: Button
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_gps)
//        supportActionBar?.setTitle("Map")
//
////        saveButton = findViewById(R.id.saveButtonGps)
////        cancelButton = findViewById(R.id.cancelButtonGps)
//        saveButton.setOnClickListener{
//           println("debug: saveButton is clicked")
//            finish()
//        }
//        cancelButton.setOnClickListener{
//            println("debug: cancelButton is clicked")
//            finish()
//
////            val intent = Intent(this, MainActivity::class.java)
////            startActivity(intent)
//        }
//    }
//}