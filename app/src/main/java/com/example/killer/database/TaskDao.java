package com.example.killer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.killer.models.Task;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) для работы с таблицей tasks
 * Определяет методы для CRUD операций с заданиями
 */
@Dao
public interface TaskDao {

    /**
     * Вставка нового задания
     * @param task Объект задания для сохранения
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    /**
     * Обновление существующего задания
     * @param task Объект задания с обновленными данными
     */
    @Update
    void update(Task task);

    /**
     * Удаление задания
     * @param task Объект задания для удаления
     */
    @Delete
    void delete(Task task);

    /**
     * Получение всех заданий
     * @return Список всех заданий
     */
    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    /**
     * Получение активных (не выполненных) заданий
     * @return Список активных заданий
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    List<Task> getActiveTasks();

    /**
     * Получение выполненных заданий
     * @return Список выполненных заданий
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    List<Task> getCompletedTasks();

    /**
     * Получение активных заданий для конкретного пользователя
     * @param userId ID пользователя
     * @return Список активных заданий пользователя
     */
    @Query("SELECT * FROM tasks WHERE assignedToId = :userId AND isCompleted = 0")
    List<Task> getActiveTasksForUser(String userId);

    /**
     * Получение выполненных заданий для конкретного пользователя
     * @param userId ID пользователя
     * @return Список выполненных заданий пользователя
     */
    @Query("SELECT * FROM tasks WHERE assignedToId = :userId AND isCompleted = 1")
    List<Task> getCompletedTasksForUser(String userId);

    /**
     * Получение заданий по имени назначенного пользователя
     * @param name Имя назначенного пользователя
     * @return Список заданий для указанного пользователя
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :name")
    List<Task> getTasksByAssignee(String name);

    /**
     * Отметка задания как выполненного
     * @param id ID задания
     * @param date Дата выполнения
     */
    @Query("UPDATE tasks SET isCompleted = 1, completedDate = :date WHERE id = :id")
    void completeTask(int id, Date date);

    /**
     * Отметка что очки за задание начислены
     * @param id ID задания
     */
    @Query("UPDATE tasks SET pointsAwarded = 1 WHERE id = :id")
    void markPointsAwarded(int id);

    /**
     * Получение заданий, требующих синхронизации с сервером
     * @return Список несинхронизированных заданий
     */
    @Query("SELECT * FROM tasks WHERE synced = 0")
    List<Task> getUnsyncedTasks();

    /**
     * Отметка задания как синхронизированного
     * @param id ID задания
     */
    @Query("UPDATE tasks SET synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    /**
     * Удаление старых выполненных заданий
     * @param date Дата, старше которой задания удаляются
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND completedDate < :date")
    void deleteOldCompletedTasks(Date date);

    /**
     * Получение заданий с истекающим сроком выполнения
     * @param today Текущая дата
     * @param //daysToDue Количество дней до истечения срока
     * @return Список заданий с истекающим сроком
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDate BETWEEN :today AND :dueDate")
    List<Task> getTasksDueSoon(Date today, Date dueDate);

    /**
     * Получение количества активных заданий для пользователя
     * @param userId ID пользователя
     * @return Количество активных заданий
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE assignedToId = :userId AND isCompleted = 0")
    int getActiveTasksCount(String userId);


}