package com.rochakgupta.stocktrading.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rochakgupta.stocktrading.detail.Everything;
import com.rochakgupta.stocktrading.main.favorites.FavoritesItem;
import com.rochakgupta.stocktrading.main.search.SearchOption;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private static final Type favoritesItemsType = new TypeToken<List<FavoritesItem>>() {
    }.getType();

    public static Map<String, Double> jsonToLastPrices(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Double>>() {
        }.getType());
    }

    public static List<SearchOption> jsonToSearchOptions(String json) {
        return gson.fromJson(json, new TypeToken<List<SearchOption>>() {
        }.getType());
    }

    public static List<FavoritesItem> jsonToFavorites(String json) {
        return gson.fromJson(json, favoritesItemsType);
    }

    public static String favoritesToJson(List<FavoritesItem> items) {
        return gson.toJson(items, favoritesItemsType);
    }

    public static Everything jsonToEverything(String json) {
        return gson.fromJson(json, Everything.class);
    }
}