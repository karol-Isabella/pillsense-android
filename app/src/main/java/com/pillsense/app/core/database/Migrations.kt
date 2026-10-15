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
}
