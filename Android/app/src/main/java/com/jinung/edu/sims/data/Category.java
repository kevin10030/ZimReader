package com.jinung.edu.sims.data;

import java.util.HashMap;

public class Category {
    public static final int ALL = 0;
    public static final int BIOLOGY = 1;
    public static final HashMap<String, Integer> CATEGORY_TO_ID_MAP = new HashMap<String, Integer>() {
        {
            put("All", 0);
            put("Biology", 1);
            put("Chemistry", 2);
            put("Physics", 5);
            put("Math", 4);
            put("Earth Science", 3);
            put("Favorites", 6);
        }
    };
    public static final int CHEMISTRY = 2;
    public static final int EARTH_SCIENCE = 3;
    public static final int FAVORITES = 6;
    public static final HashMap<Integer, String> ID_TO_CATEGORY_MAP = new HashMap<Integer, String>() {
        {
            put(0, "All");
            put(1, "Biology");
            put(2, "Chemistry");
            put(5, "Physics");
            put(4, "Math");
            put(3, "Earth Science");
            put(6, "Favorites");
        }
    };
    public static final int MATH = 4;
    public static final HashMap<Integer, Integer> PHETID_TO_APPID = new HashMap<Integer, Integer>() {
        {
            put(12, 1);
            put(13, 2);
            put(4, 5);
            put(15, 4);
            put(14, 3);
        }
    };
    public static final int PHYSICS = 5;
}
