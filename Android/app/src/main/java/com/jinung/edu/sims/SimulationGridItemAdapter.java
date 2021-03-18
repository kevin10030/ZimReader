package com.jinung.edu.sims;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jinung.edu.sims.data.Category;
import com.jinung.edu.sims.data.GradeLevel;
import com.jinung.edu.sims.data.Simulation;
import com.jinung.edu.sims.data.SimulationDbHelper;
import com.jinung.edu.sims.data.SimulationFiles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

class SimulationGridItemAdapter extends ArrayAdapter<Simulation> implements Filterable {
    static final int CATEGORY_FILTER = 2;
    static final int GRADE_LEVEL_FILTER = 8;
    static final int SEARCH_FILTER = 1;
    private String TAG = "SimulationGridItemAdapt";
    /* access modifiers changed from: private */
    public Context context;
    /* access modifiers changed from: private */
    public final ArrayList<Simulation> gridValues;
    private CompositeFilter mCompositeFilter;
    private HashMap<String, Drawable> screenshotMap;

    SimulationGridItemAdapter(Context context2, ArrayList<Simulation> gridValues2) {
        super(context2, 0, gridValues2);
        this.context = context2;
        this.gridValues = gridValues2;
        Collections.sort(gridValues2, Simulation.getSimulationComparator());
    }

    public int getCount() {
        return this.gridValues.size();
    }

    public Simulation getItem(int position) {
        return this.gridValues.get(position);
    }

