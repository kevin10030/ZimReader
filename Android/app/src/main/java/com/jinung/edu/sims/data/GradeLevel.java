package com.jinung.edu.sims.data;

import java.util.HashMap;

public class GradeLevel {
    public static final int ALL = 0;
    public static final int ELEMENTARY = 1;
    public static final HashMap<Integer, String> GRADE_LEVEL_MAP = new HashMap<Integer, String>() {
        {
            put(0, "All");
            put(1, "Elementary School");
            put(2, "Middle School");
            put(3, "High School");
            put(4, "University");
        }
    };
    public static final int HIGH = 3;
    public static final int MIDDLE = 2;
    public static final HashMap<Integer, Integer> PHETID_TO_APPID = new HashMap<Integer, Integer>() {
        {
            put(22, 1);
            put(23, 2);
            put(24, 3);
            put(25, 4);
        }
    };
    public static final int UNIVERSITY = 4;
}
