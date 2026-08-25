package com.flosi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.flosi.app.data.local.entity.SalaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {
    @Query("SELECT * FROM salary_profiles WHERE active=1 ORDER BY id LIMIT 1")
    fun observeActive(): Flow<SalaryEntity?>

    @Query("SELECT * FROM salary_profiles WHERE id=:id LIMIT 1")
    suspend fun get(id: Long): SalaryEntity?

    @Insert
    suspend fun insert(entity: SalaryEntity): Long

    @Update
    suspend fun update(entity: SalaryEntity)

    @Query("UPDATE salary_profiles SET active=0, updatedAt=:now WHERE active=1")
    suspend fun deactivateAll(now: Long = System.currentTimeMillis())
}
