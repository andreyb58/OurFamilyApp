package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "rewards")
@TypeConverters(DateConverter.class)
public class Reward {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private int cost;
    private boolean isClaimed;
    private String claimedBy;
    private String claimedById; // Добавляем ID пользователя
    private Date claimDate;
    private boolean synced; // Синхронизировано ли с сервером
    private String createdBy; // Кто создал награду

    public Reward() {}

    @Ignore
    public Reward(String title, String description, int cost) {
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.isClaimed = false;
        this.synced = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public boolean isClaimed() { return isClaimed; }
    public void setClaimed(boolean claimed) { isClaimed = claimed; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }

    public String getClaimedById() { return claimedById; }
    public void setClaimedById(String claimedById) { this.claimedById = claimedById; }

    public Date getClaimDate() { return claimDate; }
    public void setClaimDate(Date claimDate) { this.claimDate = claimDate; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}