package com.jinung.edu.sims.data;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Simulation {
    private String TAG = "Simulation";
    private List<Integer> categories;
    private String description;
    private List<Integer> gradeLevels;
    private int id;
    private boolean isFavorite;
    private String name;
    private int phetId;
    private String screenshotHash;
    private String screenshotUrl;
    private String simulationHash;
    private String simulationUrl;
    private String title;
    private String version;

    Simulation() {
    }

    Simulation(JSONObject project) {
        try {
            JSONObject simulation = project.getJSONArray("simulations").getJSONObject(0);
            JSONObject localizedSimulation = simulation.getJSONArray("localizedSimulations").getJSONObject(0);
            this.phetId = simulation.getInt("id");
            this.name = simulation.getString("name");
            this.title = localizedSimulation.getString("title");
            this.simulationUrl = localizedSimulation.getString("runUrl");
            this.screenshotUrl = simulation.getJSONObject("media").getString("screenshotUrl");
            this.version = project.getJSONObject("version").getString("string");
            this.description = simulation.getJSONObject("description").getString("en");
            this.screenshotHash = "";
            this.simulationHash = "";
            List<Integer> categoryIds = new LinkedList<>();
            List<Integer> gradeLevelIds = new LinkedList<>();
            JSONArray categoryJSONArray = simulation.getJSONArray("categoryIds");
            for (int j = 0; j < categoryJSONArray.length(); j++) {
                if (Category.PHETID_TO_APPID.containsKey(Integer.valueOf(categoryJSONArray.getInt(j)))) {
                    categoryIds.add(Category.PHETID_TO_APPID.get(Integer.valueOf(categoryJSONArray.getInt(j))));
                }
                if (GradeLevel.PHETID_TO_APPID.containsKey(Integer.valueOf(categoryJSONArray.getInt(j)))) {
                    gradeLevelIds.add(GradeLevel.PHETID_TO_APPID.get(Integer.valueOf(categoryJSONArray.getInt(j))));
                }
            }
            this.categories = categoryIds;
            this.gradeLevels = gradeLevelIds;
            this.isFavorite = false;
        } catch (JSONException je) {
            Log.e(this.TAG, "exception", je);
        }
    }

    public int getId() {
        return this.id;
    }

    /* access modifiers changed from: protected */
    public void setId(int id2) {
        this.id = id2;
    }

    public String getName() {
        return this.name;
    }

    /* access modifiers changed from: protected */
    public void setName(String name2) {
        this.name = name2;
    }

    public String getTitle() {
        return this.title;
    }

    /* access modifiers changed from: protected */
    public void setTitle(String title2) {
        this.title = title2;
    }

    /* access modifiers changed from: protected */
    public String getSimulationUrl() {
        return this.simulationUrl;
    }

    /* access modifiers changed from: protected */
    public void setSimulationUrl(String url) {
        this.simulationUrl = url;
    }

    /* access modifiers changed from: protected */
    public String getScreenshotUrl() {
        return this.screenshotUrl;
    }

    /* access modifiers changed from: protected */
    public void setScreenshotUrl(String url) {
        this.screenshotUrl = url;
    }

    public List<Integer> getCategoryIds() {
        return this.categories;
    }

    /* access modifiers changed from: protected */
    public void setCategoryIds(List<Integer> categories2) {
        this.categories = categories2;
    }

    public List<Integer> getGradeLevelIds() {
        return this.gradeLevels;
    }

    /* access modifiers changed from: protected */
    public void setGradeLevelIds(List<Integer> gradeLevels2) {
        this.gradeLevels = gradeLevels2;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    /* access modifiers changed from: protected */
    public String getVersion() {
        return this.version;
    }

    /* access modifiers changed from: protected */
    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getDescription() {
        return this.description;
    }

    /* access modifiers changed from: protected */
    public void setDescription(String description2) {
        this.description = description2;
    }

    public int getPhetId() {
        return this.phetId;
    }

    public void setPhetId(int phetId2) {
        this.phetId = phetId2;
    }

    public void printContentValues(String tag) {
        ContentValues contentValues = toContentValues();
        for (String key : contentValues.keySet()) {
            Log.d(this.TAG + ":" + tag, getName() + "." + key + ":" + contentValues.getAsString(key));
        }
        Log.d(this.TAG + ":" + tag, "\n\n");
    }

    /* access modifiers changed from: package-private */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("phetId", Integer.valueOf(getPhetId()));
        values.put("name", getName());
        values.put("title", getTitle());
        values.put("simulationUrl", getSimulationUrl());
        values.put("screenshotUrl", getScreenshotUrl());
        values.put("version", getVersion());
        values.put("description", getDescription());
        values.put("isFavorite", Integer.valueOf(isFavorite() ? 1 : 0));
        values.put("categories", TextUtils.join(",", getCategoryIds()));
        values.put("gradeLevels", TextUtils.join(",", getGradeLevelIds()));
        return values;
    }

    /* access modifiers changed from: package-private */
    public String getSimulationHash() {
        return this.simulationHash;
    }

    /* access modifiers changed from: package-private */
    public void setSimulationHash(String simulationHash2) {
        this.simulationHash = simulationHash2;
    }

    /* access modifiers changed from: package-private */
    public String getScreenshotHash() {
        return this.screenshotHash;
    }

    /* access modifiers changed from: package-private */
    public void setScreenshotHash(String screenshotHash2) {
        this.screenshotHash = screenshotHash2;
    }

    public static Comparator<Simulation> getSimulationComparator() {
        return new Comparator<Simulation>() {
            public int compare(Simulation o1, Simulation o2) {
                return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
            }
        };
    }
}
