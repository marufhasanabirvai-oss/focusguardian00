package com.focusguardian.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focusguardian.app.data.local.dao.*
import com.focusguardian.app.data.local.entity.*

@Database(
    entities = [
        BlockedDomainEntity::class,
        BlockedWordEntity::class,
        BlockedImageEntity::class,
        BlockLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun domainDao(): DomainDao
    abstract fun wordDao(): WordDao
    abstract fun imageDao(): ImageDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focus_guardian_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
