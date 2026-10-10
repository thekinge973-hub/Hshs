package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccentRecord
import com.example.data.model.Assignment
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityPost
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<Assignment>)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)

    @Query("UPDATE assignments SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM assignments WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE persona = :persona ORDER BY timestamp ASC")
    fun getMessagesForPersona(persona: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE persona = :persona")
    suspend fun clearMessagesForPersona(persona: String)
}

@Dao
interface AccentRecordDao {
    @Query("SELECT * FROM accent_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AccentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AccentRecord): Long

    @Query("SELECT AVG(overallScore) FROM accent_records")
    fun getAverageScore(): Flow<Double?>
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<CommunityPost>)

    @Query("UPDATE community_posts SET likes = likes + 1 WHERE id = :id")
    suspend fun likePost(id: Long)
}

@Dao
interface EnglishChallengeDao {
    @Query("SELECT * FROM english_challenges ORDER BY isCompleted ASC, id ASC")
    fun getAllChallenges(): Flow<List<com.example.data.model.EnglishChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: com.example.data.model.EnglishChallenge): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<com.example.data.model.EnglishChallenge>)

    @Query("UPDATE english_challenges SET isCompleted = 1, userScore = :score WHERE id = :id")
    suspend fun completeChallenge(id: Long, score: Int)
}
