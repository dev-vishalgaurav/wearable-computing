package edu.dartmouth.cs.ms.stressmeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class IntitialScreening extends Activity {

    public static final int REQUEST_CODE_FINAL_SCREEN = 1;
    private TextView mTextView;
    private ImageView mImgLow;
    private ImageView mImgMedium;
    private ImageView mImgHigh;
    private Button mBtnHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intitial_screening);
        initViews();
        Utils.vibrate(getBaseContext());
    }

    private void initViews() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.txtStressed);
                mImgHigh = (ImageView) findViewById(R.id.imgHigh);
                mImgMedium = (ImageView) findViewById(R.id.imgMedium);
                mImgLow = (ImageView) findViewById(R.id.imgLow);
                mBtnHistory = (Button) findViewById(R.id.btnHistory);
                mImgHigh.setOnClickListener(mOnClickListener);
                mImgMedium.setOnClickListener(mOnClickListener);
                mImgLow.setOnClickListener(mOnClickListener);
                mBtnHistory.setOnClickListener(mOnClickListener);
            }
        });

    }

    private void startFinalScreening(int stressLevel) {
        Intent intent = new Intent(IntitialScreening.this, FinalScreening.class);
        intent.putExtra(Constants.EXTRA_STRESS_LEVEL,stressLevel);
        startActivityForResult(intent, REQUEST_CODE_FINAL_SCREEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_FINAL_SCREEN:{
                if(resultCode == Activity.RESULT_OK){
                    if(data != null){
                        int stressLevel = Utils.getStressLevel(data,-1);
                        int finalStressLevel = Utils.getFinalStressLevel(data,-1);
                        if(stressLevel != -1 && finalStressLevel != -1){
                            // record data
                            Utils.recordStress(getBaseContext(),stressLevel,finalStressLevel);
                        }
                    }
                    finish();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Log.e("VVV", "onDestroy");
        AlarmUtils.setNextAlarm(getBaseContext());
        super.onDestroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imgHigh: {
                    startFinalScreening(Constants.STRESS_LEVEL_HIGH);
                }
                break;
                case R.id.imgMedium: {
                    startFinalScreening(Constants.STRESS_LEVEL_MEDIUM);
                }
                break;
                case R.id.imgLow: {
                    startFinalScreening(Constants.STRESS_LEVEL_LOW);
                }
                break;
                case R.id.btnHistory: {
                    showHistory();
                }
                break;
            }
        }
    };

    private void showHistory() {
        Intent intent = new Intent(IntitialScreening.this,HistoryActivity.class);
        startActivity(intent);
    }
}
