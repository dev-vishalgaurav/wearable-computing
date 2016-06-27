package edu.dartmouth.cs.ms.stressmeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FinalScreening extends Activity {

    private TextView mTextView;
    private ImageView mImgStress1;
    private ImageView mImgStress2;
    private ImageView mImgStress3;
    private ImageView mImgStress4;
    private TextView mBtnMore;
    int moreValue = 0 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_screening);
        initViews();
        setResult(Activity.RESULT_CANCELED);
    }

    private void initViews() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mImgStress1 = (ImageView) findViewById(R.id.imgStress1);
                mImgStress2 = (ImageView) findViewById(R.id.imgStress2);
                mImgStress3 = (ImageView) findViewById(R.id.imgStress3);
                mImgStress4 = (ImageView) findViewById(R.id.imgStress4);
                mBtnMore = (TextView)findViewById(R.id.btnMore);
                mImgStress1.setOnClickListener(mOnClickListener);
                mImgStress2.setOnClickListener(mOnClickListener);
                mImgStress3.setOnClickListener(mOnClickListener);
                mImgStress4.setOnClickListener(mOnClickListener);
                mBtnMore.setOnClickListener(mOnClickListener);
                updateImages(getIntent());

            }
        });

    }

    private int getMoreValue(){
        int result = moreValue;
        moreValue = (moreValue >= 2) ? 0 : ++moreValue;
        return result;
    }

    private void updateImages(Intent intent) {
        Log.e("VVV","updateImages :- " + moreValue);
        int[] images = Utils.getImagesForLevel(Utils.getStressLevel(intent,Constants.STRESS_LEVEL_DEFAULT),getMoreValue());
        if(images!=null){
            mImgStress1.setImageResource(images[0]);
            mImgStress2.setImageResource(images[1]);
            mImgStress3.setImageResource(images[2]);
            mImgStress4.setImageResource(images[3]);
        }
    }

    private void sendResultWithStressLevel(int finalStressLevel){
        Intent intent = getIntent();
//        int stressLevel = Utils.getStressLevel(intent, -1);
//        if(stressLevel != -1 && finalStressLevel != -1){
//            // record data
//            //Utils.recordStress(getBaseContext(),stressLevel,finalStressLevel);
//        }
        intent.putExtra(Constants.EXTRA_STRESS_FINAL_LEVEL,finalStressLevel);
        setResult(RESULT_OK, intent);
        finishActivity(IntitialScreening.REQUEST_CODE_FINAL_SCREEN);
        finish();
    }
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnMore:{
                    updateImages(getIntent());
                }
                break;
//                case R.id.btnBack:{
//                    finish();
//                }
//                break;
                case R.id.imgStress1:{
                    sendResultWithStressLevel(1);
                }
                break;
                case R.id.imgStress2:{
                    sendResultWithStressLevel(2);
                }
                break;
                case R.id.imgStress3:{
                    sendResultWithStressLevel(3);
                }
                break;
                case R.id.imgStress4:{
                    sendResultWithStressLevel(4);

                }
                break;
            }
        }
    };
}
