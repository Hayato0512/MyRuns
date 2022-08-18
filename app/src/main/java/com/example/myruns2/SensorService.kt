package com.example.myruns2

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myruns2.WekaClassifier.classify
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.ArrayBlockingQueue


class SensorService : Service(), SensorEventListener {
    private val mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2
    private lateinit var mFeatureFile: File
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private var mServiceTaskType = 0
    private lateinit var mLabel: String
    private lateinit var mDataset: Instances
    private lateinit var mClassAttribute: Attribute
    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private lateinit var sp: SharedPreferences
    private  var  arrayToPassToMain =ArrayList<Double>()
    val mdf = DecimalFormat("#.##")

    override fun onCreate() {
        super.onCreate()
        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)
        sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE)
        println("debug: just came into the sensorService.kt")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        val extras = intent.extras
//        mLabel = extras!!.getString(Globals.CLASS_LABEL_KEY)!!
        mFeatureFile = File(getExternalFilesDir(null), Globals.FEATURE_FILE_NAME)
        Log.d(Globals.TAG, mFeatureFile.absolutePath)
        mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT

        // Create the container for attributes
        val allAttr = ArrayList<Attribute>()

        // Adding FFT coefficient attributes
        val df = DecimalFormat("0000")
        for (i in 0 until Globals.ACCELEROMETER_BLOCK_CAPACITY) {
            allAttr.add(Attribute(Globals.FEAT_FFT_COEF_LABEL + df.format(i.toLong())))
        }
        // Adding the max feature
        allAttr.add(Attribute(Globals.FEAT_MAX_LABEL))

        // Declare a nominal attribute along with its candidate values
        val labelItems = ArrayList<String>(3)
        labelItems.add(Globals.CLASS_LABEL_STANDING)
        labelItems.add(Globals.CLASS_LABEL_WALKING)
        labelItems.add(Globals.CLASS_LABEL_RUNNING)
        labelItems.add(Globals.CLASS_LABEL_OTHER)
        mClassAttribute = Attribute(Globals.CLASS_LABEL_KEY, labelItems)
        allAttr.add(mClassAttribute)

        // Construct the dataset with the attributes specified as allAttr and
        // capacity 10000
        mDataset = Instances(Globals.FEAT_SET_NAME, allAttr, Globals.FEATURE_SET_CAPACITY)

        // Set the last column/attribute (standing/walking/running) as the class
        // index for classification
        mDataset.setClassIndex(mDataset.numAttributes() - 1)
        val i = Intent(this, MainActivity::class.java)
        // Read:
        // http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
        // IMPORTANT!. no re-create activity
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pi = PendingIntent.getActivity(this, 0, i, 0)
        val notification: Notification = Notification.Builder(this)
                .setContentTitle(
                        applicationContext.getString(
                                R.string.ui_sensor_service_notification_title))
                .setContentText(
                        resources
                                .getString(
                                        R.string.ui_sensor_service_notification_content))
                .setSmallIcon(R.drawable.logo).setContentIntent(pi).build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notification.flags = (notification.flags
                or Notification.FLAG_ONGOING_EVENT)
        notificationManager.notify(0, notification)
        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if(::mAsyncTask.isInitialized){
            mAsyncTask.cancel(true)
        }
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if(::mSensorManager.isInitialized){
            mSensorManager.unregisterListener(this)
        }
        Log.i("", "")
        super.onDestroy()
    }

    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            val inst: Instance = DenseInstance(mFeatLen)
            inst.setDataset(mDataset)
            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                    * im[i])
                            inst.setValue(i, mag)
                            im[i] = .0 // Clear the field
                        }

                        // Append max after frequency component
                        inst.setValue(Globals.ACCELEROMETER_BLOCK_CAPACITY, max)
//                        inst.setValue(mClassAttribute, mLabel)
//                        mDataset.add(inst)
                        //let's pass inst into the classifier here, and see what is the result.

                        val result = classify(inst.toDoubleArray().toTypedArray())

                        println("debug: the result is like this ${result}")
                        arrayToPassToMain.add(result)
                        //i think, those inst thing is indivisual data we look for, and Dataset is a collection of those sensor,
                        //so I think we can just throw that to classifier?? lets see the type of mDataSet
