package com.jinung.edu.sims;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinung.edu.sims.data.SimulationDbHelper;
import com.jinung.edu.sims.data.SimulationFiles;
import java.io.File;
import java.io.IOException;

public class SimulationActivity extends FragmentActivity {
    private static final String TAG_WEBVIEW_FRAGMENT = "WebviewFragment";
    private String TAG = "SimulationActivity";
    private boolean isFavorite;
    private WebviewFragment mWebviewFragment;
    private String simulationName;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulation_activity);
        setupFullscreenMode();
        Intent intent = getIntent();
        this.simulationName = intent.getStringExtra(SimCollectionActivity.SIM_NAME);
        String simulationTitle = intent.getStringExtra(SimCollectionActivity.SIM_TITLE);
        this.isFavorite = intent.getBooleanExtra(SimCollectionActivity.SIM_IS_FAVORITE, false);
        FragmentManager fm = getSupportFragmentManager();
        this.mWebviewFragment = (WebviewFragment) fm.findFragmentByTag(TAG_WEBVIEW_FRAGMENT);
        if (this.mWebviewFragment == null) {
            this.mWebviewFragment = new WebviewFragment();
            fm.beginTransaction().add((Fragment) this.mWebviewFragment, TAG_WEBVIEW_FRAGMENT).commit();
            this.mWebviewFragment.setSimulationName(this, this.simulationName);
        }
        ((TextView) findViewById(R.id.title)).setText(simulationTitle);
        FrameLayout favoriteButtonFrame = (FrameLayout) findViewById(R.id.favorite_button);
        ImageView favoriteButton = (ImageView) favoriteButtonFrame.getChildAt(0);
        if (this.isFavorite) {
            favoriteButton.setImageResource(R.drawable.heart_filled);
            favoriteButtonFrame.setContentDescription(getResources().getString(R.string.selected_favorite_a11y));
            return;
        }
        favoriteButton.setImageResource(R.drawable.heart_empty);
        favoriteButtonFrame.setContentDescription(getResources().getString(R.string.unselected_favorite_a11y));
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        setSystemUiVisilityMode();
    }

    private void setupFullscreenMode() {
        setSystemUiVisilityMode().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                View unused = SimulationActivity.this.setSystemUiVisilityMode();
            }
        });
    }

    /* access modifiers changed from: private */
    public View setSystemUiVisilityMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(5894);
        getWindow().addFlags(1024);
        return decorView;
    }

    public void refresh(View view) {
        ((WebView) findViewById(R.id.webview_fragment)).reload();
    }

    public void toggleFavorite(View view) {
        SimulationDbHelper simulationDbHelper = SimulationDbHelper.getInstance(getApplicationContext());
        FrameLayout imageViewFrame = (FrameLayout) view.findViewById(R.id.favorite_button);
        ImageView imageView = (ImageView) imageViewFrame.getChildAt(0);
        if (this.isFavorite) {
            this.isFavorite = false;
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_empty));
            imageViewFrame.setContentDescription(getResources().getString(R.string.unselected_favorite_a11y));
            simulationDbHelper.updateFavorite(this.simulationName, false);
            return;
        }
        this.isFavorite = true;
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_filled));
        imageViewFrame.setContentDescription(getResources().getString(R.string.selected_favorite_a11y));
        simulationDbHelper.updateFavorite(this.simulationName, true);
    }

    public void done(View view) {
        super.onBackPressed();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom);
    }

    public static class WebviewFragment extends Fragment {
        private static final String TAG = "WebviewFragment";
        private static WebView mWebView;
        private String simulationName;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mWebView = (WebView) inflater.inflate(R.layout.webview_fragment, container, false);
            return mWebView;
        }

        @SuppressLint({"SetJavaScriptEnabled"})
        public void setSimulationName(Activity activity, String simulationName2) {
            try {
                File htmlFile = SimulationFiles.getSimulationFile(simulationName2, activity);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                if (htmlFile.exists()) {
                    mWebView.loadUrl("file:///" + htmlFile + "?phet-android-app");
                    return;
                }
                throw new RuntimeException("Sim file did not exist!?!");
            } catch (IOException e) {
                Log.e(TAG, "exception", e);
            }
        }

        public void refresh() {
            mWebView.reload();
        }
    }
}
