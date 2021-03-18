package com.jinung.edu.sims;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jinung.edu.sims.data.Category;
import com.jinung.edu.sims.data.Simulation;
import com.jinung.edu.sims.data.SimulationDbHelper;
import com.jinung.edu.sims.data.SimulationFiles;

public class SimInfoDialogFragment extends DialogFragment {
    private static final String SIM_CATEGORIES = "sim-categories";
    private static final String TAG = "SimInfoDialog";

    public static SimInfoDialogFragment newInstance(Simulation simulation) {
        SimInfoDialogFragment fragment = new SimInfoDialogFragment();
        Bundle bundle = new Bundle(6);
        bundle.putString(SimCollectionActivity.SIM_NAME, simulation.getName());
        bundle.putString(SimCollectionActivity.SIM_TITLE, simulation.getTitle());
        bundle.putString(SimCollectionActivity.SIM_DESCRIPTION, simulation.getDescription());
        bundle.putBoolean(SimCollectionActivity.SIM_IS_FAVORITE, simulation.isFavorite());
        StringBuilder categoryStringBuilder = new StringBuilder();
        for (Integer intValue : simulation.getCategoryIds()) {
            int i = intValue.intValue();
            if (!(i == 0 || i == 6)) {
                categoryStringBuilder.append(Category.ID_TO_CATEGORY_MAP.get(Integer.valueOf(i)));
                categoryStringBuilder.append("'");
            }
        }
        if (categoryStringBuilder.length() != 0) {
            bundle.putString(SIM_CATEGORIES, categoryStringBuilder.toString().substring(0, categoryStringBuilder.length() - 1));
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogFragmentTheme);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.sim_info, (ViewGroup) null);
        Bundle arguments = getArguments();
        boolean isFavorite = arguments.getBoolean(SimCollectionActivity.SIM_IS_FAVORITE);
        String simulationName = arguments.getString(SimCollectionActivity.SIM_NAME);
        String simulationTitle = arguments.getString(SimCollectionActivity.SIM_TITLE);
        String categoryString = arguments.getString(SIM_CATEGORIES);
        FrameLayout[] categoryViewArray = {(FrameLayout) view.findViewById(R.id.category_text_1), (FrameLayout) view.findViewById(R.id.category_text_2), (FrameLayout) view.findViewById(R.id.category_text_3), (FrameLayout) view.findViewById(R.id.category_text_4), (FrameLayout) view.findViewById(R.id.category_text_5)};
        categoryViewArray[0].setNextFocusForwardId(R.id.category_text_2);
        categoryViewArray[0].setNextFocusRightId(R.id.category_text_2);
        categoryViewArray[1].setNextFocusForwardId(R.id.category_text_3);
        categoryViewArray[1].setNextFocusRightId(R.id.category_text_3);
        categoryViewArray[2].setNextFocusForwardId(R.id.category_text_4);
        categoryViewArray[2].setNextFocusRightId(R.id.category_text_4);
        categoryViewArray[3].setNextFocusForwardId(R.id.category_text_5);
        categoryViewArray[3].setNextFocusRightId(R.id.category_text_5);
        categoryViewArray[4].setNextFocusForwardId(R.id.sim_info_favorite_image);
        categoryViewArray[4].setNextFocusRightId(R.id.sim_info_favorite_image);
        if (categoryString != null) {
            String[] categoryStringArray = categoryString.split("'");
            int i = 0;
            while (i < categoryStringArray.length) {
                final String category = categoryStringArray[i];
                ((TextView) categoryViewArray[i].getChildAt(0)).setText(category.toUpperCase());
                categoryViewArray[i].setContentDescription(category.toUpperCase() + " filter button");
                categoryViewArray[i].setVisibility(View.VISIBLE);
                categoryViewArray[i].setFocusable(true);
                categoryViewArray[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ((SimCollectionActivity) SimInfoDialogFragment.this.getActivity()).setCategory(category);
                        SimInfoDialogFragment.this.dismiss();
                    }
                });
                if (i + 1 == categoryStringArray.length) {
                    categoryViewArray[i].setNextFocusForwardId(R.id.sim_info_favorite_image);
                    categoryViewArray[i].setNextFocusRightId(R.id.sim_info_favorite_image);
                }
                i++;
            }
            while (i < categoryViewArray.length) {
                ((TextView) categoryViewArray[i].getChildAt(0)).setText("");
                categoryViewArray[i].setFocusable(false);
                categoryViewArray[i].setVisibility(View.GONE);
                i++;
            }
        } else {
            int length = categoryViewArray.length;
            for (int i2 = 0; i2 < length; i2++) {
                categoryViewArray[i2].setVisibility(View.GONE);
            }
        }
        final FrameLayout imageViewFrame = (FrameLayout) view.findViewById(R.id.sim_info_image);
        String simInfo = arguments.getString(SimCollectionActivity.SIM_NAME) + "\t" + simulationTitle + "\t" + isFavorite;
        imageViewFrame.setTag(simInfo);
        Glide.with(getActivity()).load((Object) SimulationFiles.getSimulationDirectoryPath(simulationName, getActivity()) + SimulationFiles.getSimulationImageFilename(simulationName)).into((ImageView) imageViewFrame.getChildAt(0));
