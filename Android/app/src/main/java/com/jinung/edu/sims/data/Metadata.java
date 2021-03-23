package com.jinung.edu.sims.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jinung.edu.sims.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import org.json.JSONException;
import org.json.JSONObject;

public class Metadata extends Observable {
    static final String BASE_URL = "https://phet.colorado.edu";
    static final String METADATA_HASH_OPTION = "&sha-256=true";
    static final String METADATA_HASH_PREFERENCES_KEY = "metadata-hash";
    static final String METADATA_OPTIONS = "?format=json&type=html&locale=en";
    static final String METADATA_PATH = "/services/metadata/1.2/simulations";
    private static final String METADATA_VERSION = "1.2";
    private static final String TAG = "Metadata";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mHasMetadataChanged;
    /* access modifiers changed from: private */
    public boolean mHasMetadataHashRequestCompleted;
    /* access modifiers changed from: private */
    public JSONObject mMetadataJson;

    Metadata(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: private */
    public void hasMetadataChanged() {
        RequestHandler.getInstance(this.mContext).addToRequestQueue(new StringRequest(0, "https://phet.colorado.edu/services/metadata/1.2/simulations?format=json&type=html&locale=en&sha-256=true", new Response.Listener<String>() {
            public void onResponse(String response) {
                boolean z = false;
                SharedPreferences prefs = Metadata.this.mContext.getSharedPreferences(Metadata.this.mContext.getString(R.string.preference_key), 0);
                Metadata metadata = Metadata.this;
                if (!response.equals(prefs.getString(Metadata.METADATA_HASH_PREFERENCES_KEY, ""))) {
                    z = true;
                }
                boolean unused = metadata.mHasMetadataChanged = z;
                boolean unused2 = Metadata.this.mHasMetadataHashRequestCompleted = true;
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(Metadata.TAG, "Error on metadata hash http request: " + error.getMessage());
                System.out.flush();
                boolean unused = Metadata.this.mHasMetadataHashRequestCompleted = true;
            }
        }));
    }

    /* access modifiers changed from: package-private */
    public void fetchMetadata() {
        new MetadataUpdateTask().execute(new Void[0]);
    }

    public JSONObject getMetadata() {
        return this.mMetadataJson;
    }

    /* access modifiers changed from: private */
    public void initMetadataJson() throws IOException, JSONException {
        this.mMetadataJson = new JSONObject(new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.mContext.getFilesDir() + "/metadata.json")))).readLine());
    }

    private class MetadataUpdateTask extends AsyncTask<Void, Void, Void> {
        private MetadataUpdateTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... arg0) {
            if (RequestHandler.isConnected(Metadata.this.mContext)) {
                try {
                    boolean unused = Metadata.this.mHasMetadataHashRequestCompleted = false;
                    Metadata.this.hasMetadataChanged();
                    while (!Metadata.this.mHasMetadataHashRequestCompleted) {
                        try {
                            Thread.sleep(100);
                            Log.i(Metadata.TAG, "Waiting for metadata");
                        } catch (Exception e) {
                            Log.e(Metadata.TAG, "exception", e);
                        }
                    }
                    if (Metadata.this.mHasMetadataChanged) {
                        RequestHandler.getInstance(Metadata.this.mContext).addToRequestQueue(new StringRequest(0, "https://phet.colorado.edu/services/metadata/1.2/simulations?format=json&type=html&locale=en", new Response.Listener<String>() {
                            public void onResponse(String response) {
                                try {
                                    JSONObject unused = Metadata.this.mMetadataJson = new JSONObject(response);
                                    Metadata.this.setChanged();
                                    Metadata.this.notifyObservers();
                                    Metadata.saveFileAndHash(response, Metadata.this.mContext);
                                } catch (JSONException je) {
                                    Log.e(Metadata.TAG, "exception", je);
                                    Metadata.this.setChanged();
                                    Metadata.this.notifyObservers("Error parsing json");
                                }
                            }
                        }, new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                Metadata.this.setChanged();
                                Metadata.this.notifyObservers(error.getMessage());
                            }
                        }));
                    } else if (Metadata.this.mMetadataJson == null) {
                        try {
                            Metadata.this.initMetadataJson();
                            Metadata.this.setChanged();
                            Metadata.this.notifyObservers();
                        } catch (Exception e2) {
                            Log.e(Metadata.TAG, "exception", e2);
                        }
                    } else {
                        Metadata.this.setChanged();
                        Metadata.this.notifyObservers();
                    }
                } catch (RuntimeException re) {
                    Log.e(Metadata.TAG, "exception", re);
                }
            } else {
                try {
                    Metadata.this.initMetadataJson();
                } catch (IOException | JSONException e3) {
                    Log.e(Metadata.TAG, "exception", e3);
                }
                Metadata.this.setChanged();
                Metadata.this.notifyObservers();
            }
            return null;
        }
    }

    @SuppressLint({"ApplySharedPref"})
    public static void saveFileAndHash(String metadataString, Context context) {
        String hash;
        try {
            File metadataFile = new File(context.getFilesDir() + "/metadata.json");
            if (!metadataFile.createNewFile()) {
            }
            FileOutputStream metadataFileOutputStream = new FileOutputStream(metadataFile);
            metadataFileOutputStream.write(metadataString.getBytes());
            metadataFileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }
        try {
            hash = Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(metadataString.getBytes()), 0);
        } catch (NoSuchAlgorithmException e2) {
            Log.e(TAG, "Could not encode hash");
            hash = "";
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.preference_key), 0).edit();
        editor.putString(METADATA_HASH_PREFERENCES_KEY, hash);
        editor.commit();
    }
}
