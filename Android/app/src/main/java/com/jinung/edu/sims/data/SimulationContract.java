package com.jinung.edu.sims.data;

import android.provider.BaseColumns;

final class SimulationContract {
    static final String[] ALL_SIMS_PROJECTION = {"_id", "phetId", "name", "title", "simulationUrl", "screenshotUrl", "version", "description", "isFavorite", "categories", "gradeLevels", "simulationHash", "screenshotHash"};
    static final String SQL_CREATE_ENTRIES = "CREATE TABLE simulation (_id INTEGER PRIMARY KEY,phetId INTEGER,name TEXT,title TEXT,simulationUrl TEXT,screenshotUrl TEXT,version TEXT,description TEXT,isFavorite INTEGER,categories TEXT,gradeLevels TEXT,simulationHash TEXT,screenshotHash TEXT)";
    static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS simulation";

    SimulationContract() {
    }

    static class SimulationEntry implements BaseColumns {
        static final String COLUMN_NAME_CATEGORIES = "categories";
        static final String COLUMN_NAME_DESCRIPTION = "description";
        static final String COLUMN_NAME_GRADE_LEVELS = "gradeLevels";
        static final String COLUMN_NAME_IS_FAVORITE = "isFavorite";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_PHETID = "phetId";
        static final String COLUMN_NAME_SCREENSHOT_HASH = "screenshotHash";
        static final String COLUMN_NAME_SCREENSHOT_URL = "screenshotUrl";
        static final String COLUMN_NAME_SIMULATION_HASH = "simulationHash";
        static final String COLUMN_NAME_SIMULATION_URL = "simulationUrl";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_VERSION = "version";
        static final String TABLE_NAME = "simulation";

        SimulationEntry() {
        }
    }
}
