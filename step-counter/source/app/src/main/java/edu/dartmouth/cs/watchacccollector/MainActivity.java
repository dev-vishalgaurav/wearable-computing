package edu.dartmouth.cs.watchacccollector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.dartmouth.cs.watchacccollector.accelerometer.Filter;

public class MainActivity extends WearableActivity implements SensorEventListener {

    public static final float SMOOTHING_FACTOR = 0.3f; // got it through matlab data analysis
    public static final float MAX_STEP_PER_SEC = 5; // got it through matlab data analysis
    public static final int SAMPLING_RATE = 50; // got it through matlab data analysis
    public static final int WINDOW_FACTOR = 1; // got it through matlab data analysis
    public static final float MIN_STEP_DISTANCE = SAMPLING_RATE / MAX_STEP_PER_SEC; // got it through matlab data analysis
    public static final int WINDOW_SIZE = WINDOW_FACTOR * SAMPLING_RATE; // got it through matlab data analysis
    // for constant noise from accelerometer
    public static final double PEAK_TO_PEAK = 1.5;

    public static final double AVERAGE_HEIGHT_OF_MAN = 5.66; // 5 ft 8 inch = 5.66 ft
    public static final double AVERAGE_WEIGHT_OF_MAN = 74; //


    private BoxInsetLayout mContainerView;

    /**
     * Filter class required to filter noise from accelerometer
     */
    private Filter filter = null;

    /**
     * SensorManager
     */
    private SensorManager mSensorManager;
    /**
     * Accelerometer Sensor
     */
    private Sensor mAccelerometer;

    /**
     * Step count to be displayed in UI
     */
    private int stepCount = 0;

    private int totalCalorieBurnt;


    /**
     * Is accelerometer running?
     */
    private static boolean isAccelRunning = false;

    //Sensor data files
    private File mRawAccFile;
    private FileOutputStream mRawAccOutputStream;

