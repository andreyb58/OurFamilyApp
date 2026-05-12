package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.example.killer.database.DateConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "chat_messages")
@TypeConverters({DateConverter.class})
public class ChatMessage {
    @PrimaryKey
    private int id;

    private int familyId;

    // ИСПРАВЛЕНО: унифицированы поля — userId/userName (соответствует API)
    private int userId;
    private String userName;
    private String message;
    private Date timestamp;
    private boolean read;
    // Строковая дата с сервера (если timestamp не распарсился)
    @androidx.room.Ignore
    private String createdAt;

    public ChatMessage() {
        this.timestamp = new Date();
        this.read = false;
    }

    @Ignore
    public ChatMessage(int familyId, int userId, String userName, String message) {
        this.familyId = familyId;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.timestamp = new Date();
        this.read = false;
    }

    // ---- Геттеры / Сеттеры ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFamilyId() { return familyId; }
    public void setFamilyId(int familyId) { this.familyId = familyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        // Пытаемся распарсить в Date для getFormattedTime()
        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                this.timestamp = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(createdAt);
            } catch (Exception ignored) {}
        }
    }

    public String getFormattedTime() {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp);
    }

    // Алиасы для обратной совместимости со старым FamilyChatAdapter
    public int getSenderId()   { return userId; }
    public String getSenderName() { return userName; }
}
