package com.example.killer.api;

import android.content.Context;
import com.example.killer.auth.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

/**
 * Утилиты для работы с API
 */
public class ApiUtils {

    /**
     * Универсальный метод для выполнения API запросов.
     * ИСПРАВЛЕНО: корректно обрабатывает data в виде List (списки задач, наград и т.д.)
     */
    @SuppressWarnings("unchecked")
    public static void makeApiCall(Call<Map<String, Object>> call,
                                   ApiCallback<Map<String, Object>> callback) {
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> apiResponse = response.body();
                    Boolean success = (Boolean) apiResponse.get("success");

                    if (success != null && success) {
                        Object data = apiResponse.get("data");

                        if (data instanceof Map) {
                            // data — объект, передаём напрямую
                            callback.onSuccess((Map<String, Object>) data);

                        } else if (data instanceof java.util.List) {
                            // БАГ #1 ИСПРАВЛЕН: data — список (задачи, награды, участники и т.д.)
                            // Оборачиваем в Map чтобы не потерять данные
                            Map<String, Object> wrapper = new java.util.HashMap<>();
                            wrapper.put("list", data);
                            callback.onSuccess(wrapper);

                        } else {
                            // data == null или примитив (logout, delete, update)
                            Map<String, Object> emptyData = new java.util.HashMap<>();
                            Object message = apiResponse.get("message");
                            if (message != null) emptyData.put("message", message.toString());
                            callback.onSuccess(emptyData);
                        }
                    } else {
                        Object error = apiResponse.get("error");
                        Object message = apiResponse.get("message");
                        String errorMsg = error != null ? error.toString()
                                : (message != null ? message.toString() : "Неизвестная ошибка");
                        callback.onError(errorMsg);
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            callback.onError("Ошибка сервера " + response.code() + ": " + errorBody);
                        } else {
                            callback.onError("Ошибка сервера: " + response.code());
                        }
                    } catch (Exception e) {
                        callback.onError("Ошибка сервера: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Проверка авторизации перед выполнением запроса
     */
    public static void checkAuth(Context context, AuthCheckCallback callback) {
        AuthManager authManager = AuthManager.getInstance(context);

        if (!authManager.isLoggedIn()) {
            callback.onAuthRequired();
        } else {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
            apiService.checkToken().enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call,
                                       Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Boolean success = (Boolean) response.body().get("success");
                        if (success != null && success) {
                            callback.onAuthValid();
                        } else {
                            callback.onAuthRequired();
                        }
                    } else {
                        callback.onAuthRequired();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    callback.onNetworkError(t.getMessage());
                }
            });
        }
    }

    /**
     * Вспомогательный метод: безопасное извлечение int из Object (Double/Integer/String)
     */
    public static int extractInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Double) return ((Double) value).intValue();
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    /**
     * Вспомогательный метод: безопасное извлечение String из Object
     */
    public static String extractString(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Вспомогательный метод: безопасное извлечение boolean из Object
     */
    public static boolean extractBool(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return false;
    }

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public interface AuthCheckCallback {
        void onAuthValid();
        void onAuthRequired();
        void onNetworkError(String error);
    }
}
