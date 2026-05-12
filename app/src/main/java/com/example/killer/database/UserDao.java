package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE familyId = :familyId")
    List<User> getFamilyMembers(int familyId);

    @Query("UPDATE users SET lastLogin = :date WHERE id = :id")
    void updateLastLogin(int id, java.util.Date date);

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    List<User> getUsersByIds(List<Integer> userIds);
}