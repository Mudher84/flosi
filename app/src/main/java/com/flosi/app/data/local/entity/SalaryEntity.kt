package com.flosi.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "salary_profiles", indices = [Index("active")])
data class SalaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "الراتب",
    val amount: Long,
    val currency: String = "IQD",
    val frequency: String = "monthly",
    val payday: Int = 1,
    val nextPayAt: Long,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
