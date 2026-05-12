package com.example.killer.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.killer.models.User;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private User currentUser;

    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return prefs.contains("jwt_token") && prefs.getInt("user_id", 0) != 0;
    }

    public String getToken() {
        return prefs.getString("jwt_token", null);
    }

    public User getCurrentUser() {
        if (currentUser != null) return currentUser;

        if (!isLoggedIn()) {
            return null;
        }

        currentUser = new User();
        currentUser.setId(prefs.getInt("user_id", 0));
        currentUser.setEmail(prefs.getString("user_email", ""));
        currentUser.setName(prefs.getString("user_name", ""));
        currentUser.setTotalPoints(prefs.getInt("user_points", 0));
        currentUser.setRole(prefs.getString("user_role", "PARENT"));
        currentUser.setFamilyId(prefs.getInt("family_id", 0));
        return currentUser;
    }

    public int getUserId() {
        return prefs.getInt("user_id", 0);
    }

    public String getUserEmail() {
        return prefs.getString("user_email", "");
    }

    public String getUserName() {
        return prefs.getString("user_name", "");
    }

    public int getUserPoints() {
        return prefs.getInt("user_points", 0);
    }

    public String getUserRole() {
        return prefs.getString("user_role", "PARENT");
    }

    public int getFamilyId() {
        return prefs.getInt("family_id", 0);
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface LogoutCallback {
        void onLogout();
    }

    public void register(String email, String password, String name, AuthCallback callback) {
        Log.d(TAG, "Начало регистрации: " + email);

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("email", email);
            requestMap.put("password", password);
            requestMap.put("name", name);

            apiService.register(requestMap).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> body = response.body();
                        Boolean success = (Boolean) body.get("success");

                        if (success != null && success) {
                            try {
                                Map<String, Object> data = (Map<String, Object>) body.get("data");
                                Map<String, Object> userData = (Map<String, Object>) data.get("user");
                                String token = (String) data.get("token");

                                if (userData != null && token != null) {
                                    User user = parseUserFromMap(userData);
                                    saveAuthData(token, user);
                                    callback.onSuccess(user);
                                } else {
                                    callback.onError("Неверный формат ответа от сервера");
                                }
                            } catch (Exception e) {
                                callback.onError("Ошибка обработки данных: " + e.getMessage());
                            }
                        } else {
                            String error = (String) body.get("error");
                            String message = (String) body.get("message");
                            callback.onError(error != null ? error : (message != null ? message : "Неизвестная ошибка"));
                        }
                    } else {
                        callback.onError("Ошибка сервера: " + response.code());
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Ошибка регистрации: " + e.getMessage());
        }
    }

    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Начало входа: " + email);

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("email", email);
            requestMap.put("password", password);

            apiService.login(requestMap).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> body = response.body();
                        Boolean success = (Boolean) body.get("success");

                        if (success != null && success) {
                            try {
                                Map<String, Object> data = (Map<String, Object>) body.get("data");
                                Map<String, Object> userData = (Map<String, Object>) data.get("user");
                                String token = (String) data.get("token");

                                if (userData != null && token != null) {
                                    User user = parseUserFromMap(userData);
                                    saveAuthData(token, user);
                                    callback.onSuccess(user);
                                } else {
                                    callback.onError("Неверный формат ответа от сервера");
                                }
                            } catch (Exception e) {
                                callback.onError("Ошибка обработки данных: " + e.getMessage());
                            }
                        } else {
                            String error = (String) body.get("error");
                            String message = (String) body.get("message");
                            callback.onError(error != null ? error : (message != null ? message : "Неизвестная ошибка"));
                        }
                    } else {
                        callback.onError("Ошибка сервера: " + response.code());
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Ошибка входа: " + e.getMessage());
        }
    }

    private User parseUserFromMap(Map<String, Object> userData) {
        User user = new User();

        Object idObj = userData.get("id");
        if (idObj instanceof Double) {
            user.setId(((Double) idObj).intValue());
        } else if (idObj instanceof Integer) {
            user.setId((Integer) idObj);
        } else if (idObj instanceof String) {
            try {
                user.setId(Integer.parseInt((String) idObj));
            } catch (NumberFormatException e) {
                user.setId(0);
            }
        }

        user.setEmail(userData.get("email") != null ? userData.get("email").toString() : "");
        user.setName(userData.get("name") != null ? userData.get("name").toString() : "");
        user.setRole(userData.get("role") != null ? userData.get("role").toString() : "PARENT");

        Object pointsObj = userData.get("total_points");
        if (pointsObj instanceof Double) {
            user.setTotalPoints(((Double) pointsObj).intValue());
        } else if (pointsObj instanceof Integer) {
            user.setTotalPoints((Integer) pointsObj);
        } else if (pointsObj instanceof String) {
            try {
                user.setTotalPoints(Integer.parseInt((String) pointsObj));
            } catch (NumberFormatException e) {
                user.setTotalPoints(0);
            }
        }

        Object familyIdObj = userData.get("family_id");
        if (familyIdObj instanceof Double) {
            user.setFamilyId(((Double) familyIdObj).intValue());
        } else if (familyIdObj instanceof Integer) {
            user.setFamilyId((Integer) familyIdObj);
        } else {
            user.setFamilyId(0);
        }

        return user;
    }

    public void saveAuthData(String token, User user) {
        Log.d(TAG, "Сохранение данных авторизации для: " + user.getEmail());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("jwt_token", token);
        editor.putInt("user_id", user.getId());
        editor.putString("user_email", user.getEmail());
        editor.putString("user_name", user.getName());
        editor.putInt("user_points", user.getTotalPoints());
        editor.putString("user_role", user.getRole());
        editor.putInt("family_id", user.getFamilyId());
        editor.apply();

        currentUser = user;
    }

    public void logout(LogoutCallback callback) {
        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
            apiService.logout().enqueue(new retrofit2.Callback<Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                    // В любом случае очищаем локальные данные
                    clearAuthData();
                    callback.onLogout();
                }

                @Override
                public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                    // Даже при ошибке сети выходим локально
                    clearAuthData();
                    callback.onLogout();
                }
            });
        } catch (Exception e) {
            clearAuthData();
            callback.onLogout();
        }
    }

    public void logout() {
        clearAuthData();
    }

    private void clearAuthData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        currentUser = null;
    }

    public void updateUserPoints(int points) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_points", points);
        editor.apply();
        if (currentUser != null) {
            currentUser.setTotalPoints(points);
        }
    }

    public void updateFamilyId(int familyId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("family_id", familyId);
        editor.apply();
        if (currentUser != null) {
            currentUser.setFamilyId(familyId);
        }
    }
}