//                        println("debug: type of mDataset is ${mDataset::class.simpleName}")
                        //b::class.simpleName
//                        Log.i("new instance", mDataset.size.toString() + "")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //this is for when user stop colletiong-> when the sensor service is stopped
        //so, maybe this is when user presss save button in my case.
        //so , which one to throw?? it is mDataSet?? or mAccBuffer?/ let's figure outl.
        override fun onCancelled() {
            Log.e("123", mDataset.size.toString() + "")
            println("debug: the array to pass is like this ${arrayToPassToMain}")
            val sizeOfArray = arrayToPassToMain.size
            var tempValue = 0.0
            for(item in arrayToPassToMain){
               tempValue += item
            }
            var finalResult = tempValue /sizeOfArray
            println("debug: finalResult to pass to the main is ${finalResult}")
            sendMessageToActivity(finalResult.toString())
            val editor:SharedPreferences.Editor = sp.edit();
            editor.putFloat("result", finalResult.toFloat())
            editor.commit()
            //here , go through the array, and get the avarage value, and then pass that as putDouble or putFloat
            if (mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY) {
                super.onCancelled()
                return
            }
            Log.i("in the loop", "still in the loop cancelled")
            var toastDisp: String
            if (mFeatureFile.exists()) {

                // merge existing and delete the old dataset
                val source: ConverterUtils.DataSource
                try {
                    // Create a datasource from mFeatureFile where
                    // mFeatureFile = new File(getExternalFilesDir(null),
                    // "features.arff");
                    source = ConverterUtils.DataSource(FileInputStream(mFeatureFile))
                    // Read the dataset set out of this datasource
                    val oldDataset = source.dataSet
                    oldDataset.setClassIndex(mDataset.numAttributes() - 1)
                    // Sanity checking if the dataset format matches.
                    if (!oldDataset.equalHeaders(mDataset)) {
                        // Log.d(Globals.TAG,
                        // oldDataset.equalHeadersMsg(mDataset));
                        throw java.lang.Exception(
                                "The two datasets have different headers:\n")
                    }

                    // Move all items over manually
                    for (i in mDataset.indices) {
                        oldDataset.add(mDataset[i])
                    }
                    mDataset = oldDataset
                    // Delete the existing old file.
                    mFeatureFile.delete()
                    Log.i("delete", "delete the file")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                toastDisp = getString(R.string.ui_sensor_service_toast_success_file_updated)
            } else {
                toastDisp = getString(R.string.ui_sensor_service_toast_success_file_created)
            }
            Log.i("save", "create saver here")
            // create new Arff file
            val saver = ArffSaver()
            // Set the data source of the file content
            saver.instances = mDataset
            Log.e("1234", mDataset.size.toString() + "")
            try {
                // Set the destination of the file.
                // mFeatureFile = new File(getExternalFilesDir(null),
                // "features.arff");
                saver.setFile(mFeatureFile)
                // Write into the file
                saver.writeBatch()
                Log.i("batch", "write batch here")
                Toast.makeText(applicationContext, toastDisp,
                        Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                toastDisp = getString(R.string.ui_sensor_service_toast_error_file_saving_failed)
                e.printStackTrace()
            }
            Log.i("toast", "toast here")
            super.onCancelled()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        println("debug: Sensor is Changed so onSensorChanged is just evoked")
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                    * event.values[2])).toDouble())

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.
            try {
                mAccBuffer.add(m)
            } catch (e: IllegalStateException) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                mAccBuffer.drainTo(newBuf)
                mAccBuffer = newBuf
                mAccBuffer.add(m)
            }
        }
    }

    private fun sendMessageToActivity(msg: String) {
        val intent = Intent("intentKey")
        // You can also include some extra data.
        intent.putExtra("key", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


internal object WekaClassifier {
    @Throws(java.lang.Exception::class)
    fun classify(i: Array<Double>): Double {
        var p = Double.NaN
        p = N2523b2450(i)
        return p
    }

    fun N2523b2450(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() <= 506.558422) {
            p = N5f9ae71c1(i)
        } else if ((i[0] as Double?)!!.toDouble() > 506.558422) {
            p = N2bd1cc1a41(i)
        }
        return p
    }

    fun N5f9ae71c1(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() <= 69.342601) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() > 69.342601) {
            p = N4951af3d2(i)
        }
        return p
    }

    fun N4951af3d2(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() <= 328.22904) {
            p = N20ca5d883(i)
        } else if ((i[0] as Double?)!!.toDouble() > 328.22904) {
            p = N1fa5e3e727(i)
        }
        return p
    }

    fun N20ca5d883(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[15] == null) {
            p = 1.0
        } else if ((i[15] as Double?)!!.toDouble() <= 9.328311) {
            p = N18c3624e4(i)
        } else if ((i[15] as Double?)!!.toDouble() > 9.328311) {
            p = N4977b52526(i)
        }
        return p
    }

    fun N18c3624e4(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() <= 166.653995) {
            p = N10e9c44d5(i)
        } else if ((i[0] as Double?)!!.toDouble() > 166.653995) {
            p = 1.0
        }
        return p
    }

    fun N10e9c44d5(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[13] == null) {
            p = 1.0
        } else if ((i[13] as Double?)!!.toDouble() <= 2.00879) {
            p = N5485b92a6(i)
        } else if ((i[13] as Double?)!!.toDouble() > 2.00879) {
            p = N5d63158013(i)
        }
        return p
    }

    fun N5485b92a6(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() <= 82.522639) {
            p = N2651982f7(i)
        } else if ((i[0] as Double?)!!.toDouble() > 82.522639) {
            p = 1.0
        }
        return p
    }

    fun N2651982f7(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[11] == null) {
            p = 1.0
        } else if ((i[11] as Double?)!!.toDouble() <= 2.423461) {
            p = N25835fb98(i)
        } else if ((i[11] as Double?)!!.toDouble() > 2.423461) {
            p = 0.0
        }
        return p
    }

    fun N25835fb98(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[24] == null) {
            p = 1.0
        } else if ((i[24] as Double?)!!.toDouble() <= 0.413187) {
            p = N7b963ce89(i)
        } else if ((i[24] as Double?)!!.toDouble() > 0.413187) {
            p = 1.0
        }
        return p
    }

    fun N7b963ce89(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[14] == null) {
            p = 1.0
        } else if ((i[14] as Double?)!!.toDouble() <= 0.683989) {
            p = N223865910(i)
        } else if ((i[14] as Double?)!!.toDouble() > 0.683989) {
            p = 0.0
        }
        return p
    }

    fun N223865910(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[9] == null) {
            p = 0.0
        } else if ((i[9] as Double?)!!.toDouble() <= 0.647259) {
            p = N2819f6d211(i)
        } else if ((i[9] as Double?)!!.toDouble() > 0.647259) {
            p = 1.0
        }
        return p
    }

    fun N2819f6d211(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[4] == null) {
            p = 1.0
        } else if ((i[4] as Double?)!!.toDouble() <= 3.89264) {
            p = N17580aff12(i)
        } else if ((i[4] as Double?)!!.toDouble() > 3.89264) {
            p = 0.0
        }
        return p
    }

    fun N17580aff12(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[31] == null) {
            p = 0.0
        } else if ((i[31] as Double?)!!.toDouble() <= 0.060517) {
            p = 0.0
        } else if ((i[31] as Double?)!!.toDouble() > 0.060517) {
            p = 1.0
        }
        return p
    }

    fun N5d63158013(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[1] == null) {
            p = 1.0
        } else if ((i[1] as Double?)!!.toDouble() <= 41.348185) {
            p = N436c513b14(i)
        } else if ((i[1] as Double?)!!.toDouble() > 41.348185) {
            p = N1d9973a723(i)
        }
        return p
    }

    fun N436c513b14(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[1] == null) {
            p = 0.0
        } else if ((i[1] as Double?)!!.toDouble() <= 4.916364) {
            p = 0.0
        } else if ((i[1] as Double?)!!.toDouble() > 4.916364) {
            p = N24a480e215(i)
        }
        return p
    }

    fun N24a480e215(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[16] == null) {
            p = 0.0
        } else if ((i[16] as Double?)!!.toDouble() <= 0.457846) {
            p = 0.0
        } else if ((i[16] as Double?)!!.toDouble() > 0.457846) {
            p = N73fc697b16(i)
        }
        return p
    }

    fun N73fc697b16(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() <= 12.38261) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() > 12.38261) {
            p = N36f6ad9117(i)
        }
        return p
    }

    fun N36f6ad9117(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() <= 96.390521) {
            p = N17b2c7fd18(i)
        } else if ((i[0] as Double?)!!.toDouble() > 96.390521) {
            p = N1791e67919(i)
        }
        return p
    }

    fun N17b2c7fd18(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[32] == null) {
            p = 0.0
        } else if ((i[32] as Double?)!!.toDouble() <= 1.341266) {
            p = 0.0
        } else if ((i[32] as Double?)!!.toDouble() > 1.341266) {
            p = 1.0
        }
        return p
    }

    fun N1791e67919(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[5] == null) {
            p = 0.0
        } else if ((i[5] as Double?)!!.toDouble() <= 4.342662) {
            p = N736e24120(i)
        } else if ((i[5] as Double?)!!.toDouble() > 4.342662) {
            p = N42f5d1321(i)
        }
        return p
    }

    fun N736e24120(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 0.0
        } else if ((i[2] as Double?)!!.toDouble() <= 14.860408) {
            p = 0.0
        } else if ((i[2] as Double?)!!.toDouble() > 14.860408) {
            p = 1.0
        }
        return p
    }

    fun N42f5d1321(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[7] == null) {
            p = 1.0
        } else if ((i[7] as Double?)!!.toDouble() <= 3.872909) {
            p = N5a3ef4422(i)
        } else if ((i[7] as Double?)!!.toDouble() > 3.872909) {
            p = 1.0
        }
        return p
    }

    fun N5a3ef4422(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[27] == null) {
            p = 1.0
        } else if ((i[27] as Double?)!!.toDouble() <= 1.249333) {
            p = 1.0
        } else if ((i[27] as Double?)!!.toDouble() > 1.249333) {
            p = 0.0
        }
        return p
    }

    fun N1d9973a723(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[27] == null) {
            p = 0.0
        } else if ((i[27] as Double?)!!.toDouble() <= 1.898341) {
            p = N2d0dbebf24(i)
        } else if ((i[27] as Double?)!!.toDouble() > 1.898341) {
            p = 1.0
        }
        return p
    }

    fun N2d0dbebf24(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[6] == null) {
            p = 1.0
        } else if ((i[6] as Double?)!!.toDouble() <= 4.763399) {
            p = N5334a9c825(i)
        } else if ((i[6] as Double?)!!.toDouble() > 4.763399) {
            p = 0.0
        }
        return p
    }

    fun N5334a9c825(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() <= 22.131891) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() > 22.131891) {
            p = 0.0
        }
        return p
    }

    fun N4977b52526(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() <= 121.466424) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() > 121.466424) {
            p = 1.0
        }
        return p
    }

    fun N1fa5e3e727(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() <= 32.49493) {
            p = N5f9d5eea28(i)
        } else if ((i[2] as Double?)!!.toDouble() > 32.49493) {
            p = N5dbb90a230(i)
        }
        return p
    }

    fun N5f9d5eea28(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[7] == null) {
            p = 1.0
        } else if ((i[7] as Double?)!!.toDouble() <= 18.921993) {
            p = 1.0
        } else if ((i[7] as Double?)!!.toDouble() > 18.921993) {
            p = N2911d63829(i)
        }
        return p
    }

    fun N2911d63829(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() <= 394.730044) {
            p = 0.0
        } else if ((i[0] as Double?)!!.toDouble() > 394.730044) {
            p = 2.0
        }
        return p
    }

    fun N5dbb90a230(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[1] == null) {
            p = 2.0
        } else if ((i[1] as Double?)!!.toDouble() <= 64.079023) {
            p = N6d31fa2931(i)
        } else if ((i[1] as Double?)!!.toDouble() > 64.079023) {
            p = N6a09cdb37(i)
        }
        return p
    }

    fun N6d31fa2931(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[4] == null) {
            p = 2.0
        } else if ((i[4] as Double?)!!.toDouble() <= 30.208373) {
            p = N586e936b32(i)
        } else if ((i[4] as Double?)!!.toDouble() > 30.208373) {
            p = N300dfffc36(i)
        }
        return p
    }

    fun N586e936b32(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[11] == null) {
            p = 2.0
        } else if ((i[11] as Double?)!!.toDouble() <= 2.977315) {
            p = 2.0
        } else if ((i[11] as Double?)!!.toDouble() > 2.977315) {
            p = N7f692e4833(i)
        }
        return p
    }

    fun N7f692e4833(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[0] == null) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() <= 363.032216) {
            p = 1.0
        } else if ((i[0] as Double?)!!.toDouble() > 363.032216) {
            p = N7ed68f8734(i)
        }
        return p
    }

    fun N7ed68f8734(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 2.0
        } else if ((i[2] as Double?)!!.toDouble() <= 71.262558) {
            p = N1cd4d2c635(i)
        } else if ((i[2] as Double?)!!.toDouble() > 71.262558) {
            p = 2.0
        }
        return p
    }

    fun N1cd4d2c635(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[8] == null) {
            p = 1.0
        } else if ((i[8] as Double?)!!.toDouble() <= 14.968242) {
            p = 1.0
        } else if ((i[8] as Double?)!!.toDouble() > 14.968242) {
            p = 2.0
        }
        return p
    }

    fun N300dfffc36(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[64] == null) {
            p = 2.0
        } else if ((i[64] as Double?)!!.toDouble() <= 10.270859) {
            p = 2.0
        } else if ((i[64] as Double?)!!.toDouble() > 10.270859) {
            p = 1.0
        }
        return p
    }

    fun N6a09cdb37(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[10] == null) {
            p = 2.0
        } else if ((i[10] as Double?)!!.toDouble() <= 2.49303) {
            p = 2.0
        } else if ((i[10] as Double?)!!.toDouble() > 2.49303) {
            p = N4cc0813f38(i)
        }
        return p
    }

    fun N4cc0813f38(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[21] == null) {
            p = 1.0
        } else if ((i[21] as Double?)!!.toDouble() <= 4.561238) {
            p = 1.0
        } else if ((i[21] as Double?)!!.toDouble() > 4.561238) {
            p = N76b8388339(i)
        }
        return p
    }

    fun N76b8388339(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[1] == null) {
            p = 2.0
        } else if ((i[1] as Double?)!!.toDouble() <= 120.573003) {
            p = N2995cbb740(i)
        } else if ((i[1] as Double?)!!.toDouble() > 120.573003) {
            p = 1.0
        }
        return p
    }

    fun N2995cbb740(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[3] == null) {
            p = 1.0
        } else if ((i[3] as Double?)!!.toDouble() <= 37.862307) {
            p = 1.0
        } else if ((i[3] as Double?)!!.toDouble() > 37.862307) {
            p = 2.0
        }
        return p
    }

    fun N2bd1cc1a41(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[64] == null) {
            p = 1.0
        } else if ((i[64] as Double?)!!.toDouble() <= 12.193029) {
            p = N63d0792642(i)
        } else if ((i[64] as Double?)!!.toDouble() > 12.193029) {
            p = N549b39ff43(i)
        }
        return p
    }

    fun N63d0792642(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[2] == null) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() <= 47.476846) {
            p = 1.0
        } else if ((i[2] as Double?)!!.toDouble() > 47.476846) {
            p = 2.0
        }
        return p
    }

    fun N549b39ff43(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[18] == null) {
            p = 2.0
        } else if ((i[18] as Double?)!!.toDouble() <= 12.734885) {
            p = 2.0
        } else if ((i[18] as Double?)!!.toDouble() > 12.734885) {
            p = N53d498a844(i)
        }
        return p
    }

    fun N53d498a844(i: Array<Double>): Double {
        var p = Double.NaN
        if (i[25] == null) {
            p = 2.0
        } else if ((i[25] as Double?)!!.toDouble() <= 14.57448) {
            p = 2.0
        } else if ((i[25] as Double?)!!.toDouble() > 14.57448) {
            p = 1.0
        }
        return p
    }
}
