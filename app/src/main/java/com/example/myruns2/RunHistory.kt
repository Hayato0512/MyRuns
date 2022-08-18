package com.example.myruns2

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="run_history_table")
data class RunHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: Int = 0,

    @ColumnInfo(name = "activity_type")
var activityType: Int = 0,
//changed to Int data type
@ColumnInfo(name = "date")
var date: String = "",

@ColumnInfo(name = "time")
var time: String = "",

    @ColumnInfo(name = "duration")
    var duration: Int = 0,

    @ColumnInfo(name = "distance")
    var distance: Float =0f,
    //changed from Int to Float

    @ColumnInfo(name = "distanceUnit")
    var distanceUnit: Int =0,

    @ColumnInfo(name = "calories")
    var calories: Float = 0f,
    //changed from Int to Float

    @ColumnInfo(name = "heart_rate")
    var heartRate: Int = 0,

    @ColumnInfo(name = "comment")
    var comment: String = "",

    @ColumnInfo(name = "avgPace")
    var avgPace: Float = 0f,

    @ColumnInfo(name = "avgSpeed")
    var avgSpeed: Float = 0f,

    @ColumnInfo(name = "climb")
    var climb : Float = 0f,

    @ColumnInfo(name = "locationList")
    var locationList :ByteArray = ByteArray(100),
    //let's see.

)
