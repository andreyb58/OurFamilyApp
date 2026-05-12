package com.example.killer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.killer.models.Reward;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) для работы с таблицей rewards
 * Определяет методы для CRUD операций с наградами
 */
@Dao
public interface RewardDao {

    /**
     * Вставка новой награды
     * @param reward Объект награды для сохранения
     */
    @Insert
    void insert(Reward reward);

    /**
     * Обновление существующей награды
     * @param reward Объект награды с обновленными данными
     */
    @Update
    void update(Reward reward);

    /**
     * Удаление награды
     * @param reward Объект награды для удаления
     */
    @Delete
    void delete(Reward reward);

    /**
     * Получение всех наград
     * @return Список всех наград
     */
    @Query("SELECT * FROM rewards")
    List<Reward> getAllRewards();

    /**
     * Получение доступных (не полученных) наград
     * @return Список доступных наград
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0")
    List<Reward> getAvailableRewards();

    /**
     * Получение полученных наград
     * @return Список полученных наград
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 1")
    List<Reward> getClaimedRewards();

    /**
     * Получение наград, которые пользователь может себе позволить
     * @param points Количество очков пользователя
     * @return Список доступных по цене наград
     */
    @Query("SELECT * FROM rewards WHERE cost <= :points AND isClaimed = 0")
    List<Reward> getAffordableRewards(int points);

    /**
     * Получение награды если хватает очков (атомарная операция)
     * @param id ID награды
     * @param claimer Имя пользователя, получающего награду
     * @param date Дата получения
     * @param availablePoints Доступные очки пользователя
     * @return Количество обновленных строк (1 если успешно, 0 если не хватает очков)
     */
    @Query("UPDATE rewards SET isClaimed = 1, claimedBy = :claimer, claimDate = :date WHERE id = :id AND cost <= :availablePoints")
    int claimRewardIfAffordable(int id, String claimer, Date date, int availablePoints);

    /**
     * Получение наград, полученных конкретным пользователем
     * @param userId ID пользователя
     * @return Список полученных пользователем наград
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 1 AND claimedById = :userId")
    List<Reward> getClaimedRewardsByUser(String userId);

    /**
     * Получение награды (без проверки очков)
     * @param id ID награды
     * @param claimer Имя пользователя
     * @param date Дата получения
     */
    @Query("UPDATE rewards SET isClaimed = 1, claimedBy = :claimer, claimDate = :date WHERE id = :id")
    void claimReward(int id, String claimer, Date date);

    /**
     * Получение наград, требующих синхронизации с сервером
     * @return Список несинхронизированных наград
     */
    @Query("SELECT * FROM rewards WHERE synced = 0")
    List<Reward> getUnsyncedRewards();

    /**
     * Отметка награды как синхронизированной
     * @param id ID награды
     */
    @Query("UPDATE rewards SET synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    /**
     * Удаление старых полученных наград
     * @param date Дата, старше которой награды удаляются
     */
    @Query("DELETE FROM rewards WHERE isClaimed = 1 AND claimDate < :date")
    void deleteOldClaimedRewards(Date date);

    /**
     * Получение наград созданных конкретным пользователем
     * @param userId ID создателя
     * @return Список наград созданных пользователем
     */
    @Query("SELECT * FROM rewards WHERE createdBy = :userId")
    List<Reward> getRewardsCreatedByUser(String userId);

    /**
     * Получение наград отсортированных по стоимости (по возрастанию)
     * @return Отсортированный по возрастанию стоимости список наград
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 ORDER BY cost ASC")
    List<Reward> getRewardsSortedByCostAsc();

    /**
     * Получение наград отсортированных по стоимости (по убыванию)
     * @return Отсортированный по убыванию стоимости список наград
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 ORDER BY cost DESC")
    List<Reward> getRewardsSortedByCostDesc();

    /**
     * Поиск наград по названию или описанию
     * @param searchQuery Поисковый запрос
     * @return Список наград, соответствующих запросу
     */
    @Query("SELECT * FROM rewards WHERE (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') AND isClaimed = 0")
    List<Reward> searchRewards(String searchQuery);

    /**
     * Получение недавних наград с ограничением по количеству
     * @param limit Максимальное количество наград
     * @return Список наград (не более limit)
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 ORDER BY id DESC LIMIT :limit")
    List<Reward> getRecentRewards(int limit);

    /**
     * Получение самой дорогой доступной награды
     * @return Самая дорогая награда или null
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 ORDER BY cost DESC LIMIT 1")
    Reward getMostExpensiveReward();

    /**
     * Получение самой дешевой доступной награды
     * @return Самая дешевая награда или null
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 ORDER BY cost ASC LIMIT 1")
    Reward getCheapestReward();

    /**
     * Получение количества доступных наград
     * @return Количество доступных наград
     */
    @Query("SELECT COUNT(*) FROM rewards WHERE isClaimed = 0")
    int getAvailableRewardsCount();

    /**
     * Получение суммы стоимости всех доступных наград
     * @return Общая стоимость всех доступных наград
     */
    @Query("SELECT SUM(cost) FROM rewards WHERE isClaimed = 0")
    Integer getTotalCostOfAvailableRewards();

    /**
     * Получение наград в определенном диапазоне стоимости
     * @param minCost Минимальная стоимость
     * @param maxCost Максимальная стоимость
     * @return Список наград в указанном диапазоне стоимости
     */
    @Query("SELECT * FROM rewards WHERE isClaimed = 0 AND cost BETWEEN :minCost AND :maxCost ORDER BY cost ASC")
    List<Reward> getRewardsByCostRange(int minCost, int maxCost);

    /**
     * Обновление стоимости награды
     * @param id ID награды
     * @param newCost Новая стоимость
     */
    @Query("UPDATE rewards SET cost = :newCost WHERE id = :id")
    void updateRewardCost(int id, int newCost);

    /**
     * Обновление описания награды
     * @param id ID награды
     * @param newDescription Новое описание
     */
    @Query("UPDATE rewards SET description = :newDescription WHERE id = :id")
    void updateRewardDescription(int id, String newDescription);

    /**
     * Получение наград по ID календаря (если будет связь с календарем)
     * @param calendarId ID календаря
     * @return Список наград для календаря
     */
    // @Query("SELECT * FROM rewards WHERE calendarId = :calendarId")
    // List<Reward> getRewardsByCalendar(String calendarId);

}