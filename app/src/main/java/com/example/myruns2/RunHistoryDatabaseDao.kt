package com.example.myruns2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow



@Dao
interface RunHistoryDatabaseDao  {

    @Insert
    suspend fun insertRunHistory(runHistory: RunHistory)

    @Query("SELECT * FROM run_history_table")
    fun getAllRunHistory(): Flow<List<RunHistory>>

    @Query("DELETE FROM run_history_table")
    suspend fun deleteAll():Integer

    @Query("DELETE FROM run_history_table WHERE id = :key")
    suspend fun deleteRunHistory(key:Long):Integer

}