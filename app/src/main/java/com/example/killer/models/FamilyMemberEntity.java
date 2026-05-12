package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "family_members")
public class FamilyMemberEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String role;
    private int points;
    private String email;
    private boolean isInvited;
    private Date joinDate;

    public FamilyMemberEntity(String name, String role, String email) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.points = 0;
        this.isInvited = false;
        this.joinDate = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isInvited() { return isInvited; }
    public void setInvited(boolean invited) { isInvited = invited; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
}