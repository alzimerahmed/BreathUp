package com.smokingtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE smoking_entries ADD COLUMN isResisted INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE smoking_entries ADD COLUMN cravingIntensity INTEGER")
        db.execSQL("ALTER TABLE smoking_entries ADD COLUMN outcomeNote TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_smoking_entries_timestamp ON smoking_entries(timestamp)")
    }
}

@Database(entities = [SmokingEntryEntity::class], version = 4, exportSchema = true)
abstract class SmokingDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
}

