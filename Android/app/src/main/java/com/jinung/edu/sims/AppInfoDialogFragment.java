package com.jinung.edu.sims;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class AppInfoDialogFragment extends DialogFragment {
    @SuppressLint({"InflateParams"})
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.app_info, (ViewGroup) null);
        builder.setView(view);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppInfoDialogFragment.this.dismiss();
            }
        });
        return builder.create();
    }
}
