package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.example.killer.database.DateConverter;
import java.util.Date;

@Entity(tableName = "family_members")
@TypeConverters({DateConverter.class})
public class FamilyMember {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int familyId;
    private int userId;
    private String role; // PARENT, CHILD, OTHER
    private Date joinDate;
    private boolean isInvited;
    private String invitedEmail;
    private Date invitedAt;

    // Конструктор без аргументов для Room
    public FamilyMember() {
        this.joinDate = new Date();
        this.isInvited = false;
    }

    @Ignore
    public FamilyMember(int familyId, int userId, String role) {
        this.familyId = familyId;
        this.userId = userId;
        this.role = role;
        this.joinDate = new Date();
        this.isInvited = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFamilyId() { return familyId; }
    public void setFamilyId(int familyId) { this.familyId = familyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    public boolean isInvited() { return isInvited; }
    public void setInvited(boolean invited) { isInvited = invited; }

    public String getInvitedEmail() { return invitedEmail; }
    public void setInvitedEmail(String invitedEmail) { this.invitedEmail = invitedEmail; }

    public Date getInvitedAt() { return invitedAt; }
    public void setInvitedAt(Date invitedAt) { this.invitedAt = invitedAt; }
}