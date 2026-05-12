package com.example.killer.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.example.killer.database.DateConverter;
import java.util.Date;

/**
 * Модель пользователя приложения
 * Entity для Room Database, представляет таблицу users
 */
@Entity(tableName = "users")
@TypeConverters({DateConverter.class}) // Конвертер для дат
public class User {
    @PrimaryKey
    private int id; // Уникальный идентификатор пользователя

    private String email; // Email пользователя (уникальный)
    private String name; // Имя пользователя
    private String profileImageUrl; // URL аватара
    private int totalPoints; // Общее количество очков
    private String role; // Роль: ADMIN, PARENT, CHILD
    private int familyId; // ID семьи (если есть)

    // Даты
    private Date createdAt; // Дата создания аккаунта
    private Date updatedAt; // Дата последнего обновления
    private Date lastLogin; // Дата последнего входа

    // Статусы
    private boolean active; // Активен ли аккаунт
    private String status; // Статус: online, offline, away
    private String phone; // Телефон (опционально)
    private String avatarColor; // Цвет аватара (если нет фото)

    // Локальные поля (не сохраняются в БД и не отправляются на сервер)
    private transient String password; // Пароль (только для локальной проверки)
    private transient String jwtToken; // JWT токен (только для сессии)

    /**
     * Конструктор по умолчанию для Room
     */
    public User() {
        this.createdAt = new Date(); // Текущая дата создания
        this.updatedAt = new Date(); // Текущая дата обновления
        this.lastLogin = new Date(); // Текущая дата входа
        this.active = true; // По умолчанию активен
        this.status = "offline"; // По умолчанию офлайн
        this.avatarColor = generateRandomColor(); // Генерируем случайный цвет
    }

    /**
     * Конструктор для регистрации нового пользователя
     */
    @Ignore
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.totalPoints = 0; // Начинаем с 0 очков
        this.role = "PARENT"; // Роль по умолчанию
        this.active = true; // Активный аккаунт
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.lastLogin = new Date();
        this.status = "offline";
        this.avatarColor = generateRandomColor(); // Цвет аватара
    }

    /**
     * Генерация случайного цвета для аватара
     */
    private String generateRandomColor() {
        String[] colors = {
                "#FF5252", "#FF9800", "#FFEB3B", "#4CAF50", "#2196F3",
                "#9C27B0", "#795548", "#607D8B", "#FF4081", "#00BCD4",
                "#8BC34A", "#FF5722", "#E91E63", "#3F51B5"
        };
        return colors[(int)(Math.random() * colors.length)]; // Случайный цвет из массива
    }

    // ========== ГЕТТЕРЫ И СЕТТЕРЫ ==========

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFamilyId() { return familyId; }
    public void setFamilyId(int familyId) { this.familyId = familyId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }

    /**
     * Добавление очков пользователю
     */
    public void addPoints(int points) {
        this.totalPoints += points; // Увеличиваем общее количество
    }
}