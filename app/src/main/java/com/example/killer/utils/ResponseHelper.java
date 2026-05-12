package com.example.killer.utils;

import java.util.Map;

/**
 * Вспомогательный класс для извлечения данных из Map ответов API
 * Обеспечивает безопасное извлечение данных с проверкой типов
 */
public class ResponseHelper {

    /**
     * Проверка успешности ответа API
     * @param response Ответ от API
     * @return true если ответ успешен, false в противном случае
     */
    public static boolean isSuccess(Map<String, Object> response) {
        if (response == null) return false;
        Object successObj = response.get("success");
        return successObj instanceof Boolean && (Boolean) successObj;
    }

    /**
     * Получение сообщения из ответа API
     * @param response Ответ от API
     * @return Сообщение или null если отсутствует
     */
    public static String getMessage(Map<String, Object> response) {
        if (response == null) return null;
        Object messageObj = response.get("message");
        return messageObj instanceof String ? (String) messageObj : null;
    }

    /**
     * Получение ошибки из ответа API
     * @param response Ответ от API
     * @return Текст ошибки или null если отсутствует
     */
    public static String getError(Map<String, Object> response) {
        if (response == null) return null;
        Object errorObj = response.get("error");
        return errorObj instanceof String ? (String) errorObj : null;
    }

    /**
     * Получение данных из ответа API в виде Map
     * @param response Ответ от API
     * @return Map с данными или null если отсутствует
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getData(Map<String, Object> response) {
        if (response == null) return null;
        Object dataObj = response.get("data");
        if (dataObj instanceof Map) {
            return (Map<String, Object>) dataObj;
        }
        return null;
    }

    /**
     * Получение данных из ответа API с приведением к указанному типу
     * @param response Ответ от API
     * @param clazz Класс для приведения типа
     * @return Данные приведенные к указанному типу или null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDataAs(Map<String, Object> response, Class<T> clazz) {
        if (response == null) return null;
        Object dataObj = response.get("data");
        if (clazz.isInstance(dataObj)) {
            return (T) dataObj;
        }
        return null;
    }

    /**
     * Безопасное извлечение целого числа из Map
     * @param map Map с данными
     * @param key Ключ для извлечения
     * @return Integer значение или null если не найдено
     */
    public static Integer getInt(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);

        // Обработка разных типов чисел
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue(); // Конвертируем Double в Integer
        } else if (value instanceof Long) {
            return ((Long) value).intValue(); // Конвертируем Long в Integer
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value); // Парсим строку
            } catch (NumberFormatException e) {
                return null; // Не удалось распарсить
            }
        }
        return null; // Неподдерживаемый тип
    }

    /**
     * Безопасное извлечение строки из Map
     * @param map Map с данными
     * @param key Ключ для извлечения
     * @return String значение или null если не найдено
     */
    public static String getString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }

    /**
     * Безопасное извлечение boolean из Map
     * @param map Map с данными
     * @param key Ключ для извлечения
     * @return Boolean значение или null если не найдено
     */
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value; // Прямое значение
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value); // Парсим строку
        }
        return null; // Неподдерживаемый тип
    }

    /**
     * Извлечение Double из Map
     * @param map Map с данными
     * @param key Ключ для извлечения
     * @return Double значение или null если не найдено
     */
    public static Double getDouble(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Проверка наличия ключа в Map
     * @param map Map с данными
     * @param key Ключ для проверки
     * @return true если ключ существует и значение не null
     */
    public static boolean hasKey(Map<String, Object> map, String key) {
        return map != null && map.containsKey(key) && map.get(key) != null;
    }
}