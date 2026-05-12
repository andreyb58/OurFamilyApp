package com.example.killer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.example.killer.services.SyncService;

/**
 * Утилиты для синхронизации данных с сервером
 * Управляет проверкой подключения и запуском службы синхронизации
 */
public class SyncUtils {

    /**
     * Запланировать синхронизацию данных с сервером
     * @param context Контекст приложения
     */
    public static void scheduleSync(Context context) {
        if (isNetworkAvailable(context)) { // Проверяем доступность сети
            Intent syncIntent = new Intent(context, SyncService.class); // Создаем Intent для службы
            context.startService(syncIntent); // Запускаем службу синхронизации
        } else {
            Toast.makeText(context, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Проверить доступность сети
     * @param context Контекст приложения
     * @return true если сеть доступна, false в противном случае
     */
    public static boolean isNetworkAvailable(Context context) {
        // Получаем ConnectivityManager для проверки состояния сети
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Активное соединение
            return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Проверяем подключение
        }
        return false; // Менеджер не доступен
    }

    /**
     * Принудительная синхронизация с проверкой аутентификации
     * @param context Контекст приложения
     * @param authRequired Требуется ли авторизация
     */
    public static void forceSync(Context context, boolean authRequired) {
        if (authRequired) {
            // Проверяем авторизацию перед синхронизацией
            com.example.killer.auth.AuthManager authManager =
                    com.example.killer.auth.AuthManager.getInstance(context);

            if (!authManager.isLoggedIn()) {
                Toast.makeText(context, "Для синхронизации войдите в систему", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        scheduleSync(context); // Запускаем синхронизацию
    }

    /**
     * Отменить запланированную синхронизацию
     * @param context Контекст приложения
     */
    public static void cancelSync(Context context) {
        Intent syncIntent = new Intent(context, SyncService.class);
        context.stopService(syncIntent); // Останавливаем службу
    }
}