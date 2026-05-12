package com.example.killer.database;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Конвертер типов для Room Database
 * Преобразует Date в Long (timestamp) и обратно для хранения в SQLite
 */
public class DateConverter {

    /**
     * Конвертировать Date в Long (timestamp в миллисекундах)
     * @param date Дата для конвертации
     * @return Timestamp в миллисекундах или null если date равен null
     */
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Конвертировать Long (timestamp) в Date
     * @param timestamp Timestamp в миллисекундах
     * @return Объект Date или null если timestamp равен null
     */
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    /**
     * Конвертировать Date в строку формата "yyyy-MM-dd"
     * @param date Дата для форматирования
     * @return Строка в формате "yyyy-MM-dd" или пустая строка если date равен null
     */
    public static String toDateString(Date date) {
        if (date == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Конвертировать Date в строку формата "dd.MM.yyyy HH:mm"
     * @param date Дата для форматирования
     * @return Строка в формате "dd.MM.yyyy HH:mm" или пустая строка если date равен null
     */
    public static String toDateTimeString(Date date) {
        if (date == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Конвертировать Date в строку формата "HH:mm"
     * @param date Дата для форматирования (используется только время)
     * @return Строка в формате "HH:mm" или пустая строка если date равен null
     */
    public static String toTimeString(Date date) {
        if (date == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Парсить строку формата "yyyy-MM-dd" в Date
     * @param dateString Строка с датой
     * @return Объект Date или null если строка пустая или невалидная
     */
    public static Date fromDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            return sdf.parse(dateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Парсить строку формата "dd.MM.yyyy HH:mm" в Date
     * @param dateTimeString Строка с датой и временем
     * @return Объект Date или null если строка пустая или невалидная
     */
    public static Date fromDateTimeString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
            return sdf.parse(dateTimeString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить текущую дату
     * @return Текущая дата и время
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Получить дату начала дня (00:00:00)
     * @param date Исходная дата
     * @return Дата с установленным временем на начало дня
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) return null;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Получить дату конца дня (23:59:59)
     * @param date Исходная дата
     * @return Дата с установленным временем на конец дня
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) return null;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * Добавить дни к дате
     * @param date Исходная дата
     * @param days Количество дней для добавления (может быть отрицательным)
     * @return Новая дата
     */
    public static Date addDays(Date date, int days) {
        if (date == null) return null;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);

        return calendar.getTime();
    }

    /**
     * Проверить, является ли дата сегодняшней
     * @param date Дата для проверки
     * @return true если дата сегодняшняя, false в противном случае
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;

        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar targetDate = java.util.Calendar.getInstance();
        targetDate.setTime(date);

        return today.get(java.util.Calendar.YEAR) == targetDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == targetDate.get(java.util.Calendar.DAY_OF_YEAR);
    }

    /**
     * Проверить, является ли дата вчерашней
     * @param date Дата для проверки
     * @return true если дата вчерашняя, false в противном случае
     */
    public static boolean isYesterday(Date date) {
        if (date == null) return false;

        Date yesterday = addDays(getCurrentDate(), -1);
        return isSameDay(date, yesterday);
    }

    /**
     * Проверить, являются ли две даты одним днем
     * @param date1 Первая дата
     * @param date2 Вторая дата
     * @return true если даты в один день, false в противном случае
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
}