    /*
     * Various UI components
	 */
    private TextView stepsView;
    private TextView calorieView;
    private CompoundButton accelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        // Init files
        mRawAccFile = new File(Environment.getExternalStorageDirectory(), "acc_raw.csv");
        Log.d("ACC_DATA_PATH", mRawAccFile.getAbsolutePath());
        try {
            mRawAccOutputStream = new FileOutputStream(mRawAccFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        stepsView = (TextView) findViewById(R.id.stepCount);
        calorieView = (TextView) findViewById(R.id.calorieCount);
        //Set the buttons and the text accordingly
        accelButton = (ToggleButton) findViewById(R.id.StartButton);
        accelButton.setChecked(isAccelRunning);
        accelButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                        if (!isAccelRunning) {
                            startAccelerometer();
                            accelButton.setChecked(true);
                        } else {
                            stopAccelerometer();
                            accelButton.setChecked(false);
                        }
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            mRawAccOutputStream.close();
        } catch (Exception ex) {
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

    /**
     * start accelerometer
     */
    private void startAccelerometer() {
        isAccelRunning = true;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        //Set up filter
        //Following sets up smoothing filter from mcrowdviz
        int SMOOTH_FACTOR = 10;
        filter = new Filter(SMOOTH_FACTOR);
        //OR Use Butterworth filter from mcrowdviz
        //double CUTOFF_FREQUENCY = 0.3;
        //filter = new Filter(CUTOFF_FREQUENCY);
        stepCount = 0;
    }

    /**
     * stop accelerometer
     */
    private void stopAccelerometer() {
        isAccelRunning = false;
        mSensorManager.unregisterListener(this);
        if(currentCount > 0 && currentCount < WINDOW_SIZE - 1 ) {
            getAndUpdateSteps();
        }
        //Free filter and step detector
        filter = null;
    }

    double[] sampleBuffer = new double[WINDOW_SIZE];
    int currentCount = 0;
    int currentWindow = 0 ;
    int currentWindowStep = 0 ;

    private void addToBuffer(double accX, double accY, double accZ) {
        sampleBuffer[currentCount] = Math.sqrt(accX * accX + accY * accY + accZ * accZ);
        currentCount++;
    }
    private void clearBuffer() {
        currentCount = 0;
        for (int i = 0; i < sampleBuffer.length; i++) {
            sampleBuffer[i] = 0.0;
        }

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            /**
             * TODO: Step Detection
             */
            float accel[] = event.values;
            sendAccelValuesToUI(accel[0], accel[1], accel[2]);
            if (currentCount < WINDOW_SIZE - 1) {
                addToBuffer(accel[0], accel[1], accel[2]);
            } else {
                // detect steps
                getAndUpdateSteps();
                clearBuffer();
            }
        }

    }

    private void getAndUpdateSteps() {
        int steps = getStepsFromWindow(sampleBuffer);
        updateCalories(steps);
        stepCount += steps;
        currentCount = 0;
        sendUpdatedStepCountToUI();
    }

    private void updateCalories(int steps) {
        currentWindow++;
        if(currentWindow == 2){ // according to formula defined in lecture note
            int calorieSteps = currentWindowStep + steps;
            int caloriesBurnt = getCaloriesFromSteps(calorieSteps);
            totalCalorieBurnt += caloriesBurnt;
            updateCalorieOnDisplay(caloriesBurnt);
            currentWindowStep = 0 ;
            currentWindow = 0;
        }else{
            currentWindowStep = steps;
        }
    }

    private void updateCalorieOnDisplay(int caloriesBurnt) {

        calorieView.setText(getString(R.string.calorie) + " " + totalCalorieBurnt);
    }

    private int getCaloriesFromSteps(int calorieSteps){
        int calorieBurnt  = 0;
        if(calorieSteps == 0 ){
            calorieBurnt = (int)AVERAGE_WEIGHT_OF_MAN / 1800 ; // 1800 is taken from formula
        }else{
            calorieBurnt = (int)((AVERAGE_WEIGHT_OF_MAN / 400 ) * getSpeedFromSteps(calorieSteps));
        }
        return  calorieBurnt;
    }

    private double getSpeedFromSteps(int calorieSteps) {
        double stride = 0 ;
        if(calorieSteps <= 1 ){
            stride = AVERAGE_HEIGHT_OF_MAN / 5 ;
        }else if(calorieSteps == 2){
            stride = AVERAGE_HEIGHT_OF_MAN / 4 ;
        }else if(calorieSteps == 3){
            stride = AVERAGE_HEIGHT_OF_MAN / 3 ;
        }else if(calorieSteps == 4){
            stride = AVERAGE_HEIGHT_OF_MAN / 2 ;
        }
        else if(calorieSteps == 5){
            stride = AVERAGE_HEIGHT_OF_MAN / 1.2 ;
        }else if(calorieSteps == 6 || calorieSteps == 7){
            stride = AVERAGE_HEIGHT_OF_MAN;
        }else{
            stride = 1.2 * AVERAGE_HEIGHT_OF_MAN;
        }

        return  (calorieSteps * stride )* 0.5; // as per speed formula given in lecture 3 notes
    }


    /* (non-Javadoc)
 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
 */
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    /**
     * This should return number of steps detected.
     *
     * @param filt_acc_x
     * @param filt_acc_y
     * @param filt_acc_z
     * @return
     */
    public int detectSteps(double filt_acc_x, double filt_acc_y, double filt_acc_z) {
        return 0;
    }

    private void sendAccelValuesToUI(float accX, float accY, float accZ) {
        /**
         * save raw data to a file
         */
        String record = System.currentTimeMillis() + "," +
                accX + "," + accY + "," + accZ + "\n";
        try {
            mRawAccOutputStream.write(record.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * show raw data on UI
         */
        Log.d("ACC_RAW", record);
    }

    private void sendUpdatedStepCountToUI() {
        stepsView.setText("Steps=" + stepCount);
    }

    private int getStepsFromWindow(double[] amplitude) {
        int steps = 0;
        double[] smoothedData = smoothValues(amplitude, SMOOTHING_FACTOR);
        boolean[] stepIndices = new boolean[smoothedData.length];
        // dynamic thresholding
        double[] minMax = getMinMax(smoothedData);
        double peakToPeak = minMax[1] - minMax[0];
        if (peakToPeak > PEAK_TO_PEAK) { // ignoring noise
            double threshold = getAverage(minMax); // to determine steps
            boolean isAboveThreshold = smoothedData[0] >= threshold;
            for (int i = 1; i < smoothedData.length; i++) {
                if (isAboveThreshold) {
                    if (smoothedData[i] < threshold) {
                        steps++;
                        stepIndices[i] = true;
                    }
                }
                isAboveThreshold = smoothedData[i] > threshold;
            }
            // below is the code for removing invalid steps.
            int lastStepIndex = -1;
            for (int i = 1; i < stepIndices.length; i++) {
                if (stepIndices[i]) {
                    boolean isFound = false;
                    for (int j = i + 1; j <= i + MIN_STEP_DISTANCE  && j < stepIndices.length; j++) {
                        if (stepIndices[j]) {
                            isFound = true;
                            lastStepIndex = j;
                            if (stepIndices[i]) {
                                steps--;
                                stepIndices[i] = false;
                            }
                            steps--;
                            stepIndices[i] = false;
                        }
                    }
                    if(!isFound && i > stepIndices.length && i - lastStepIndex > MIN_STEP_DISTANCE){
                        steps--;
                    }
                    i = i + (int) MIN_STEP_DISTANCE;
                }
            }
            steps = (steps >= MAX_STEP_PER_SEC) ? 0 : steps;
        }
        return steps;
    }

    private double getAverage(double[] values) {
        double result = 0;
        for (int i = 0; i < values.length; i++) {
            result += values[i];
        }
        result = result / values.length;
        return result;
    }

    /*
     * size returned will be 2 and 0 is min and 1 is max
     */
    private double[] getMinMax(double[] values) {
        double[] result = new double[2];
        result[0] = Double.MAX_VALUE; // min index
        result[1] = Double.MIN_VALUE; // max index

        for (int i = 0; i < values.length; i++) {
            result[0] = Math.min(result[0], values[i]);
            result[1] = Math.max(result[1], values[i]);
        }
        return result;
    }

    /**
     * as per this method in matlab
     * <p/>
     * function s = expsmooth(data, alpha)
     * % Calculates single exponentially smoothed data with weight parameter
     * % alpha.
     * <p/>
     * n = length(data);
     * s = zeros(1,n+1);
     * s(2) = data(1);
     * for i = 3:n+1
     * s(i) = alpha*data(i-1) + (1-alpha)*s(i-1);
     * end
     * end
     * [1,2,3,4,5,6]
     *
     * @param values
     * @param factor
     */
    private double[] smoothValues(double[] values, float factor) {

        double[] output = null;
        if (values != null && values.length > 3 && factor <= 1.0) {
            output = new double[values.length + 1];
            output[1] = values[0];
            for (int i = 2; i < output.length; i++) {
                output[i] = values[i - 1] * factor + output[i - 1] * (1 - factor);
            }
        }
        return output;
    }
}
