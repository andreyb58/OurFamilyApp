package com.example.killer.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.killer.models.Calendar;
import com.example.killer.models.CalendarEvent;
import com.example.killer.models.User;
import com.example.killer.models.Task;
import com.example.killer.models.Reward;
import com.example.killer.models.Family;
import com.example.killer.models.FamilyMember;
import com.example.killer.models.ChatMessage;
import com.example.killer.models.UserProfile;

/**
 * Главная база данных приложения (Room Database)
 * Определяет все Entity (таблицы) и их версию
 * Паттерн Singleton для единственного экземпляра БД
 */
@TypeConverters({DateConverter.class, StringListConverter.class}) // Конвертеры для сложных типов
@Database(entities = {
        Calendar.class,
        CalendarEvent.class,
        User.class,
        Task.class,
        Reward.class,
        Family.class,
        FamilyMember.class,
        ChatMessage.class,
        UserProfile.class // ДОБАВЛЕНО: Профиль пользователя
}, version = 3, exportSchema = false) // Увеличиваем версию до 3
public abstract class AppDatabase extends RoomDatabase {
    // DAO (Data Access Objects) для каждой таблицы
    public abstract CalendarDao calendarDao();
    public abstract CalendarEventDao calendarEventDao();
    public abstract UserDao userDao();
    public abstract TaskDao taskDao();
    public abstract RewardDao rewardDao();
    public abstract FamilyDao familyDao();
    public abstract FamilyMemberDao familyMemberDao(); // ДОБАВЛЕНО
    public abstract ChatMessageDao chatMessageDao();
    public abstract UserProfileDao userProfileDao(); // ДОБАВЛЕНО: DAO для профилей

    private static volatile AppDatabase INSTANCE; // Единственный экземпляр БД

    /**
     * Получение экземпляра базы данных (Singleton)
     * @param context Контекст приложения
     * @return Экземпляр AppDatabase
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) { // Синхронизация для потокобезопасности
                if (INSTANCE == null) {
                    // Создаем базу данных с использованием Room
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "family_planner_database")
                            .fallbackToDestructiveMigration() // Удаляет старую БД при обновлении версии
                            .build();
                }
            }
        }
        return INSTANCE; // Возвращаем экземпляр БД
    }

    /**
     * Закрытие соединения с базой данных
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close(); // Закрываем соединение
            INSTANCE = null; // Сбрасываем экземпляр
        }
    }
}