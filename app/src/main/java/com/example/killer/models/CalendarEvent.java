package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "calendar_events")
@TypeConverters({DateConverter.class})
public class CalendarEvent {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private Date eventDate;  // Полная дата события
    private String time;     // Время события
    private String calendarId; // К какому календарю относится
    private String assignedTo; // Кому назначено
    private boolean isCompleted;
    private Date createdAt;
    private String createdBy;
    private boolean synced;  // Синхронизировано ли с сервером

    public CalendarEvent() {}

    @Ignore
    public CalendarEvent(String title, String description, Date eventDate,
                         String time, String calendarId, String assignedTo) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.time = time;
        this.calendarId = calendarId;
        this.assignedTo = assignedTo;
        this.isCompleted = false;
        this.createdAt = new Date();
        this.synced = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCalendarId() { return calendarId; }
    public void setCalendarId(String calendarId) { this.calendarId = calendarId; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}