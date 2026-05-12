package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.Calendar;
import java.util.List;

@Dao
public interface CalendarDao {
    @Insert
    void insert(Calendar calendar);

    @Update
    void update(Calendar calendar);

    @Delete
    void delete(Calendar calendar);

    @Query("SELECT * FROM calendars WHERE ownerId = :ownerId ORDER BY isDefault DESC, name ASC")
    List<Calendar> getUserCalendars(String ownerId);

    @Query("SELECT * FROM calendars WHERE id = :id")
    Calendar getCalendarById(int id);

    @Query("SELECT * FROM calendars WHERE isDefault = 1 AND ownerId = :ownerId LIMIT 1")
    Calendar getDefaultCalendar(String ownerId);

    @Query("UPDATE calendars SET isDefault = 0 WHERE ownerId = :ownerId")
    void clearDefault(String ownerId);

    @Query("UPDATE calendars SET isDefault = 1 WHERE id = :id")
    void setAsDefault(int id);

    @Query("SELECT * FROM calendars WHERE synced = 0")
    List<Calendar> getUnsyncedCalendars();

    @Query("UPDATE calendars SET synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    @Query("SELECT COUNT(*) FROM calendars WHERE ownerId = :ownerId")
    int getCalendarCount(String ownerId);

    @Query("UPDATE calendars SET color = :color WHERE id = :id")
    void updateCalendarColor(int id, String color);

    @Query("UPDATE calendars SET name = :name WHERE id = :id")
    void updateCalendarName(int id, String name);

    @Query("SELECT * FROM calendars WHERE name = :name AND ownerId = :ownerId LIMIT 1")
    Calendar getCalendarByName(String name, String ownerId);
}