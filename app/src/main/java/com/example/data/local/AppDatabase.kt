package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AccentRecord
import com.example.data.model.Assignment
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityPost
import com.example.data.model.EnglishChallenge

@Database(
    entities = [
        Assignment::class,
        ChatMessage::class,
        AccentRecord::class,
        CommunityPost::class,
        EnglishChallenge::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assignmentDao(): AssignmentDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun accentRecordDao(): AccentRecordDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun challengeDao(): EnglishChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amideast_access.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
