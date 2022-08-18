package com.example.myruns2

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import java.lang.IllegalArgumentException

class RunHistoryViewModel (private val repository: RunHistoryRepository):ViewModel(){
    val allRunHistoryLiveData:LiveData<List<RunHistory>> = repository.allRunHistory.asLiveData()

    fun insert(runHistory: RunHistory){
        repository.insert(runHistory)
    }
    fun deleteFirst(){
        val runHistoryList = allRunHistoryLiveData.value
        if(runHistoryList!= null && runHistoryList.size >0){
            val id = runHistoryList[0].id
            repository.delete(id)
        }
    }
    fun deleteOne(id:Long){
//            val runHistoryList = allRunHistoryLiveData.value
//            if(runHistoryList!= null && runHistoryList.size >0){
//                val id = runHistoryList[0].id
        repository.delete(id)
    }
    fun deleteAll(){
        val runHistoryList = allRunHistoryLiveData.value
        if(runHistoryList!= null && runHistoryList.size >0)
            repository.deleteAll()

    }

}


class RunHistoryViewModelFactory(private val repository: RunHistoryRepository): ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RunHistoryViewModel::class.java))
            return RunHistoryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



//class CommentViewModel (private val repository: CommentRepository): ViewModel(){
//    val allCommentsLiveData: LiveData<List<Comment>> = repository.allComments.asLiveData()
    //this gives us Live access to all the data in the DB ->powerful


//    fun insert(comment:Comment){
//        repository.insert(comment)
//    }

//    fun deleteFirst(){
//        val commentList = allCommentsLiveData.value
//        if(commentList!= null && commentList.size >0){
//            val id = commentList[0].id
//            repository.delete(id)
//        }
//    }
//    fun deleteAll(){
//        val commentList = allCommentsLiveData.value
//        if(commentList!= null && commentList.size >0)
//            repository.deleteAll()
//
//    }
//}