package com.example.killer.api;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface ApiService {

    // ========== АВТОРИЗАЦИЯ ==========
    @POST("auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> request);

    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, Object> request);

    @GET("auth/check")
    Call<Map<String, Object>> checkToken();

    @POST("auth/logout")
    Call<Map<String, Object>> logout();

    // ========== ПОЛЬЗОВАТЕЛИ ==========
    @GET("users/profile")
    Call<Map<String, Object>> getProfile();

    @PUT("users/profile")
    Call<Map<String, Object>> updateProfile(@Body Map<String, Object> request);

    @PUT("users/password")
    Call<Map<String, Object>> changePassword(@Body Map<String, Object> request);

    @GET("users/leaderboard")
    Call<Map<String, Object>> getLeaderboard();

    @GET("achievements")
    Call<Map<String, Object>> getAchievements(@Query("userId") Integer userId);

    // ========== СЕМЬЯ ==========
    @GET("families/my")
    Call<Map<String, Object>> getMyFamily();

    @GET("families/members")
    Call<Map<String, Object>> getFamilyMembers();

    @PUT("families/settings")
    Call<Map<String, Object>> updateFamilySettings(@Body Map<String, Object> request);

    @POST("families")
    Call<Map<String, Object>> createFamily(@Body Map<String, Object> request);

    @POST("families/join")
    Call<Map<String, Object>> joinFamily(@Body Map<String, Object> request);

    @POST("families/leave")
    Call<Map<String, Object>> leaveFamily(@Body Map<String, Object> request);

    // Логическое обновление/удаление семьи пока не реализовано на сервере
    @POST("families/update")
    Call<Map<String, Object>> updateFamily(@Body Map<String, Object> request);

    @POST("families/delete")
    Call<Map<String, Object>> deleteFamily(@Body Map<String, Object> request);

    // ========== ЗАДАНИЯ ==========
    @GET("tasks")
    Call<Map<String, Object>> getTasks(@Query("status") String status);

    @POST("tasks")
    Call<Map<String, Object>> createTask(@Body Map<String, Object> request);

    @PUT("tasks/{id}/complete")
    Call<Map<String, Object>> completeTask(@Path("id") int id);

    @DELETE("tasks/{id}")
    Call<Map<String, Object>> deleteTask(@Path("id") int id);

    // ========== НАГРАДЫ ==========
    @GET("rewards")
    Call<Map<String, Object>> getRewards(@Query("status") String status);

    @POST("rewards")
    Call<Map<String, Object>> createReward(@Body Map<String, Object> request);

    @PUT("rewards/{id}/claim")
    Call<Map<String, Object>> claimReward(@Path("id") int id);

    @DELETE("rewards/{id}")
    Call<Map<String, Object>> deleteReward(@Path("id") int id);

    // ========== СТАТИСТИКА ==========
    @GET("stats")
    Call<Map<String, Object>> getStats();

    // ========== ЧАТ ==========
    @GET("chat")
    Call<Map<String, Object>> getFamilyChat(@Query("since") Integer sinceId);

    @POST("chat")
    Call<Map<String, Object>> sendFamilyChat(@Body Map<String, Object> request);

    @GET("chat/private")
    Call<Map<String, Object>> getPrivateChat(@Query("withUserId") int withUserId,
                                             @Query("since") Integer sinceId);

    @POST("chat/private")
    Call<Map<String, Object>> sendPrivateChat(@Body Map<String, Object> request);

    // ========== КАЛЕНДАРЬ ==========
    @GET("calendar")
    Call<Map<String, Object>> getCalendarEvents(@Query("from") String from,
                                                @Query("to") String to);

    @POST("calendar")
    Call<Map<String, Object>> createCalendarEvent(@Body Map<String, Object> request);

    @PUT("calendar/{id}")
    Call<Map<String, Object>> updateCalendarEvent(@Path("id") int id,
                                                  @Body Map<String, Object> request);

    @DELETE("calendar/{id}")
    Call<Map<String, Object>> deleteCalendarEvent(@Path("id") int id);
}