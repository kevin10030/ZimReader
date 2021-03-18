package com.jinung.edu.sims.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SimulationDbHelper extends SQLiteOpenHelper implements Observer {
    private static final String DATABASE_NAME = "Simulation.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "SimulationDbHelper";
    @SuppressLint({"StaticFieldLeak"})
    private static SimulationDbHelper mSimulationDbHelper;
    private static boolean simulationArrayUpdating = false;
    private static ArrayList<Simulation> simulations;
    private Context mContext;
    private Metadata mMetadata;

    private SimulationDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
        this.mContext = context.getApplicationContext();
        this.mMetadata = new Metadata(context);
        this.mMetadata.addObserver(this);
        simulations = getSimulationsFromDatabase();
    }

    public static synchronized SimulationDbHelper getInstance(Context context) {
        SimulationDbHelper simulationDbHelper;
        synchronized (SimulationDbHelper.class) {
            if (mSimulationDbHelper == null) {
                mSimulationDbHelper = new SimulationDbHelper(context);
            }
            simulationDbHelper = mSimulationDbHelper;
        }
        return simulationDbHelper;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE simulation (_id INTEGER PRIMARY KEY,phetId INTEGER,name TEXT,title TEXT,simulationUrl TEXT,screenshotUrl TEXT,version TEXT,description TEXT,isFavorite INTEGER,categories TEXT,gradeLevels TEXT,simulationHash TEXT,screenshotHash TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS simulation");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public ArrayList<Simulation> getSimulations() {
        if (simulations == null) {
            simulations = getSimulationsFromDatabase();
        }
        return simulations;
    }

    private ArrayList<Simulation> getSimulationsFromDatabase() {
        ArrayList<Simulation> simulationList = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query("simulation", SimulationContract.ALL_SIMS_PROJECTION, (String) null, (String[]) null, (String) null, (String) null, "title DESC");
        while (cursor.moveToNext()) {
            Simulation simulation = new Simulation();
            simulation.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            simulation.setPhetId(cursor.getInt(cursor.getColumnIndex("phetId")));
            simulation.setName(cursor.getString(cursor.getColumnIndex("name")));
            simulation.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            simulation.setSimulationUrl(cursor.getString(cursor.getColumnIndex("simulationUrl")));
            simulation.setScreenshotUrl(cursor.getString(cursor.getColumnIndex("screenshotUrl")));
            simulation.setVersion(cursor.getString(cursor.getColumnIndex("version")));
            simulation.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            simulation.setScreenshotHash(cursor.getString(cursor.getColumnIndex("screenshotHash")));
            simulation.setSimulationHash(cursor.getString(cursor.getColumnIndex("simulationHash")));
            String categoryString = cursor.getString(cursor.getColumnIndex("categories"));
            List<Integer> categoryIds = new LinkedList<>();
            if (categoryString.length() > 0) {
                for (String idString : categoryString.split(",")) {
                    categoryIds.add(Integer.valueOf(Integer.parseInt(idString)));
                }
            }
            simulation.setCategoryIds(categoryIds);
            String gradeLevelString = cursor.getString(cursor.getColumnIndex("gradeLevels"));
            List<Integer> gradeLevelIds = new LinkedList<>();
            if (gradeLevelString.length() > 0) {
                for (String idString2 : gradeLevelString.split(",")) {
                    gradeLevelIds.add(Integer.valueOf(Integer.parseInt(idString2)));
                }
            }
            simulation.setGradeLevelIds(gradeLevelIds);
            simulation.setFavorite(cursor.getInt(cursor.getColumnIndex("isFavorite")) == 1);
            simulationList.add(simulation);
        }
        cursor.close();
        simulations = simulationList;
        return simulations;
    }

    public void updateSimulationsFromServer() {
        if (!simulationArrayUpdating) {
            if (RequestHandler.isConnected(this.mContext)) {
                simulationArrayUpdating = true;
                getSimulationsFromDatabase();
            }
            this.mMetadata.fetchMetadata();
        }
    }

    private void upsertSimulations(JSONObject metadata) {
        try {
            JSONArray projects = metadata.getJSONArray("projects");
            ProgressHandler progressHandler = ProgressHandler.getInstance(this.mContext);
            progressHandler.setProgMax(projects.length());
            for (int i = 0; i < projects.length(); i++) {
                boolean simulationInArray = false;
                int j = 0;
                while (true) {
                    if (j >= simulations.size()) {
                        break;
                    } else if (projects.getJSONObject(i).getJSONArray("simulations").getJSONObject(0).getString("name").equals(simulations.get(j).getName())) {
                        simulationInArray = true;
                        if (!simulations.get(j).getSimulationHash().trim().equals(projects.getJSONObject(i).getJSONArray("simulations").getJSONObject(0).getJSONArray("localizedSimulations").getJSONObject(0).getString("hash"))) {
                            SimulationFiles.retrieveAndStoreSimulation(simulations.get(j), this.mContext);
                        }
                        if (!simulations.get(j).getScreenshotHash().trim().equals(projects.getJSONObject(i).getJSONArray("simulations").getJSONObject(0).getJSONObject("media").getString("screenshotHash"))) {
                            SimulationFiles.retrieveAndStoreScreenshot(simulations.get(j), this.mContext);
                        }
                        simulations.set(j, updateSimulationData(projects.getJSONObject(i), simulations.get(j).getId(), simulations.get(j).isFavorite()));
                    } else {
                        j++;
                    }
                }
                if (!simulationInArray) {
                    simulations.add(insertSimulation(projects.getJSONObject(i), false));
                }
                progressHandler.completeOne();
            }
        } catch (NullPointerException | JSONException npe) {
            Log.e(TAG, "exception", npe);
        }
    }

    private Simulation updateSimulationData(JSONObject project, int simulationDatabaseId, boolean isFavorite) {
        Simulation updatedSimulation = new Simulation(project);
        updatedSimulation.setFavorite(isFavorite);
        getWritableDatabase().update("simulation", updatedSimulation.toContentValues(), "_id = ?", new String[]{Integer.toString(simulationDatabaseId)});
        return updatedSimulation;
    }

    public void insertSimulations(JSONObject metadata) {
        try {
            JSONArray projects = metadata.getJSONArray("projects");
            for (int i = 0; i < projects.length(); i++) {
                if (simulations == null) {
                    simulations = new ArrayList<>();
                }
                simulations.add(insertSimulation(projects.getJSONObject(i), true));
            }
        } catch (JSONException je) {
            Log.e(TAG, "exception", je);
        }
    }

    private Simulation insertSimulation(JSONObject project, boolean isDownloaded) {
        Simulation newSimulation = new Simulation(project);
        newSimulation.setId((int) getWritableDatabase().insert("simulation", (String) null, newSimulation.toContentValues()));
        if (!isDownloaded) {
            SimulationFiles.retrieveAndStoreSimulation(newSimulation, this.mContext);
            SimulationFiles.retrieveAndStoreScreenshot(newSimulation, this.mContext);
        }
        return newSimulation;
    }

    public void updateFavorite(String simulationName, boolean isFavorite) {
        int i;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (isFavorite) {
            i = 1;
        } else {
            i = 0;
        }
        values.put("isFavorite", Integer.valueOf(i));
        db.update("simulation", values, "name = ?", new String[]{simulationName});
        Iterator<Simulation> it = simulations.iterator();
        while (it.hasNext()) {
            Simulation simulation = it.next();
            if (simulation.getName().equals(simulationName)) {
                simulation.setFavorite(isFavorite);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateScreenshotHash(String simulationName, String hash) {
        Iterator<Simulation> it = simulations.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Simulation sim = it.next();
            if (sim.getName().equals(simulationName)) {
                sim.setScreenshotHash(hash);
                break;
            }
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("screenshotHash", hash);
        db.update("simulation", values, "name = ?", new String[]{simulationName});
    }

    /* access modifiers changed from: package-private */
    public void updateSimulationHash(String simulationName, String hash) {
        Iterator<Simulation> it = simulations.iterator();
        while (it.hasNext()) {
            Simulation sim = it.next();
            if (sim.getName().equals(simulationName)) {
                sim.setSimulationHash(hash);
            }
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("simulationHash", hash);
        db.update("simulation", values, "name = ?", new String[]{simulationName});
    }

    public void resetFavorites() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isFavorite", 0);
        db.update("simulation", values, (String) null, (String[]) null);
        Iterator<Simulation> it = simulations.iterator();
        while (it.hasNext()) {
            it.next().setFavorite(false);
        }
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            Log.e(TAG, (String) arg);
        } else if (this.mMetadata == o) {
            upsertSimulations(this.mMetadata.getMetadata());
            simulationArrayUpdating = false;
        }
    }
}