//        Glide.with(getActivity()).load((Object) SimulationFiles.getSimulationDirectoryPath(simulationName, getActivity()) + SimulationFiles.getSimulationImageFilename(simulationName)).apply(new RequestOptions().placeholder((int) R.drawable.phet_logo)).into((ImageView) imageViewFrame.getChildAt(0));
//        GlideApp.with(view).load((Object) SimulationFiles.getSimulationDirectoryPath(simulationName, getActivity()) + SimulationFiles.getSimulationImageFilename(simulationName)).placeholder((int) R.drawable.phet_logo).into((ImageView) imageViewFrame.getChildAt(0));
        ((TextView) view.findViewById(R.id.title)).setText(simulationTitle);
        final TextView descriptionView = (TextView) ((FrameLayout) view.findViewById(R.id.description)).getChildAt(0);
        descriptionView.setText(arguments.getString(SimCollectionActivity.SIM_DESCRIPTION));
        descriptionView.post(new Runnable() {
            public void run() {
                descriptionView.setLines(descriptionView.getLineCount());
            }
        });
        FrameLayout favoriteButtonFrame = (FrameLayout) view.findViewById(R.id.sim_info_favorite_image);
        ImageView favoriteButton = (ImageView) favoriteButtonFrame.getChildAt(0);
        favoriteButtonFrame.setTag(simInfo);
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.heart_filled);
            favoriteButtonFrame.setContentDescription(getResources().getString(R.string.selected_favorite_a11y));
        } else {
            favoriteButton.setImageResource(R.drawable.heart_empty);
            favoriteButtonFrame.setContentDescription(getResources().getString(R.string.unselected_favorite_a11y));
        }
        view.findViewById(R.id.empty_for_talkback).setContentDescription(getResources().getString(R.string.about) + " " + simulationTitle);
        imageViewFrame.setContentDescription(getResources().getString(R.string.play) + " " + simulationTitle);
        favoriteButtonFrame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean z;
                boolean z2 = true;
                String[] simInfo = ((String) v.getTag()).split("\t");
                boolean isFavorite = Boolean.parseBoolean(simInfo[2]);
                FrameLayout favoriteButtonFrame = (FrameLayout) v;
                if (isFavorite) {
                    ((ImageView) favoriteButtonFrame.getChildAt(0)).setImageResource(R.drawable.heart_empty);
                    favoriteButtonFrame.setContentDescription(SimInfoDialogFragment.this.getResources().getString(R.string.unselected_favorite_a11y));
                } else {
                    ((ImageView) favoriteButtonFrame.getChildAt(0)).setImageResource(R.drawable.heart_filled);
                    favoriteButtonFrame.setContentDescription(SimInfoDialogFragment.this.getResources().getString(R.string.selected_favorite_a11y));
                }
                SimulationDbHelper instance = SimulationDbHelper.getInstance(SimInfoDialogFragment.this.getActivity().getApplicationContext());
                String str = simInfo[0];
                if (!isFavorite) {
                    z = true;
                } else {
                    z = false;
                }
                instance.updateFavorite(str, z);
                StringBuilder append = new StringBuilder().append(simInfo[0]).append("\t").append(simInfo[1]).append("\t");
                if (isFavorite) {
                    z2 = false;
                }
                String tagString = append.append(z2).toString();
                v.setTag(tagString);
                imageViewFrame.setTag(tagString);
                ((SimulationGridItemAdapter) ((SimCollectionActivity) SimInfoDialogFragment.this.getActivity()).mGridView.getAdapter()).updateFavoriteStatus(simInfo[0]);
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimInfoDialogFragment.this.dismiss();
            }
        });
        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.6f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
