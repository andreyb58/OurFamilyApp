package com.example.killer.database;

import androidx.room.*;
import com.example.killer.models.CalendarEvent;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) для работы с таблицей calendar_events
 * Определяет методы для CRUD операций с событиями календаря
 */
@Dao
public interface CalendarEventDao {

    /**
     * Вставка нового события
     * @param event Объект события для сохранения
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalendarEvent event);

    /**
     * Обновление существующего события
     * @param event Объект события с обновленными данными
     */
    @Update
    void update(CalendarEvent event);

    /**
     * Удаление события
     * @param event Объект события для удаления
     */
    @Delete
    void delete(CalendarEvent event);

    /**
     * Получение всех событий календаря
     * @param calendarId ID календаря
     * @return Список событий календаря, отсортированный по дате и времени
     */
    @Query("SELECT * FROM calendar_events WHERE calendarId = :calendarId ORDER BY eventDate ASC, time ASC")
    List<CalendarEvent> getCalendarEvents(String calendarId);

    /**
     * Получение событий календаря по конкретной дате
     * @param calendarId ID календаря
     * @param date Дата для фильтрации
     * @return Список событий на указанную дату, отсортированный по времени
     */
    @Query("SELECT * FROM calendar_events WHERE calendarId = :calendarId AND eventDate = :date ORDER BY time ASC")
    List<CalendarEvent> getEventsByDate(String calendarId, Date date);

    /**
     * Получение событий календаря по диапазону дат
     * @param calendarId ID календаря
     * @param startDate Начальная дата диапазона
     * @param endDate Конечная дата диапазона
     * @return Список событий в указанном диапазоне дат
     */
    @Query("SELECT * FROM calendar_events WHERE calendarId = :calendarId AND eventDate BETWEEN :startDate AND :endDate ORDER BY eventDate ASC, time ASC")
    List<CalendarEvent> getEventsByDateRange(String calendarId, Date startDate, Date endDate);

    /**
     * Получение ожидающих событий для пользователя
     * @param assignedTo Имя пользователя
     * @return Список невыполненных событий назначенных пользователю
     */
    @Query("SELECT * FROM calendar_events WHERE assignedTo = :assignedTo AND isCompleted = 0")
    List<CalendarEvent> getPendingEventsForUser(String assignedTo);

    /**
     * Отметка события как выполненного
     * @param id ID события
     */
    @Query("UPDATE calendar_events SET isCompleted = 1 WHERE id = :id")
    void completeEvent(int id);

    /**
     * Получение событий, требующих синхронизации с сервером
     * @return Список несинхронизированных событий
     */
    @Query("SELECT * FROM calendar_events WHERE synced = 0")
    List<CalendarEvent> getUnsyncedEvents();

    /**
     * Отметка события как синхронизированного
     * @param id ID события
     */
    @Query("UPDATE calendar_events SET synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    /**
     * Получение количества событий в календаре
     * @param calendarId ID календаря
     * @return Количество событий
     */
    @Query("SELECT COUNT(*) FROM calendar_events WHERE calendarId = :calendarId")
    int getEventCount(String calendarId);

    /**
     * Поиск событий по названию или описанию
     * @param searchQuery Строка поиска
     * @return Список событий, соответствующих поисковому запросу
     */
    @Query("SELECT * FROM calendar_events WHERE (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')")
    List<CalendarEvent> searchEvents(String searchQuery);
}