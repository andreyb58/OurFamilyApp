package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.Family;
import java.util.List;

@Dao
public interface FamilyDao {
    @Insert
    void insert(Family family);

    @Update
    void update(Family family);

    @Delete
    void delete(Family family);

    @Query("SELECT * FROM families WHERE id = :id")
    Family getFamilyById(int id);

    @Query("SELECT * FROM families WHERE createdBy = :userId")
    Family getFamilyByUser(int userId);

    @Query("SELECT * FROM families WHERE inviteCode = :code")
    Family getFamilyByInviteCode(String code);

    @Query("UPDATE families SET inviteCode = :code WHERE id = :id")
    void updateInviteCode(int id, String code);
}