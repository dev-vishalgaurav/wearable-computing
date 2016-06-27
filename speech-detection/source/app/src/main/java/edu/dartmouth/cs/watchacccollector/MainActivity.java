package edu.dartmouth.cs.watchacccollector;

import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import edu.dartmouth.cs.watchacccollector.audio.MicrophoneRecorder;

public class MainActivity extends WearableActivity implements MicrophoneRecorder.MicrophoneListener {

    private BoxInsetLayout mContainerView;

    private final static String TAG = "RecorderActivity";
    private final int CAPACITY = MicrophoneRecorder.frequency * 60 * 15; // split files by 15 minute each
    private static String filename = null;
    private static DataOutputStream dos = null;
    private static int totalWritten = 0;
    private static final int THRESHOLD = 22;

    /*
     * Various UI components
	 */
    private Button recordButton;
    private TextView mTxtAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTxtAudio = (TextView)findViewById(R.id.textView);

        //Set the buttons and the text accordingly
        recordButton = (Button) findViewById(R.id.RecordButton);

        recordButton.setText(MicrophoneRecorder.getInstance().isRecording() ? R.string.stop : R.string.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MicrophoneRecorder recorder = MicrophoneRecorder.getInstance();
                if (recorder.isRecording()) {
                    Log.d(TAG, "stopping recording");
                    recorder.stopRecording();
                    recordButton.setText(R.string.record);
                    mTxtAudio.setText("");
                    //closeOutAudioFile();
                } else {
                    Log.d(TAG, "starting recording");
                    recorder.registerListener(MainActivity.this);
                    recorder.startRecording();
                    recordButton.setText(R.string.stop);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    protected String generateFileName() {
        return "MICR_" + Calendar.getInstance().getTimeInMillis();
    }


    private File getStorageLocation() {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return root;
    }

    private void setupNewAudioFile() {
        try {
            filename = generateFileName() + ".pcm";
            File audioFile = new File(getStorageLocation(), filename + ".inprogress");

            Log.d(TAG, "created new audioFile: " + audioFile.getAbsolutePath());
            OutputStream os = new FileOutputStream(audioFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeOutAudioFile() {
        try {
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File audioFile = new File(getStorageLocation(), filename + ".inprogress");
        audioFile.renameTo(new File(getStorageLocation(), filename));                // mark file as ready to upload
        filename = null;                                        // reset filename so new file is created
        totalWritten = 0;                                        // reset totalWritten
        Log.d(TAG, "closed out file: " + audioFile.getAbsolutePath());
    }

    private Object[] result = new Object[12];

//    @Override
//    public void microphoneBuffer(short[] buffer, int window_size) {
//        Log.d(TAG, "received buffer");
//        try {
//            if (filename == null) {                    // this is a new file, create it
//                setupNewAudioFile();
//            }
//            for (int i = 0; i < window_size; i++) {    // write to file
//                dos.writeShort(buffer[i]);
//            }
//            totalWritten += window_size;            // increment total written
//            if (totalWritten >= CAPACITY) {            // Did we reach the capacity for current file?
//                closeOutAudioFile();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static final int WINDOWS_N = 40;

    @Override
    //This method was declared in an interface in MicrophoneRecorder
    public void microphoneBuffer(short[] buffer, int window_size) {

        //You will break a chunk of one-second-long samples into multiple 25-ms windows. Think about how many of 25-ms windows you will get for a second.

        //number of 25-ms-long windows that contains mostly voice in a second
        int voiced = 0;
        Log.e("VVV","microphoneBuffer" + window_size);
        //TODO: Fill out appropriate numbers in the following for loop statement
        int sampleSize = MicrophoneRecorder.BUFFER_SIZE / WINDOWS_N;
        //Log.e("VVV","Sample size = " + sampleSize + " Buffer size = " + MicrophoneRecorder.BUFFER_SIZE);
        for (int k = 0; k < MicrophoneRecorder.BUFFER_SIZE; k += sampleSize) {
            //TODO: replace ‘??’ with appropriate numbers below
            double[] features = FeatureExtractor.ComputeFeaturesForFrame(buffer, sampleSize, k);
            try {
                result = getObjectDoubleArray(features, result);
                double classifYResult = WekaClassifier.classify(result);
                //Log.e("VVV","classifYResult = " + classifYResult);
                if(classifYResult == 0.0d) {
                    voiced++;
                }
                //TODO: classify whether the window is voiced or not
                //If output of the classifier is 0.0d, increment ‘voiced’ variable. If output is 1.0d, it is unvoiced. This is assuming that you have the order of classes written in arff file as: “speech{true,false}.”

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("VVV","voiced" + voiced);
        updateSpeechToUI(voiced >= THRESHOLD ? 1: 0);
    }

    private void updateSpeechToUI(final int speaking){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(speaking == 1){
                    mTxtAudio.setText(getString(R.string.speaking));
                }else{
                    mTxtAudio.setText("");
                }
            }
        });
    }

    public Object[] getObjectDoubleArray(double[] in, Object[] result) {
        //Log.e("VVV", " STARTED ");
        for (int i = 0; i < in.length; i++) {
            result[i] = new Double(in[i]);
            //Log.e("VVV", "" + in[i]);
        }
        //Log.e("VVV", " ENDED ");
        return result;
    }
}


