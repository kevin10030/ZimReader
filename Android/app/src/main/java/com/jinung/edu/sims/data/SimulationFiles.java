package com.jinung.edu.sims.data;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SimulationFiles {
    private static final String TAG = "SimulationFiles";
    /* access modifiers changed from: private */
    public static ProgressHandler mProgressHandler = null;

    static void retrieveAndStoreSimulation(final Simulation simulation, final Context context) {
        if (RequestHandler.isConnected(context)) {
            if (mProgressHandler == null) {
                mProgressHandler = ProgressHandler.getInstance(context);
            }
            mProgressHandler.addToMax();
            RequestHandler.getInstance(context).addToRequestQueue(new StringRequest(0, simulation.getSimulationUrl(), new Response.Listener<String>() {
                public void onResponse(String response) {
                    try {
                        File simulationFile = SimulationFiles.getSimulationFile(simulation.getName(), context);
                        if (!simulationFile.createNewFile()) {
                        }
                        FileOutputStream simulationFileOutputStream = new FileOutputStream(simulationFile);
                        simulationFileOutputStream.write(response.getBytes());
                        simulationFileOutputStream.close();
                    } catch (OutOfMemoryError e) {
                        Log.e(SimulationFiles.TAG, "exception", e);
                    } catch (Exception e2) {
                        Log.e(SimulationFiles.TAG, "exception", e2);
                    }
                    SimulationFiles.calculateSimulationHash(simulation.getName(), context);
                    SimulationFiles.mProgressHandler.completeOne();
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    SimulationFiles.calculateSimulationHash(simulation.getName(), context);
                    SimulationFiles.mProgressHandler.completeOne();
                    Log.e(SimulationFiles.TAG, "Simulation " + simulation.getName() + " HTML retrieval failed: " + error.getMessage());
                }
            }));
        }
    }

    static void retrieveAndStoreScreenshot(final Simulation simulation, final Context context) {
        if (RequestHandler.isConnected(context)) {
            if (mProgressHandler == null) {
                mProgressHandler = ProgressHandler.getInstance(context);
            }
            mProgressHandler.addToMax();
            RequestHandler.getInstance(context).addToRequestQueue(new ByteRequest(simulation.getScreenshotUrl(), new Response.Listener<byte[]>() {
                public void onResponse(byte[] response) {
                    try {
                        File screenshotFile = SimulationFiles.getSimulationImage(simulation.getName(), context);
                        screenshotFile.createNewFile();
                        FileOutputStream screenshotFileOutputStream = new FileOutputStream(screenshotFile);
                        screenshotFileOutputStream.write(response);
                        screenshotFileOutputStream.close();
                    } catch (OutOfMemoryError e) {
                        Log.e(SimulationFiles.TAG, "exception", e);
                    } catch (Exception e2) {
                        Log.e(SimulationFiles.TAG, "exception", e2);
                    }
                    SimulationFiles.calculateScreenshotHash(simulation.getName(), context);
                    SimulationFiles.mProgressHandler.completeOne();
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    SimulationFiles.calculateScreenshotHash(simulation.getName(), context);
                    SimulationFiles.mProgressHandler.completeOne();
                    Log.e(SimulationFiles.TAG, "Simulation " + simulation.getName() + " image retrieval failed: " + error.getMessage());
                }
            }));
        }
    }

    public static void calculateSimulationHash(String simName, Context context) {
        try {
            SimulationDbHelper.getInstance(context).updateSimulationHash(simName, calculateHash(getSimulationFile(simName, context)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void calculateScreenshotHash(String simName, Context context) {
        try {
            SimulationDbHelper.getInstance(context).updateScreenshotHash(simName, calculateHash(getSimulationImage(simName, context)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String calculateHash(File file) throws IOException {
        try {
            FileInputStream stream = new FileInputStream(file);
            byte[] data = new byte[((int) file.length())];
            stream.read(data);
            return Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(data), 0).trim();
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
            throw new RuntimeException("failed algorithm lookup");
        }
    }

    public static File getSimulationFile(String simulationName, Context context) throws IOException {
        return getOrCreateFile(getSimulationFilename(simulationName), getSimulationDirectoryPath(simulationName, context));
    }

    public static File getSimulationImage(String simulationName, Context context) throws IOException {
        return getOrCreateFile(getSimulationImageFilename(simulationName), getSimulationDirectoryPath(simulationName, context));
    }

    private static File getOrCreateFile(String filename, String directory) throws IOException {
        File file = new File(directory + filename);
        if (!file.exists()) {
            File simDir = new File(directory);
            if ((!simDir.exists() && !simDir.mkdirs()) || !file.createNewFile()) {
                throw new IOException();
            }
        }
        return file;
    }

    public static String getSimulationImageFilename(String simulationName) {
        return simulationName + "-600.png";
    }

    public static String getSimulationFilename(String simulationName) {
        return simulationName + "_en.html";
    }

    public static String getSimulationDirectoryPath(String simulationName, Context context) {
        return context.getFilesDir() + "/phetsims/html/" + simulationName + "/";
    }

    public static boolean simulationFileExists(String simulationName, Context context) {
        return new File(getSimulationDirectoryPath(simulationName, context) + getSimulationFilename(simulationName)).exists();
    }

    public static boolean simulationFileIsValid(String simulationName, Context context) {
        File file = new File(getSimulationDirectoryPath(simulationName, context) + getSimulationFilename(simulationName));
        return file.exists() && file.length() > 0;
    }
}
