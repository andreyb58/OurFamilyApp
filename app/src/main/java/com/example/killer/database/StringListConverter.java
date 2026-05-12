package com.example.killer.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StringListConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}