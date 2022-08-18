package com.example.myruns2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.w3c.dom.Comment

@Database(entities = [RunHistory::class], version = 5)
abstract class RunHistoryDatabase:RoomDatabase() {
    abstract  val runHistoryDatabaseDao: RunHistoryDatabaseDao

    companion object{

        @Volatile
        private var INSTANCE : RunHistoryDatabase? = null

        fun getInstance(context:Context):RunHistoryDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance==null){
                    instance= Room.databaseBuilder(context.applicationContext,
                    RunHistoryDatabase::class.java, "run_history_db").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }

        }
    }
}
//@Database(entities = [Comment::class], version=1)
//abstract class CommentDatabase: RoomDatabase() {
//    //when room produce lots of code for us, we ned to say abstract.
//    abstract val commentDatabaseDao: CommentDatabaseDao
//
//    companion object{
//
//        @Volatile
//        private var INSTANCE : CommentDatabase? =null
//
//        fun getInstance(context: Context): CommentDatabase{
//            synchronized(this){
//                var instance = INSTANCE
//                if(instance==null){
//                    instance= Room.databaseBuilder(context.applicationContext,
//                        CommentDatabase::class.java, "comment_db"
//                    ).build()
//                    INSTANCE = instance
//
//                    //context means the activity??
//                }
//                return instance
//            }
//        }
//    }
//}