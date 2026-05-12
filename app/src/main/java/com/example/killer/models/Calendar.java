package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "calendars")
@TypeConverters({DateConverter.class})
public class Calendar {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String color;
    private String ownerId;
    private boolean isDefault;
    private Date createdAt;
    private boolean synced;

    public Calendar() {}

    @Ignore
    public Calendar(String name, String color, String ownerId) {
        this.name = name;
        this.color = color;
        this.ownerId = ownerId;
        this.isDefault = false;
        this.createdAt = new Date();
        this.synced = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}