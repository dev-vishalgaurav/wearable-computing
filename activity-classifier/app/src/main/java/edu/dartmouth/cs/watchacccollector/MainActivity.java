package edu.dartmouth.cs.watchacccollector;

import android.app.NotificationManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.dartmouth.cs.watchacccollector.accelerometer.Filter;

public class MainActivity extends WearableActivity {

    private BoxInsetLayout mContainerView;

    private enum State {
        IDLE, COLLECTING, TRAINING, CLASSIFYING
    }

    public static final String[] mLabels = {Globals.CLASS_LABEL_STANDING,
            Globals.CLASS_LABEL_WALKING, Globals.CLASS_LABEL_RUNNING,
            Globals.CLASS_LABEL_OTHER};

    private RadioGroup radioGroup;
    private final RadioButton[] radioBtns = new RadioButton[4];
    private Intent mServiceIntent;
    private File mFeatureFile;

    private State mState;
    private Button btnDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroupLabels);
        radioBtns[0] = (RadioButton) findViewById(R.id.radioStanding);
        radioBtns[1] = (RadioButton) findViewById(R.id.radioWalking);
        radioBtns[2] = (RadioButton) findViewById(R.id.radioRunning);
        radioBtns[3] = (RadioButton) findViewById(R.id.radioOther);

        btnDelete = (Button) findViewById(R.id.btnDeleteData);

        mState = State.IDLE;
        mFeatureFile = new File(getExternalFilesDir(null),
                Globals.FEATURE_FILE_NAME);
        mServiceIntent = new Intent(this, SensorsService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop the service and the notification.
        // Need to check whether the mSensorService is null or not.
        if (mState == State.TRAINING) {
            return;
        } else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
            stopService(mServiceIntent);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .cancel(Globals.NOTIFICATION_ID);
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }
    }

    public void onCollectClicked(View view) {

        if (mState == State.IDLE) {
            mState = State.COLLECTING;
            ((Button) view).setText(R.string.ui_collector_button_stop_title);
            btnDelete.setEnabled(false);
            radioBtns[0].setEnabled(false);
            radioBtns[1].setEnabled(false);
            radioBtns[2].setEnabled(false);
            radioBtns[3].setEnabled(false);

            int acvitivtyId = radioGroup.indexOfChild(findViewById(radioGroup
                    .getCheckedRadioButtonId()));
            String label = mLabels[acvitivtyId];

            Bundle extras = new Bundle();
            extras.putString(Globals.CLASS_LABEL_KEY, label);
            mServiceIntent.putExtras(extras);

            startService(mServiceIntent);

        } else if (mState == State.COLLECTING) {
            mState = State.IDLE;
            ((Button) view).setText(R.string.ui_collector_button_start_title);
            btnDelete.setEnabled(true);
            radioBtns[0].setEnabled(true);
            radioBtns[1].setEnabled(true);
            radioBtns[2].setEnabled(true);
            radioBtns[3].setEnabled(true);

            stopService(mServiceIntent);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        }
    }

    public void onDeleteDataClicked(View view) {

        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            if (mFeatureFile.exists()) {
                mFeatureFile.delete();
            }

            Toast.makeText(getApplicationContext(),
                    R.string.ui_collector_toast_file_deleted,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
