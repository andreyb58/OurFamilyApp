package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.ChatMessage;
import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessage message);

    @Update
    void update(ChatMessage message);

    @Delete
    void delete(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE familyId = :familyId ORDER BY timestamp ASC")
    List<ChatMessage> getFamilyMessages(int familyId);

    @Query("SELECT * FROM chat_messages WHERE familyId = :familyId AND read = 0")
    List<ChatMessage> getUnreadMessages(int familyId);

    @Query("UPDATE chat_messages SET read = 1 WHERE familyId = :familyId AND read = 0")
    void markAsRead(int familyId);

    // ИСПРАВЛЕНО: senderId → userId (соответствует полю в модели ChatMessage)
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesByUser(int userId);
}
