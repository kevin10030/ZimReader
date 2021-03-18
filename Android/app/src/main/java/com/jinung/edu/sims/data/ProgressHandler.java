package com.jinung.edu.sims.data;

import android.annotation.SuppressLint;
import android.content.Context;
import com.jinung.edu.sims.R;
import java.util.Observable;

public class ProgressHandler extends Observable {
    private static final String TAG = "ProgressHandler";
    @SuppressLint({"StaticFieldLeak"})
    private static ProgressHandler mProgressHandler;
    private int barMax;
    private int count = 0;
    private int countMax = 0;

    private ProgressHandler(Context context) {
        this.barMax = context.getApplicationContext().getResources().getInteger(R.integer.progress_bar_max);
    }

    public static synchronized ProgressHandler getInstance(Context context) {
        ProgressHandler progressHandler;
        synchronized (ProgressHandler.class) {
            if (mProgressHandler == null) {
                mProgressHandler = new ProgressHandler(context);
            }
            progressHandler = mProgressHandler;
        }
        return progressHandler;
    }

    /* access modifiers changed from: package-private */
    public void addToMax() {
        this.countMax++;
        setChanged();
        notifyObservers();
    }

    /* access modifiers changed from: package-private */
    public void completeOne() {
        this.count++;
        setChanged();
        notifyObservers();
    }

    /* access modifiers changed from: package-private */
    public void setProgMax(int max) {
        this.countMax = max;
        setChanged();
        notifyObservers();
    }

    public int getProgress() {
        if (this.countMax == 0) {
            return -1;
        }
        if (this.count != this.countMax) {
            return (int) ((((double) this.count) / ((double) this.countMax)) * ((double) this.barMax));
        }
        this.countMax = 0;
        this.count = 0;
        return -1;
    }
}
