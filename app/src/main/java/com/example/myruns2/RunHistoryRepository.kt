package com.example.myruns2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RunHistoryRepository(private val runHistoryDatabaseDao: RunHistoryDatabaseDao) {
    val allRunHistory : Flow<List<RunHistory>> = runHistoryDatabaseDao.getAllRunHistory()

    fun insert(runHistory: RunHistory){
        CoroutineScope(IO).launch{
            runHistoryDatabaseDao.insertRunHistory(runHistory)
        }
    }

    fun delete(id:Long){
        CoroutineScope(IO).launch{
            runHistoryDatabaseDao.deleteRunHistory(id)
        }
    }
    fun deleteAll(){
        CoroutineScope(IO).launch{
            runHistoryDatabaseDao.deleteAll()
        }
    }
}


//class CommentRepository(private val commentDatabaseDao: CommentDatabaseDao) {
//
//    val allComments: Flow<List<Comment>> = commentDatabaseDao.getAllComments()
//
//    fun insert(comment:Comment){
//        CoroutineScope(IO).launch{
//            commentDatabaseDao.insertComment(comment)
//        }
//    }
//
//    fun delete(id: Long){
//        CoroutineScope(Dispatchers.IO).launch{
//            commentDatabaseDao.deleteComment(id)
//        }
//    }
//
//    fun deleteAll(){
//        CoroutineScope(Dispatchers.IO).launch{
//            commentDatabaseDao.deleteAll()
//        }
//    }
//
//
//}