package com.example.killer.managers;

import android.content.Context;
import android.util.Log;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import java.util.HashMap;
import java.util.Map;

public class FamilyManager {
    private static final String TAG = "FamilyManager";
    private static FamilyManager instance;
    private final Context context;
    private final AuthManager authManager;

    public interface FamilyCallback {
        void onSuccess(Map<String, Object> data);
        void onError(String error);
    }

    private FamilyManager(Context context) {
        this.context = context.getApplicationContext();
        this.authManager = AuthManager.getInstance(context);
    }

    public static synchronized FamilyManager getInstance(Context context) {
        if (instance == null) {
            instance = new FamilyManager(context);
        }
        return instance;
    }

    // Создание семьи
    public void createFamily(String name, String description, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> request = new HashMap<>();
            request.put("name", name);
            request.put("description", description);

            ApiUtils.makeApiCall(apiService.createFamily(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Семья создана: " + data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка создания семьи: " + e.getMessage());
        }
    }

    // Присоединение к семье по коду
    public void joinFamily(String inviteCode, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> request = new HashMap<>();
            request.put("inviteCode", inviteCode);

            ApiUtils.makeApiCall(apiService.joinFamily(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Присоединились к семье: " + data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка присоединения: " + e.getMessage());
        }
    }

    // Получение информации о моей семье
    public void getMyFamily(FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            ApiUtils.makeApiCall(apiService.getMyFamily(), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка получения семьи: " + e.getMessage());
        }
    }

    // Получение членов семьи (используем getMyFamily так как там уже есть члены)
    public void getFamilyMembers(FamilyCallback callback) {
        getMyFamily(callback);
    }

    // Приглашение члена семьи (не реализовано на сервере, показываем сообщение)
    public void inviteMember(String email, String role, FamilyCallback callback) {
        callback.onError("Функция приглашения пока не доступна. Используйте код приглашения для добавления новых членов.");
    }

    // Выход из семьи
    public void leaveFamily(int familyId, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            // Создаем запрос с familyId
            Map<String, Object> request = new HashMap<>();
            request.put("familyId", familyId);

            ApiUtils.makeApiCall(apiService.leaveFamily(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Покинули семью: " + data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка выхода из семьи: " + e.getMessage());
        }
    }

    // Смена роли участника (только для создателя семьи)
    public void changeMemberRole(int memberId, String newRole, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }
        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
            Map<String, Object> request = new HashMap<>();
            request.put("role", newRole);
            ApiUtils.makeApiCall(apiService.changeMemberRole(memberId, request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Роль участника изменена: " + data);
                    callback.onSuccess(data);
                }
                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка смены роли: " + e.getMessage());
        }
    }

    // Обновление информации о семье
    public void updateFamily(int familyId, String name, String description, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> request = new HashMap<>();
            request.put("familyId", familyId);
            request.put("name", name);
            request.put("description", description);

            ApiUtils.makeApiCall(apiService.updateFamily(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Семья обновлена: " + data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка обновления семьи: " + e.getMessage());
        }
    }

    // Удаление семьи
    public void deleteFamily(int familyId, FamilyCallback callback) {
        if (!authManager.isLoggedIn()) {
            callback.onError("Требуется авторизация");
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(context).create(ApiService.class);

            Map<String, Object> request = new HashMap<>();
            request.put("familyId", familyId);

            ApiUtils.makeApiCall(apiService.deleteFamily(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Log.d(TAG, "Семья удалена: " + data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError("Ошибка удаления семьи: " + e.getMessage());
        }
    }
}