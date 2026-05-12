package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "tasks")
@TypeConverters(DateConverter.class)
public class Task {

    @PrimaryKey
    private int id;

    private String title;
    private String description;
    private int points;
    private String assignedTo;
    private String assignedToId;
    private boolean isCompleted;
    private Date dueDate;
    private Date completedDate;
    private boolean pointsAwarded;
    private boolean synced;
    private String createdBy;

    // Конструктор без аргументов для Room (основной)
    public Task() {}

    // @Ignore — Room не будет использовать этот конструктор
    @Ignore
    public Task(String title, String description, int points,
                String assignedTo, String assignedToId) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.assignedTo = assignedTo;
        this.assignedToId = assignedToId;
        this.isCompleted = false;
        this.pointsAwarded = false;
        this.dueDate = new Date();
        this.synced = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToId() { return assignedToId; }
    public void setAssignedToId(String assignedToId) { this.assignedToId = assignedToId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getCompletedDate() { return completedDate; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }

    public boolean isPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(boolean pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
