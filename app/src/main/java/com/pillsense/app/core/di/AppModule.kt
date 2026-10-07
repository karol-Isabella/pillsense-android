package com.pillsense.app.core.di

import android.content.Context
import androidx.room.Room
import com.pillsense.app.core.database.PillSenseDatabase
import com.pillsense.app.core.security.SecureStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context, secure: SecureStore): PillSenseDatabase {
        System.loadLibrary("sqlcipher")
        return Room.databaseBuilder(context, PillSenseDatabase::class.java, "pillsense-secure.db")
            .openHelperFactory(SupportOpenHelperFactory(secure.databaseKey())).build()
    }
    @Provides fun dao(database: PillSenseDatabase) = database.dao()
}
