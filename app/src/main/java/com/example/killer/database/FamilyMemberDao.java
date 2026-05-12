package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.FamilyMember;
import java.util.List;

@Dao
public interface FamilyMemberDao {
    @Insert
    void insert(FamilyMember member);

    @Update
    void update(FamilyMember member);

    @Delete
    void delete(FamilyMember member);

    @Query("SELECT * FROM family_members WHERE familyId = :familyId")
    List<FamilyMember> getFamilyMembers(int familyId);

    @Query("SELECT * FROM family_members WHERE userId = :userId")
    FamilyMember getMemberByUserId(int userId);

    @Query("SELECT * FROM family_members WHERE familyId = :familyId AND userId = :userId")
    FamilyMember getMember(int familyId, int userId);

    @Query("DELETE FROM family_members WHERE familyId = :familyId")
    void deleteAllFamilyMembers(int familyId);
}