package com.pillsense.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE medications ADD COLUMN unitsRemaining INTEGER")
            database.execSQL("ALTER TABLE medications ADD COLUMN unitsPerDose INTEGER NOT NULL DEFAULT 1")
        }
    }
    
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ai_cache` (`owner` TEXT NOT NULL, `summary` TEXT NOT NULL, `dataHash` TEXT NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`owner`))")
        }
    }
}
