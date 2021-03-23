package com.jinung.edu.sims;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import com.jinung.edu.sims.data.Metadata;
import com.jinung.edu.sims.data.Simulation;
import com.jinung.edu.sims.data.SimulationDbHelper;
import com.jinung.edu.sims.data.SimulationFiles;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;

public class SplashScreenActivity extends Activity {
    private static final String TAG = "SplashScreenActivity";
    /* access modifiers changed from: private */
    public boolean launchApp = true;
    SimulationDbHelper mSimulationDbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullscreenMode();
        setContentView(R.layout.splash_screen_activity);
        new LoadingTasks().execute(new Void[0]);
    }

    private class LoadingTasks extends AsyncTask<Void, Void, Void> {
        private LoadingTasks() {
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... arg0) {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            if (!new File(SplashScreenActivity.this.getFilesDir() + "/phetsims").exists()) {
                if (blockSize * availableBlocks < 104857600) {
                    boolean unused = SplashScreenActivity.this.launchApp = false;
                    return null;
                }
                try {
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(SplashScreenActivity.this.getAssets().open("phetsims.zip")));
                    byte[] buffer = new byte[1024];
                    while (true) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        if (zipEntry == null) {
                            zipInputStream.close();
                            break;
                        }
                        String filename = zipEntry.getName();
                        if (!zipEntry.isDirectory()) {
                            File file = new File(SplashScreenActivity.this.getFilesDir() + "/" + filename);
                            if (!file.createNewFile()) {
                            }
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            while (true) {
                                int count = zipInputStream.read(buffer);
                                if (count == -1) {
                                    break;
                                }
                                fileOutputStream.write(buffer, 0, count);
                            }
                            fileOutputStream.close();
                            zipInputStream.closeEntry();
                        } else if (!new File(SplashScreenActivity.this.getFilesDir() + "/" + filename).mkdirs()) {
                            throw new IOException();
                        }
                    }
                } catch (Exception e) {
                    Log.e(SplashScreenActivity.TAG, "exception", e);
                }
                try {
                    SplashScreenActivity.this.mSimulationDbHelper = SimulationDbHelper.getInstance(SplashScreenActivity.this.getApplicationContext());
                    BufferedReader metadataBuffer = new BufferedReader(new InputStreamReader(SplashScreenActivity.this.getAssets().open("metadata.json")));
                    String metadataString = metadataBuffer.readLine();
                    metadataBuffer.close();
                    SplashScreenActivity.this.mSimulationDbHelper.insertSimulations(new JSONObject(metadataString));
                    Metadata.saveFileAndHash(metadataString, SplashScreenActivity.this.getApplicationContext());
                } catch (Exception e2) {
                    Log.e(SplashScreenActivity.TAG, "exception", e2);
                }
            }
            if (new File(SplashScreenActivity.this.getFilesDir() + "/phetsims").exists()) {
                Iterator<Simulation> it = SimulationDbHelper.getInstance(SplashScreenActivity.this.getApplicationContext()).getSimulations().iterator();
                while (it.hasNext()) {
                    Simulation sim = it.next();
                    SimulationFiles.calculateSimulationHash(sim.getName(), SplashScreenActivity.this.getApplicationContext());
                    SimulationFiles.calculateScreenshotHash(sim.getName(), SplashScreenActivity.this.getApplicationContext());
                }
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (SplashScreenActivity.this.launchApp) {
                SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this, SimCollectionActivity.class));
                SplashScreenActivity.this.finish();
                return;
            }
            AlertDialog alertDialog = new AlertDialog.Builder(SplashScreenActivity.this).create();
            alertDialog.setTitle(SplashScreenActivity.this.getResources().getString(R.string.insufficient_storage_title));
            alertDialog.setMessage(SplashScreenActivity.this.getResources().getString(R.string.insufficient_storage_init));
            alertDialog.setButton(-2, SplashScreenActivity.this.getResources().getString(R.string.insufficient_storage_close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    SplashScreenActivity.this.finish();
                }
            });
            alertDialog.show();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (this.mSimulationDbHelper != null) {
            this.mSimulationDbHelper.close();
        }
        super.onDestroy();
    }

    private void setupFullscreenMode() {
        setSystemUiVisilityMode().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                View unused = SplashScreenActivity.this.setSystemUiVisilityMode();
            }
        });
    }

    /* access modifiers changed from: private */
    public View setSystemUiVisilityMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(  View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return decorView;
    }
}
