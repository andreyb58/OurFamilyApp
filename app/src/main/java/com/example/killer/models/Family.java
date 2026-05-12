package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "families")
@TypeConverters({DateConverter.class})
public class Family {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String inviteCode;
    private int createdBy;
    private Date createdAt;
    private Date updatedAt;
    private String description;
    private String color;

    // Конструктор без аргументов для Room
    public Family() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Ignore
    public Family(String name, int createdBy, String description) {
        this.name = name;
        this.createdBy = createdBy;
        this.description = description;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.color = "#" + String.format("%06x", (int)(Math.random() * 0x1000000));
        generateInviteCode();
    }

    private void generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        this.inviteCode = code.toString();
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}