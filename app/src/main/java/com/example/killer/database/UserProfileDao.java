package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.UserProfile;

/**
 * Data Access Object (DAO) для работы с таблицей user_profiles
 * Определяет методы для работы с профилями пользователей
 */
@Dao
public interface UserProfileDao {

    /**
     * Вставка нового профиля пользователя
     * @param profile Объект профиля для сохранения
     */
    @Insert
    void insert(UserProfile profile);

    /**
     * Обновление существующего профиля
     * @param profile Объект профиля с обновленными данными
     */
    @Update
    void update(UserProfile profile);

    /**
     * Получение профиля пользователя по ID
     * @param userId ID пользователя
     * @return Профиль пользователя или null если не найден
     */
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    UserProfile getUserProfile(String userId);

    /**
     * Добавление очков пользователю
     * @param userId ID пользователя
     * @param points Количество очков для добавления (может быть отрицательным для списания)
     */
    @Query("UPDATE user_profiles SET totalPoints = totalPoints + :points WHERE userId = :userId")
    void addPoints(String userId, int points);

    /**
     * Установка текущего календаря пользователя
     * @param userId ID пользователя
     * @param calendarId ID календаря
     */
    @Query("UPDATE user_profiles SET currentCalendarId = :calendarId WHERE userId = :userId")
    void setCurrentCalendar(String userId, String calendarId);

    /**
     * Обновление даты последней синхронизации
     * @param userId ID пользователя
     * @param date Временная метка последней синхронизации
     */
    @Query("UPDATE user_profiles SET lastSyncDate = :date WHERE userId = :userId")
    void updateLastSync(String userId, long date);

    /**
     * Получение профиля для синхронизации
     * @param userId ID пользователя
     * @return Профиль пользователя с актуальными данными
     */
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    UserProfile getProfileForSync(String userId);

    /**
     * Включение/выключение автосинхронизации
     * @param userId ID пользователя
     * @param enabled Статус автосинхронизации
     */
    @Query("UPDATE user_profiles SET syncEnabled = :enabled WHERE userId = :userId")
    void setSyncEnabled(String userId, boolean enabled);

    /**
     * Получение количества очков пользователя
     * @param userId ID пользователя
     * @return Количество очков или 0 если профиль не найден
     */
    @Query("SELECT COALESCE(totalPoints, 0) FROM user_profiles WHERE userId = :userId")
    int getUserPoints(String userId);

    /**
     * Получение текущего календаря пользователя
     * @param userId ID пользователя
     * @return ID текущего календаря или null
     */
    @Query("SELECT currentCalendarId FROM user_profiles WHERE userId = :userId")
    String getCurrentCalendarId(String userId);

    /**
     * Проверка включена ли автосинхронизация
     * @param userId ID пользователя
     * @return true если автосинхронизация включена
     */
    @Query("SELECT syncEnabled FROM user_profiles WHERE userId = :userId")
    boolean isSyncEnabled(String userId);

    /**
     * Удаление профиля пользователя
     * @param userId ID пользователя
     */
    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    void deleteUserProfile(String userId);

    /**
     * Получение всех профилей, требующих синхронизации
     * @return Список профилей с включенной синхронизацией
     */
    @Query("SELECT * FROM user_profiles WHERE syncEnabled = 1")
    java.util.List<UserProfile> getProfilesForSync();
}