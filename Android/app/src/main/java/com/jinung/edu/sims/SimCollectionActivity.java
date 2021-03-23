package com.jinung.edu.sims;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.jinung.edu.sims.SimulationGridItemAdapter;
import com.jinung.edu.sims.data.Category;
import com.jinung.edu.sims.data.GradeLevel;
import com.jinung.edu.sims.data.ProgressHandler;
import com.jinung.edu.sims.data.Simulation;
import com.jinung.edu.sims.data.SimulationDbHelper;
import com.jinung.edu.sims.data.SimulationFiles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

public class SimCollectionActivity extends Activity implements Observer {
    private static final String CATEGORY_FILTER_STRING = "category-filter";
    private static final String GRADE_LEVEL_FILTER_STRING = "grade-level-filter";
    private static final String SEARCH_FILTER_STRING = "search-filter";
    private static final String SEARCH_LAYOUT_IS_VISIBLE_BOOL = "search-layout-is-visible";
    public static final String SIM_DESCRIPTION = "sim-description";
    public static final String SIM_IS_FAVORITE = "sim-is-favorite";
    public static final String SIM_NAME = "sim-name";
    public static final String SIM_TITLE = "sim-title";
    private static final String TAG = "SimCollectionActivity";
    public static Activity self;
    /* access modifiers changed from: private */
    public String categoryFilterString;
    /* access modifiers changed from: private */
    public int currentGridViewFocusPosition;
    /* access modifiers changed from: private */
    public View currentlyFocusedView;
    /* access modifiers changed from: private */
    public String gradeLevelFilterString;
    private FrameLayout mAppInfoButton;
    private EditText mAutoCompleteTextView;
    /* access modifiers changed from: private */
    public Spinner mCategorySpinner;
    public DialogFragment mDialogFragment;
    private Spinner mGradeLevelSpinner;
    public GridView mGridView;
    private LinearLayout mLinearLayout;
    /* access modifiers changed from: private */
    public ProgressBar mProgressBar;
    private ProgressHandler mProgressHandler;
    private FrameLayout mResetButton;
    /* access modifiers changed from: private */
    public LinearLayout mSearchLayout;
    /* access modifiers changed from: private */
    public TextView mSearchToggleView;
    /* access modifiers changed from: private */
    public LinearLayout mSettingsLayout;
    /* access modifiers changed from: private */
    public TextView mSettingsToggleView;
    /* access modifiers changed from: private */
    public String searchFilterString;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setupFullscreenMode();
        setContentView(R.layout.sim_collection_activity);
        this.mGridView = (GridView) findViewById(R.id.grid_view);
        this.mGradeLevelSpinner = (Spinner) findViewById(R.id.grade_level_spinner);
        this.mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        this.mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        this.mAppInfoButton = (FrameLayout) findViewById(R.id.app_info);
        this.mSearchToggleView = (TextView) findViewById(R.id.search_toggle);
        this.mSettingsToggleView = (TextView) findViewById(R.id.settings_toggle);
        this.mSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
        this.mSettingsLayout = (LinearLayout) findViewById(R.id.settings_layout);
        this.mResetButton = (FrameLayout) findViewById(R.id.reset_button);
        this.mProgressHandler = ProgressHandler.getInstance(this);
        this.mProgressHandler.addObserver(this);
        this.mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        ArrayList<String> categoryStringList = new ArrayList<>();
        for (Integer intValue : Category.ID_TO_CATEGORY_MAP.keySet()) {
            int key = intValue.intValue();
            if (!(key == 0 || key == 6)) {
                categoryStringList.add(Category.ID_TO_CATEGORY_MAP.get(Integer.valueOf(key)));
            }
        }
        Collections.sort(categoryStringList);
        categoryStringList.add(0, Category.ID_TO_CATEGORY_MAP.get(0));
        categoryStringList.add(categoryStringList.size(), Category.ID_TO_CATEGORY_MAP.get(6));
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.dark_spinner_item, categoryStringList);
        categoryAdapter.setDropDownViewResource(R.layout.dark_spinner_dropdown_item);
        this.mCategorySpinner.setAdapter(categoryAdapter);
        this.mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SimCollectionActivity.hideKeyboard();
                String unused = SimCollectionActivity.this.categoryFilterString = parent.getSelectedItem().toString() + "," + 2;
                ((SimulationGridItemAdapter) SimCollectionActivity.this.mGridView.getAdapter()).getFilter().filter(SimCollectionActivity.this.categoryFilterString);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                SimCollectionActivity.hideKeyboard();
            }
        });
        ArrayList<String> gradeLevelStringList = new ArrayList<>(Arrays.asList(new String[]{"0", "1", "2", "3", "4"}));
        for (Integer intValue2 : GradeLevel.GRADE_LEVEL_MAP.keySet()) {
            int key2 = intValue2.intValue();
            gradeLevelStringList.set(key2, GradeLevel.GRADE_LEVEL_MAP.get(Integer.valueOf(key2)));
        }
        ArrayAdapter<String> gradeLevelAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.dark_spinner_item, gradeLevelStringList);
        gradeLevelAdapter.setDropDownViewResource(R.layout.dark_spinner_dropdown_item);
        this.mGradeLevelSpinner.setAdapter(gradeLevelAdapter);
        this.mGradeLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SimCollectionActivity.hideKeyboard();
                String unused = SimCollectionActivity.this.gradeLevelFilterString = parent.getSelectedItem().toString() + "," + 8;
                ((SimulationGridItemAdapter) SimCollectionActivity.this.mGridView.getAdapter()).getFilter().filter(SimCollectionActivity.this.gradeLevelFilterString);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                SimCollectionActivity.hideKeyboard();
            }
        });
        this.mAutoCompleteTextView = (EditText) findViewById(R.id.search_text);
        this.mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_focus);
        this.mAppInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AppInfoDialogFragment().show(SimCollectionActivity.this.getFragmentManager(), "app_info");
            }
        });
        this.mSettingsLayout.setVisibility(View.GONE);
        this.mSearchToggleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimCollectionActivity.this.mSearchToggleView.setBackgroundResource(R.drawable.search_light);
                SimCollectionActivity.this.mSearchToggleView.setTextColor(ContextCompat.getColor(SimCollectionActivity.this.getApplicationContext(), R.color.light_theme_text));
                SimCollectionActivity.this.mSearchLayout.setVisibility(View.VISIBLE);
                SimCollectionActivity.this.mSettingsToggleView.setBackgroundResource(R.drawable.settings_dark);
                SimCollectionActivity.this.mSettingsToggleView.setTextColor(ContextCompat.getColor(SimCollectionActivity.this.getApplicationContext(), R.color.collection_view_text));
                SimCollectionActivity.this.mSettingsLayout.setVisibility(View.GONE);
            }
        });
        this.mSettingsToggleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimCollectionActivity.this.mSearchToggleView.setBackgroundResource(R.drawable.search_dark);
                SimCollectionActivity.this.mSearchToggleView.setTextColor(ContextCompat.getColor(SimCollectionActivity.this.getApplicationContext(), R.color.collection_view_text));
                SimCollectionActivity.this.mSearchLayout.setVisibility(View.GONE);
                SimCollectionActivity.this.mSettingsToggleView.setBackgroundResource(R.drawable.settings_light);
                SimCollectionActivity.this.mSettingsToggleView.setTextColor(ContextCompat.getColor(SimCollectionActivity.this.getApplicationContext(), R.color.light_theme_text));
                SimCollectionActivity.this.mSettingsLayout.setVisibility(View.VISIBLE);
            }
        });
        this.mResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(SimCollectionActivity.self).setMessage(R.string.reset_confirmation).setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SimulationDbHelper dbHelper = SimulationDbHelper.getInstance(SimCollectionActivity.this.getApplicationContext());
                        dbHelper.resetFavorites();
                        Iterator<Simulation> it = dbHelper.getSimulations().iterator();
                        while (it.hasNext()) {
                            ((SimulationGridItemAdapter) SimCollectionActivity.this.mGridView.getAdapter()).updateFavoriteStatus(it.next().getName());
                        }
                        if (SimCollectionActivity.this.categoryFilterString.equals("Favorites,2")) {
                            String unused = SimCollectionActivity.this.categoryFilterString = "All,2";
                            SimCollectionActivity.this.mCategorySpinner.setSelection(0);
                            ((SimulationGridItemAdapter) SimCollectionActivity.this.mGridView.getAdapter()).getFilter().filter(SimCollectionActivity.this.categoryFilterString);
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).show();
            }
        });
        this.mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String unused = SimCollectionActivity.this.searchFilterString = s.toString().replace(",", "") + "," + 1;
                ((SimulationGridItemAdapter) SimCollectionActivity.this.mGridView.getAdapter()).getFilter().filter(SimCollectionActivity.this.searchFilterString);
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void launchSimulation(View view) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.dismiss();
        }
        String[] simInfo = ((String) view.getTag()).split("\t");
        if (SimulationFiles.simulationFileIsValid(simInfo[0], getApplicationContext())) {
            Intent intent = new Intent(this, SimulationActivity.class);
            intent.putExtra(SIM_NAME, simInfo[0]);
            intent.putExtra(SIM_TITLE, simInfo[1]);
            intent.putExtra(SIM_IS_FAVORITE, Boolean.parseBoolean(simInfo[2]));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
            return;
        }
        Toast.makeText(this, getResources().getString(R.string.still_downloading), Toast.LENGTH_SHORT).show();
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_FILTER_STRING, this.searchFilterString);
        outState.putString(CATEGORY_FILTER_STRING, this.categoryFilterString);
        outState.putString(GRADE_LEVEL_FILTER_STRING, this.gradeLevelFilterString);
        outState.putBoolean(SEARCH_LAYOUT_IS_VISIBLE_BOOL, this.mSearchLayout.getVisibility() == View.VISIBLE);
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (this.mGridView.getAdapter() == null) {
            this.mGridView.setAdapter(new SimulationGridItemAdapter(this, new ArrayList(SimulationDbHelper.getInstance(getApplicationContext()).getSimulations())));
        }
        this.searchFilterString = savedInstanceState.getString(SEARCH_FILTER_STRING);
        this.categoryFilterString = savedInstanceState.getString(CATEGORY_FILTER_STRING);
        this.gradeLevelFilterString = savedInstanceState.getString(GRADE_LEVEL_FILTER_STRING);
        if (!savedInstanceState.getBoolean(SEARCH_LAYOUT_IS_VISIBLE_BOOL)) {
            this.mSearchToggleView.setBackgroundResource(R.drawable.search_dark);
            this.mSearchToggleView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.collection_view_text));
            this.mSearchLayout.setVisibility(View.GONE);
            this.mSettingsToggleView.setBackgroundResource(R.drawable.settings_light);
            this.mSettingsToggleView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light_theme_text));
            this.mSettingsLayout.setVisibility(View.VISIBLE);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        resize();
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        if (stat.getBlockSizeLong() * stat.getAvailableBlocksLong() < 31457280) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.insufficient_storage_title));
            alertDialog.setMessage(getResources().getString(R.string.insufficient_storage_update));
            alertDialog.setButton(-1, getResources().getString(R.string.insufficient_storage_close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SimCollectionActivity.this.finish();
                }
            });
            alertDialog.setButton(-2, getResources().getString(R.string.insufficient_storage_continue), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else {
            SimulationDbHelper.getInstance(getApplicationContext()).updateSimulationsFromServer();
        }
        ((SimulationGridItemAdapter) this.mGridView.getAdapter()).notifyDataSetChanged();
        this.mAutoCompleteTextView.clearFocus();
        this.mLinearLayout.requestFocus();
    }

    private void resize() {
        switch (getResources().getConfiguration().screenLayout & 15) {
            case 1:
                this.mGridView.setNumColumns(1);
                break;
            case 2:
                this.mGridView.setNumColumns(2);
                break;
            case 3:
                this.mGridView.setNumColumns(3);
                break;
            case 4:
                this.mGridView.setNumColumns(4);
                break;
            default:
                this.mGridView.setNumColumns(3);
                break;
        }
        this.mGridView.setAdapter(new SimulationGridItemAdapter(this, new ArrayList(SimulationDbHelper.getInstance(getApplicationContext()).getSimulations())));
        ((SimulationGridItemAdapter.CompositeFilter) ((SimulationGridItemAdapter) this.mGridView.getAdapter()).getFilter()).resume(this.searchFilterString, this.categoryFilterString, this.gradeLevelFilterString);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.mProgressHandler.deleteObserver(this);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 19:
                if (this.currentlyFocusedView == null || !this.currentlyFocusedView.hasFocus()) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView.getParent().getParent() == this.mGridView) {
                    if (this.currentGridViewFocusPosition >= this.mGridView.getNumColumns()) {
                        this.mGridView.post(new Runnable() {
                            public void run() {
                                int unused = SimCollectionActivity.this.currentGridViewFocusPosition = SimCollectionActivity.this.currentGridViewFocusPosition - SimCollectionActivity.this.mGridView.getNumColumns();
                                SimCollectionActivity.this.mGridView.smoothScrollToPosition(SimCollectionActivity.this.currentGridViewFocusPosition);
                                SimCollectionActivity.this.mGridView.setSelection(SimCollectionActivity.this.currentGridViewFocusPosition);
                                SimCollectionActivity.this.mGridView.post(new Runnable() {
                                    public void run() {
                                        for (int i = 0; i < SimCollectionActivity.this.mGridView.getChildCount(); i++) {
                                            if (((Integer) SimCollectionActivity.this.mGridView.getChildAt(i).getTag(0x7F0C0000)).intValue() == SimCollectionActivity.this.currentGridViewFocusPosition) {
                                                SimCollectionActivity.this.currentlyFocusedView.clearFocus();
                                                View unused = SimCollectionActivity.this.currentlyFocusedView = SimCollectionActivity.this.mGridView.getChildAt(i).findViewById(R.id.info);
                                                SimCollectionActivity.this.currentlyFocusedView.requestFocus();
                                                return;
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                } else if (this.currentlyFocusedView.getParent().getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = ((View) this.currentlyFocusedView.getParent().getParent()).findViewById(R.id.image_frame_layout);
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mAppInfoButton) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSettingsToggleView;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mSettingsToggleView || this.currentlyFocusedView == this.mSearchToggleView) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView.clearFocus();
                        this.currentlyFocusedView = this.mGradeLevelSpinner;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView.clearFocus();
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView == this.mGradeLevelSpinner) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mCategorySpinner;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mCategorySpinner) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mAutoCompleteTextView;
                    this.currentlyFocusedView.requestFocus();
                }
                return true;
            case 20:
                if (this.currentlyFocusedView == null || !this.currentlyFocusedView.hasFocus()) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView.getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = ((View) this.currentlyFocusedView.getParent()).findViewById(R.id.info);
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView.getParent().getParent().getParent() == this.mGridView) {
                    if (this.currentGridViewFocusPosition + this.mGridView.getNumColumns() < this.mGridView.getAdapter().getCount()) {
                        this.mGridView.post(new Runnable() {
                            public void run() {
                                int unused = SimCollectionActivity.this.currentGridViewFocusPosition = SimCollectionActivity.this.currentGridViewFocusPosition + SimCollectionActivity.this.mGridView.getNumColumns();
                                SimCollectionActivity.this.mGridView.smoothScrollToPosition(SimCollectionActivity.this.currentGridViewFocusPosition);
                                SimCollectionActivity.this.mGridView.setSelection(SimCollectionActivity.this.currentGridViewFocusPosition);
                                SimCollectionActivity.this.mGridView.post(new Runnable() {
                                    public void run() {
                                        for (int i = 0; i < SimCollectionActivity.this.mGridView.getChildCount(); i++) {
                                            if (((Integer) SimCollectionActivity.this.mGridView.getChildAt(i).getTag(0x7F0C0000)).intValue() == SimCollectionActivity.this.currentGridViewFocusPosition) {
                                                SimCollectionActivity.this.currentlyFocusedView.clearFocus();
                                                View unused = SimCollectionActivity.this.currentlyFocusedView = SimCollectionActivity.this.mGridView.getChildAt(i).findViewById(R.id.image_frame_layout);
                                                SimCollectionActivity.this.currentlyFocusedView.requestFocus();
                                                return;
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                } else if (this.currentlyFocusedView == this.mAutoCompleteTextView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mCategorySpinner;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mCategorySpinner) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mGradeLevelSpinner;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mGradeLevelSpinner) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSearchToggleView;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mSearchToggleView || this.currentlyFocusedView == this.mSettingsToggleView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mAppInfoButton;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mResetButton) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSearchToggleView;
                    this.currentlyFocusedView.requestFocus();
                }
                return true;
            case 21:
                if (this.currentlyFocusedView == null || !this.currentlyFocusedView.hasFocus()) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView == this.mAppInfoButton) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSettingsToggleView;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mSettingsToggleView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSearchToggleView;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView.getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    if (this.currentGridViewFocusPosition % this.mGridView.getNumColumns() == 0) {
                        this.currentlyFocusedView = this.currentlyFocusedView.focusSearch(View.FOCUS_LEFT);
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentGridViewFocusPosition--;
                        int i = 0;
                        while (i < this.mGridView.getChildCount()) {
                            if (((Integer) this.mGridView.getChildAt(i).getTag(0x7F0C0000)).intValue() == this.currentGridViewFocusPosition) {
                                this.currentlyFocusedView = this.mGridView.getChildAt(i).findViewById(R.id.info);
                                break;
                            } else {
                                i++;
                            }
                        }
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView.getParent().getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = ((View) this.currentlyFocusedView.getParent().getParent()).findViewById(R.id.image_frame_layout);
                    this.currentlyFocusedView.requestFocus();
                }
                return true;
            case 22:
                if (this.currentlyFocusedView == null || !this.currentlyFocusedView.hasFocus()) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView == this.mSearchToggleView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mSettingsToggleView;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView == this.mSettingsToggleView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = this.mAppInfoButton;
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView.getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = ((View) this.currentlyFocusedView.getParent()).findViewById(R.id.info);
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView.getParent().getParent().getParent() == this.mGridView) {
                    if (!((this.currentGridViewFocusPosition + 1) % this.mGridView.getNumColumns() == 0 || this.currentGridViewFocusPosition + 1 == this.mGridView.getAdapter().getCount())) {
                        this.currentGridViewFocusPosition++;
                        for (int i2 = 0; i2 < this.mGridView.getChildCount(); i2++) {
                            if (((Integer) this.mGridView.getChildAt(i2).getTag(0x7F0C0000)).intValue() == this.currentGridViewFocusPosition) {
                                this.currentlyFocusedView.clearFocus();
                                this.currentlyFocusedView = this.mGridView.getChildAt(i2).findViewById(R.id.image_frame_layout);
                                this.currentlyFocusedView.requestFocus();
                            }
                        }
                    }
                } else if (this.mGridView.getVisibility() != View.GONE) {
                    this.mGridView.post(new Runnable() {
                        public void run() {
                            SimCollectionActivity.this.mGridView.smoothScrollToPosition(0);
                            SimCollectionActivity.this.mGridView.setSelection(0);
                            int unused = SimCollectionActivity.this.currentGridViewFocusPosition = 0;
                            SimCollectionActivity.this.mGridView.post(new Runnable() {
                                public void run() {
                                    View unused = SimCollectionActivity.this.currentlyFocusedView = SimCollectionActivity.this.mGridView.getChildAt(0).findViewById(R.id.image_frame_layout);
                                    SimCollectionActivity.this.currentlyFocusedView.requestFocus();
                                }
                            });
                        }
                    });
                }
                return true;
            case 61:
                if (this.currentlyFocusedView == null || !this.currentlyFocusedView.hasFocus()) {
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView == this.mAppInfoButton) {
                    if (this.mGridView.getVisibility() != View.GONE) {
                        this.mGridView.post(new Runnable() {
                            public void run() {
                                SimCollectionActivity.this.mGridView.smoothScrollToPosition(0);
                                SimCollectionActivity.this.mGridView.setSelection(0);
                                int unused = SimCollectionActivity.this.currentGridViewFocusPosition = 0;
                                SimCollectionActivity.this.mGridView.post(new Runnable() {
                                    public void run() {
                                        SimCollectionActivity.this.currentlyFocusedView.clearFocus();
                                        View unused = SimCollectionActivity.this.currentlyFocusedView = SimCollectionActivity.this.mGridView.getChildAt(0).findViewById(R.id.image_frame_layout);
                                        SimCollectionActivity.this.currentlyFocusedView.requestFocus();
                                    }
                                });
                            }
                        });
                    } else if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView.clearFocus();
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        this.currentlyFocusedView.clearFocus();
                        this.currentlyFocusedView = this.mResetButton;
                        this.currentlyFocusedView.requestFocus();
                    }
                } else if (this.currentlyFocusedView.getParent().getParent() == this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = ((View) this.currentlyFocusedView.getParent()).findViewById(R.id.info);
                    this.currentlyFocusedView.requestFocus();
                } else if (this.currentlyFocusedView.getParent().getParent().getParent() != this.mGridView) {
                    this.currentlyFocusedView.clearFocus();
                    this.currentlyFocusedView = findViewById(this.currentlyFocusedView.getNextFocusForwardId());
                    if (this.currentlyFocusedView != null) {
                        this.currentlyFocusedView.requestFocus();
                    } else {
                        findViewById(R.id.search_text).requestFocus();
                    }
                } else if (this.currentGridViewFocusPosition == this.mGridView.getAdapter().getCount() - 1) {
                    this.currentGridViewFocusPosition = 0;
                    this.currentlyFocusedView.clearFocus();
                    if (this.mSearchLayout.getVisibility() == View.VISIBLE) {
                        this.currentlyFocusedView = this.mAutoCompleteTextView;
                    } else {
                        this.currentlyFocusedView = this.mResetButton;
                    }
                    this.currentlyFocusedView.requestFocus();
                } else {
                    this.mGridView.post(new Runnable() {
                        public void run() {
                            int unused = SimCollectionActivity.this.currentGridViewFocusPosition = SimCollectionActivity.this.currentGridViewFocusPosition + 1;
                            SimCollectionActivity.this.mGridView.smoothScrollToPosition(SimCollectionActivity.this.currentGridViewFocusPosition);
                            SimCollectionActivity.this.mGridView.setSelection(SimCollectionActivity.this.currentGridViewFocusPosition);
                            SimCollectionActivity.this.mGridView.post(new Runnable() {
                                public void run() {
                                    for (int i = 0; i < SimCollectionActivity.this.mGridView.getChildCount(); i++) {
                                        if (((Integer) SimCollectionActivity.this.mGridView.getChildAt(i).getTag(0x7F0C0000)).intValue() == SimCollectionActivity.this.currentGridViewFocusPosition) {
                                            SimCollectionActivity.this.currentlyFocusedView.clearFocus();
                                            View unused = SimCollectionActivity.this.currentlyFocusedView = SimCollectionActivity.this.mGridView.getChildAt(i).findViewById(R.id.image_frame_layout);
                                            SimCollectionActivity.this.currentlyFocusedView.requestFocus();
                                            return;
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void setupFullscreenMode() {
        setSystemUiVisilityMode().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & 4) == 0) {
                    View unused = SimCollectionActivity.setSystemUiVisilityMode();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static View setSystemUiVisilityMode() {
        View decorView = self.getWindow().getDecorView();
        decorView.setSystemUiVisibility(5894);
        self.getWindow().addFlags(1024);
        return decorView;
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            Log.e(TAG, "Error in update:" + arg);
        } else if (this.mProgressHandler == o) {
            try {
                final int progress = this.mProgressHandler.getProgress();
                if (progress != -1) {
                    this.mProgressBar.setVisibility(View.VISIBLE);
                    this.mProgressBar.post(new Runnable() {
                        public void run() {
                            SimCollectionActivity.this.mProgressBar.setProgress(progress);
                        }
                    });
                    return;
                }
                this.mProgressBar.post(new Runnable() {
                    public void run() {
                        SimCollectionActivity.this.mProgressBar.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
        }
    }

    public void setCategory(String category) {
        if (!this.categoryFilterString.startsWith(category)) {
            this.mCategorySpinner.setSelection(Category.CATEGORY_TO_ID_MAP.get(category).intValue());
            if (this.mSettingsLayout.getVisibility() == View.VISIBLE) {
                this.categoryFilterString = category + "," + 2;
                ((SimulationGridItemAdapter) this.mGridView.getAdapter()).getFilter().filter(this.categoryFilterString);
            }
        }
    }

    public static void hideKeyboard() {
        try {
            ((InputMethodManager) self.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(self.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resize();
    }

    @SuppressLint({"NewApi"})
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideKeyboard();
        setSystemUiVisilityMode();
    }
}
