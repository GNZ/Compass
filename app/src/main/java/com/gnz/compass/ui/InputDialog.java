package com.gnz.compass.ui;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.gnz.compass.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InputDialog extends DialogFragment {

    private final static String TITLE = "title";
    private final static String LOCATION = "location";
    private final static String CURRENT_VALUE = "current";

    @Bind(R.id.location_textview)
    TextView locationTextView;

    @Bind(R.id.input_edittext)
    EditText input;

    public static InputDialog newInstance(int title, String location, String current) {
        InputDialog frag = new InputDialog();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        args.putString(LOCATION, location);
        args.putString(CURRENT_VALUE, current);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt(TITLE);
        String location = getArguments().getString(LOCATION);
        String current = getArguments().getString(CURRENT_VALUE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputString = input.getText().toString();
                                ((FragmentInputDialog) getActivity()).doPositiveClick(inputString);
                            }
                        }
                )
                .setNegativeButton(R.string.dialog_negative_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentInputDialog) getActivity()).doNegativeClick();
                            }
                        }
                );

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.fragment_input_dialog, null);
        ButterKnife.bind(this, rootView);

        locationTextView.setText(location);
        input.setText(current);

        builder.setView(rootView);

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
