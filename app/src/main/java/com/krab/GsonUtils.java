package com.krab;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

/**
 * @Auhtor zhangzhongren
 * @Date 2018/11/29
 * @Description
 */
public class GsonUtils {
    public static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    public static JsonParser parser = new JsonParser();

    public static <T> ArrayList<T> jsonToList(String s, Class<T> tClass) {
        ArrayList<T> objects = new ArrayList<>();
        try {
            JsonArray asJsonArray = parser.parse(s).getAsJsonArray();
            for (JsonElement jsonElement : asJsonArray) {
                objects.add(gson.fromJson(jsonElement, tClass));
            }
        } catch (Exception ignore) {
        }
        return objects;
    }

    public static <T> T from(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String to(Object object) {
        return gson.toJson(object);
    }
}
