package com.gnz.compass.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gnz.compass.R;
import com.gnz.compass.core.RotaryView;
import com.gnz.compass.core.SensorsListener;
import com.gnz.compass.core.SensorsListenerContext;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SensorsListenerContext, FragmentInputDialog {

    static final int LAT = 0;
    static final int LON = 1;
    static final int EMPTY = 200;

    @Bind(R.id.imageView)
    View arrow;

    @Bind(R.id.imageViewArrow)
    View compassView;

    SensorsListener sensorsListener;
    RotaryView arrowView;
    RotaryView compass;
    int mode;
    boolean[] set = {false, false};
    float[] destination = {EMPTY, EMPTY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sensorsListener = new SensorsListener(this);
        arrowView = new RotaryView(arrow);
        compass = new RotaryView(compassView);
        compass.changeVisibilityView(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorsListener.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorsListener.stopListening();
    }

    @Override
    public void onSensorChange() {
        if (set[LAT] && set[LON]) {
            compass.changeVisibilityView(true);
            compass.doRotate(sensorsListener.getBearingToDestination());
        } else compass.changeVisibilityView(false);

        arrowView.doRotate(sensorsListener.getNorth());
    }

    @Override
    public void doPositiveClick(String value) {
        boolean valid = !value.equals("");
        float floatValue = EMPTY;

        try {
            floatValue = Float.parseFloat(value);

        } catch (NumberFormatException e) {
            valid = false;
        }

        if ((mode == LAT) && (floatValue > 90 || floatValue < -90))
            valid = false;
        if ((mode == LON) && (floatValue > 180 || floatValue < -180))
            valid = false;

        if (!valid) {
            Toast.makeText(MainActivity.this, R.string.not_valid_input, Toast.LENGTH_SHORT).show();
            destination[mode] = EMPTY;
        } else {
            destination[mode] = floatValue;
        }
        set[mode] = valid;

        if (set[LAT] && set[LON])
            sensorsListener.setDestinationLocation(destination[LAT], destination[LON]);
    }

    @Override
    public void doNegativeClick() {

    }

    @OnClick(R.id.latitude_button)
    void onLatitudeButtonClick() {
        mode = LAT;
        showInputDialog();
    }

    @OnClick(R.id.longitude_button)
    void onLongitudeButtonClick() {
        mode = LON;
        showInputDialog();
    }

    private void showInputDialog() {
        String currentDestination;
        InputDialog dialog;
        if (destination[mode] == EMPTY)
            currentDestination = "";
        else currentDestination = destination[mode] + "";

        if (mode == LAT) {
            String location = getResources().getString(R.string.latitude_dialog_message)
                    + ": " + sensorsListener.getCurrentLocation().getLatitude();
            dialog = InputDialog.newInstance(R.string.latitude_dialog_title,
                    location, currentDestination);
        } else {
            String location = getResources().getString(R.string.longitude_dialog_message)
                    + ": " + sensorsListener.getCurrentLocation().getLongitude();
            dialog = InputDialog.newInstance(R.string.longitude_dialog_title,
                    location, currentDestination);
        }
        dialog.show(getFragmentManager(), "dialog");
    }
}
