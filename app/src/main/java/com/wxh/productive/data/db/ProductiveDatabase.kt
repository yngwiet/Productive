package com.wxh.productive.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wxh.productive.data.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class ProductiveDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    private class TaskDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Timber.d("onOpen of database")
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Timber.d("onCreate of database")
            INSTANCE?.let { database ->
                scope.launch {
                    sampleTasks.forEach {
                        database.taskDao().insertTask(it)
                    }
                }
            }
        }
    }

    companion object {

        val sampleTasks: List<Task> = listOf(
            Task("Learn", "Learn Kotlin and Android!"),
            Task("Play", "Play!")
        )

        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ProductiveDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): ProductiveDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductiveDatabase::class.java,
                    "task_database"
                ).addCallback(
                    TaskDatabaseCallback(
                        scope
                    )
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}