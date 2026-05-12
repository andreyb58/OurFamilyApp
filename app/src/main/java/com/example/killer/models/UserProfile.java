package com.example.killer.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

/**
 * Модель профиля пользователя для локального хранения
 * Entity для Room Database, представляет таблицу user_profiles
 * Отдельная от основной модели User для разделения локальных и серверных данных
 */
@Entity(tableName = "user_profiles")
@TypeConverters({DateConverter.class})
public class UserProfile {
    @PrimaryKey
    @NonNull
    private String userId; // Firebase UID или другой уникальный идентификатор

    // Основная информация
    private String name; // Имя пользователя
    private String email; // Email пользователя
    private String profileImageUrl; // URL аватара

    // Статистика и прогресс
    private int totalPoints; // Общее количество очков
    private String currentCalendarId; // ID текущего выбранного календаря

    // Настройки синхронизации
    private Date lastSyncDate; // Дата последней синхронизации
    private boolean syncEnabled; // Включена ли автосинхронизация

    // Дополнительные данные
    private String themeColor; // Цветовая тема приложения
    private boolean notificationsEnabled; // Включены ли уведомления
    private String language; // Язык интерфейса

    /**
     * Конструктор профиля пользователя
     * @param userId Уникальный идентификатор пользователя
     * @param name Имя пользователя
     * @param email Email пользователя
     */
    public UserProfile() {}

    @Ignore
    public UserProfile(@NonNull String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.totalPoints = 0; // Начинаем с 0 очков
        this.syncEnabled = true; // Автосинхронизация включена по умолчанию
        this.lastSyncDate = new Date(); // Текущая дата
        this.notificationsEnabled = true; // Уведомления включены по умолчанию
        this.language = "ru"; // Русский язык по умолчанию
        this.themeColor = "#2196F3"; // Синяя тема по умолчанию
    }

    // ========== ГЕТТЕРЫ И СЕТТЕРЫ ==========

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getCurrentCalendarId() { return currentCalendarId; }
    public void setCurrentCalendarId(String currentCalendarId) { this.currentCalendarId = currentCalendarId; }

    public Date getLastSyncDate() { return lastSyncDate; }
    public void setLastSyncDate(Date lastSyncDate) { this.lastSyncDate = lastSyncDate; }

    public boolean isSyncEnabled() { return syncEnabled; }
    public void setSyncEnabled(boolean syncEnabled) { this.syncEnabled = syncEnabled; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    /**
     * Добавление очков пользователю
     * @param points Количество очков для добавления (может быть отрицательным для списания)
     */
    public void addPoints(int points) {
        this.totalPoints += points;
        if (this.totalPoints < 0) {
            this.totalPoints = 0; // Не допускаем отрицательных очков
        }
    }

    /**
     * Проверка, достаточно ли очков для покупки
     * @param requiredPoints Требуемое количество очков
     * @return true если очков достаточно, false в противном случае
     */
    public boolean hasEnoughPoints(int requiredPoints) {
        return this.totalPoints >= requiredPoints;
    }

    /**
     * Списание очков с проверкой
     * @param points Количество очков для списания
     * @return true если списание успешно, false если недостаточно очков
     */
    public boolean deductPoints(int points) {
        if (hasEnoughPoints(points)) {
            this.totalPoints -= points;
            return true;
        }
        return false;
    }

    /**
     * Обновление даты последней синхронизации
     */
    public void updateLastSync() {
        this.lastSyncDate = new Date();
    }

    /**
     * Получение форматированной строки с очками
     * @return Строка вида "1,250 очков"
     */
    public String getFormattedPoints() {
        return String.format("%,d", totalPoints) + " очков";
    }
}