    public long getItemId(int position) {
        return (long) this.gridValues.get(position).getId();
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final View gridItemView;
        ViewHolderItem viewHolder;
        String simulationName = this.gridValues.get(position).getName();
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            gridItemView = inflater.inflate(R.layout.simulation_grid_item, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.titleTextView = (TextView) gridItemView.findViewById(R.id.title);
            viewHolder.screenshotImageView = (ImageView) gridItemView.findViewById(R.id.grid_item_image);
            viewHolder.infoImageContainerView = (FrameLayout) gridItemView.findViewById(R.id.info);
            viewHolder.infoImageView = (ImageView) viewHolder.infoImageContainerView.getChildAt(0);
            viewHolder.favoriteImageView = (ImageView) gridItemView.findViewById(R.id.grid_item_favorite_image);
            viewHolder.gridItemBottomLayout = (RelativeLayout) gridItemView.findViewById(R.id.grid_item_text_and_info);
            viewHolder.imageFrameLayout = (FrameLayout) viewHolder.screenshotImageView.getParent();
            gridItemView.setTag(0x7F0C0001, viewHolder);
        } else {
            gridItemView = convertView;
            viewHolder = (ViewHolderItem) gridItemView.getTag(0x7F0C0001);
        }
        gridItemView.setTag(0x7F0C0000, Integer.valueOf(position));
        TextView textView = viewHolder.titleTextView;
        String simulationTitle = this.gridValues.get(position).getTitle();
        textView.setText(simulationTitle);
        boolean isFavorite = this.gridValues.get(position).isFavorite();
        viewHolder.imageFrameLayout.setTag(simulationName + "\t" + simulationTitle + "\t" + isFavorite);

        Glide.with(context).load(SimulationFiles.getSimulationDirectoryPath(simulationName, this.context) + SimulationFiles.getSimulationImageFilename(simulationName)).into(viewHolder.screenshotImageView);
//        Glide.with(gridItemView).load(SimulationFiles.getSimulationDirectoryPath(simulationName, this.context) + SimulationFiles.getSimulationImageFilename(simulationName)).into(viewHolder.screenshotImageView);
//        Glide.with(gridItemView).load(SimulationFiles.getSimulationDirectoryPath(simulationName, this.context) + SimulationFiles.getSimulationImageFilename(simulationName)).into(viewHolder.screenshotImageView);
//        GlideApp.with(gridItemView).load((Object) SimulationFiles.getSimulationDirectoryPath(simulationName, this.context) + SimulationFiles.getSimulationImageFilename(simulationName)).placeholder((int) R.drawable.phet_logo_grey).error((int) R.drawable.phet_logo_grey).into(viewHolder.screenshotImageView);
        ImageView favoriteImageView = viewHolder.favoriteImageView;
        if (isFavorite) {
            favoriteImageView.setImageResource(R.drawable.heart_filled);
        } else {
            favoriteImageView.setImageDrawable((Drawable) null);
        }
        viewHolder.imageFrameLayout.setContentDescription(getContext().getResources().getString(R.string.play) + " " + simulationTitle);
        viewHolder.infoImageContainerView.setContentDescription(getContext().getResources().getString(R.string.about) + " " + simulationTitle);
        if (SimulationFiles.simulationFileExists(simulationName, getContext().getApplicationContext())) {
            final int i = position;
            viewHolder.infoImageContainerView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    DialogFragment newFragment = SimInfoDialogFragment.newInstance((Simulation) SimulationGridItemAdapter.this.gridValues.get(i));
                    newFragment.show(((Activity) SimulationGridItemAdapter.this.context).getFragmentManager(), "sim_info");
                    ((SimCollectionActivity) SimulationGridItemAdapter.this.context).mDialogFragment = newFragment;
                }
            });
            viewHolder.imageFrameLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((SimCollectionActivity) SimulationGridItemAdapter.this.getContext()).launchSimulation(v);
                }
            });
            gridItemView.setOnClickListener((View.OnClickListener) null);
        } else {
            viewHolder.imageFrameLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(SimulationGridItemAdapter.this.getContext(), SimulationGridItemAdapter.this.getContext().getResources().getString(R.string.still_downloading), Toast.LENGTH_SHORT).show();
                }
            });
            gridItemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(SimulationGridItemAdapter.this.getContext(), SimulationGridItemAdapter.this.getContext().getResources().getString(R.string.still_downloading), Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.infoImageContainerView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(SimulationGridItemAdapter.this.getContext(), SimulationGridItemAdapter.this.getContext().getResources().getString(R.string.still_downloading), Toast.LENGTH_SHORT).show();
                }
            });
        }
        final ImageView imageView = viewHolder.screenshotImageView;
        gridItemView.post(new Runnable() {
            public void run() {
                int gridItemWidth = ((View) imageView.getParent()).getWidth() - ((View) imageView.getParent().getParent()).getPaddingStart();
                if (gridItemWidth != imageView.getLayoutParams().width) {
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(gridItemWidth, (int) (((double) gridItemWidth) * 0.65666667d)));
                    gridItemView.postInvalidate();
                }
            }
        });
        return gridItemView;
    }

    /* access modifiers changed from: package-private */
    public void updateFavoriteStatus(String updatedSimulationName) {
        boolean found = false;
        Iterator<Simulation> it = this.gridValues.iterator();
        while (it.hasNext()) {
            Simulation currSim = it.next();
            if (currSim.getName().equals(updatedSimulationName)) {
                Iterator<Simulation> it2 = SimulationDbHelper.getInstance(getContext().getApplicationContext()).getSimulations().iterator();
                while (true) {
                    if (!it2.hasNext()) {
//                        continue;
                        break;
                    }
                    Simulation updatedSim = it2.next();
                    if (updatedSim.getName().equals(updatedSimulationName)) {
                        currSim.setFavorite(updatedSim.isFavorite());
                        notifyDataSetChanged();
                        found = true;
//                        continue;
                        break;
                    }
                }
            }
            if (found) {
                return;
            }
        }
    }

    public void setItems(ArrayList<Simulation> list) {
        this.gridValues.clear();
        this.gridValues.addAll(list);
        Collections.sort(this.gridValues, Simulation.getSimulationComparator());
        notifyDataSetChanged();
    }

    private static class ViewHolderItem {
        ImageView favoriteImageView;
        RelativeLayout gridItemBottomLayout;
        FrameLayout imageFrameLayout;
        FrameLayout infoImageContainerView;
        ImageView infoImageView;
        ImageView screenshotImageView;
        TextView titleTextView;

        private ViewHolderItem() {
        }
    }

    @NonNull
    public synchronized Filter getFilter() {
        if (this.mCompositeFilter == null) {
            this.mCompositeFilter = new CompositeFilter();
        }
        return this.mCompositeFilter;
    }

    class CompositeFilter extends Filter {
        private CharSequence categoryConstraint;
        private CharSequence gradeLevelConstraint;
        private boolean isCategoryFiltering = false;
        private boolean isGradeLevelFiltering = false;
        private boolean isSearching = false;
        private CharSequence searchConstraint;

        CompositeFilter() {
        }

        /* access modifiers changed from: protected */
        public synchronized Filter.FilterResults performFiltering(CharSequence constraint) {
            Filter.FilterResults results;
            String[] constraintAndKey = constraint.toString().split(",", -1);
            switch (Integer.parseInt(constraintAndKey[1])) {
                case SEARCH_FILTER:
                    this.searchConstraint = constraintAndKey[0];
                    this.isSearching = true;
                    break;
                case CATEGORY_FILTER:
                    this.categoryConstraint = constraintAndKey[0];
                    this.isCategoryFiltering = true;
                    break;
                case GRADE_LEVEL_FILTER:
                    this.gradeLevelConstraint = constraintAndKey[0];
                    this.isGradeLevelFiltering = true;
                    break;
            }
            ArrayList<Simulation> values = new ArrayList<>(SimulationDbHelper.getInstance(SimulationGridItemAdapter.this.getContext().getApplicationContext()).getSimulations());
            if (this.isSearching) {
                performSearchFiltering(this.searchConstraint, values);
            }
            if (this.isCategoryFiltering) {
                performCategoryFiltering(this.categoryConstraint, values);
            }
            if (this.isGradeLevelFiltering) {
                performGradeLevelFiltering(this.gradeLevelConstraint, values);
            }
            results = new Filter.FilterResults();
            results.values = values;
            results.count = values.size();
            return results;
        }

        /* access modifiers changed from: protected */
        public void publishResults(CharSequence constraint, Filter.FilterResults results) {
            View gridView = ((SimCollectionActivity) SimulationGridItemAdapter.this.getContext()).findViewById(R.id.grid_view);
            View noResultsTextView = ((SimCollectionActivity) SimulationGridItemAdapter.this.getContext()).findViewById(R.id.no_results);
            if (results.count != 0) {
                if (gridView.getVisibility() == View.GONE) {
                    gridView.setVisibility(View.VISIBLE);
                    noResultsTextView.setVisibility(View.GONE);
                }
                SimulationGridItemAdapter.this.setItems((ArrayList) results.values);
            } else if (noResultsTextView.getVisibility() == View.GONE) {
                gridView.setVisibility(View.GONE);
                noResultsTextView.setVisibility(View.VISIBLE);
            }
        }

        private void performSearchFiltering(CharSequence constraint, ArrayList<Simulation> values) {
            if (constraint == null || constraint.length() == 0) {
                this.isSearching = false;
                return;
            }
            ArrayList<Simulation> valuesToRemove = new ArrayList<>();
            Iterator<Simulation> it = values.iterator();
            while (it.hasNext()) {
                Simulation sim = it.next();
                if (!sim.getTitle().toLowerCase().contains(constraint.toString().toLowerCase()) && !sim.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    valuesToRemove.add(sim);
                }
            }
            values.removeAll(valuesToRemove);
        }

        private void performCategoryFiltering(CharSequence constraint, ArrayList<Simulation> values) {
            if (constraint == null || constraint.length() == 0 || constraint.equals(Category.ID_TO_CATEGORY_MAP.get(0))) {
                this.isCategoryFiltering = false;
            } else if (constraint.equals(Category.ID_TO_CATEGORY_MAP.get(6))) {
                ArrayList<Simulation> valuesToRemove = new ArrayList<>();
                Iterator<Simulation> it = values.iterator();
                while (it.hasNext()) {
                    Simulation sim = it.next();
                    if (!sim.isFavorite()) {
                        valuesToRemove.add(sim);
                    }
                }
                values.removeAll(valuesToRemove);
            } else {
                ArrayList<Simulation> valuesToRemove2 = new ArrayList<>();
                Iterator<Simulation> it2 = values.iterator();
                while (it2.hasNext()) {
                    Simulation sim2 = it2.next();
                    boolean found = false;
                    Iterator<Integer> it3 = sim2.getCategoryIds().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        if (constraint.equals(Category.ID_TO_CATEGORY_MAP.get(Integer.valueOf(it3.next().intValue())))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        valuesToRemove2.add(sim2);
                    }
                }
                values.removeAll(valuesToRemove2);
            }
        }

        private void performGradeLevelFiltering(CharSequence constraint, ArrayList<Simulation> values) {
            if (constraint == null || constraint.length() == 0 || constraint.equals(GradeLevel.GRADE_LEVEL_MAP.get(0))) {
                this.isGradeLevelFiltering = false;
                return;
            }
            ArrayList<Simulation> valuesToRemove = new ArrayList<>();
            Iterator<Simulation> it = values.iterator();
            while (it.hasNext()) {
                Simulation sim = it.next();
                boolean found = false;
                Iterator<Integer> it2 = sim.getGradeLevelIds().iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    if (constraint.equals(GradeLevel.GRADE_LEVEL_MAP.get(Integer.valueOf(it2.next().intValue())))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    valuesToRemove.add(sim);
                }
            }
            values.removeAll(valuesToRemove);
        }

        /* access modifiers changed from: package-private */
        public void resume(String searchFilterString, String categoryFilterString, String gradeLevelFilterString) {
            boolean allNull = true;
            if (searchFilterString != null) {
                this.isSearching = true;
                this.searchConstraint = searchFilterString.split(",")[0];
                allNull = false;
            }
            if (categoryFilterString != null) {
                this.isCategoryFiltering = true;
                this.categoryConstraint = categoryFilterString.split(",")[0];
                allNull = false;
            }
            if (gradeLevelFilterString != null) {
                this.isGradeLevelFiltering = true;
                this.gradeLevelConstraint = gradeLevelFilterString.split(",")[0];
                allNull = false;
            }
            if (!allNull) {
                publishResults("", performFiltering(",-1"));
            }
        }
    }
}
