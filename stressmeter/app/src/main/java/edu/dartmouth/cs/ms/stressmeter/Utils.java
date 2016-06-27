package edu.dartmouth.cs.ms.stressmeter;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Vishal Gaurav
 */
public class Utils {

    public static final String DATE_FORMAT_HISTORY = "HH:mm MM/yy";

    public static final int getStressLevel(Intent intent,int defaultLevel) {
        Log.e("VVV", "stress level = " + intent.getIntExtra(Constants.EXTRA_STRESS_LEVEL, defaultLevel) + " default = " + defaultLevel);
        return (intent.hasExtra(Constants.EXTRA_STRESS_LEVEL)) ? intent.getIntExtra(Constants.EXTRA_STRESS_LEVEL, defaultLevel) : defaultLevel;
    }
    public static final int getFinalStressLevel(Intent intent,int defaultLevel) {
        return (intent.hasExtra(Constants.EXTRA_STRESS_FINAL_LEVEL)) ? intent.getIntExtra(Constants.EXTRA_STRESS_FINAL_LEVEL, defaultLevel) : defaultLevel;
    }

    public static final int[] getImagesForLevel(int stressLevel, int moreValue) {
        int[] result = null;
        Log.e("VVV","getImagesForLevel  Stress level = " + stressLevel);
        switch (stressLevel) {
            case Constants.STRESS_LEVEL_HIGH: {
                result = getHighStressImage(moreValue);
                Log.e("VVV","HIGH");
            }
            break;
            case Constants.STRESS_LEVEL_MEDIUM: {
                result = getMediumStressImage(moreValue);
                Log.e("VVV","medium");
            }
            break;
            case Constants.STRESS_LEVEL_LOW: {
                result = getLowStressImage(moreValue);
                Log.e("VVV","low");
            }
            break;
            default: {
                // this should never happen
            }
            break;
        }
        return result;
    }

    private static final int[] getLowStressImage(int random) {
        int[] result = null;
        switch (random){
            case 0:{
                result =  new int[]{R.drawable.s2_1,R.drawable.s3_1,R.drawable.s4_1,R.drawable.s5_1};
            }
            break;
            case 1:{
                result =  new int[]{R.drawable.s2_2,R.drawable.s3_2,R.drawable.s4_2,R.drawable.s5_2};
            }
            break;
            case 2: {
                result = new int[]{R.drawable.s2_3, R.drawable.s3_3, R.drawable.s4_3, R.drawable.s5_3};
            }
            break;
            default:{
                result =  new int[]{R.drawable.s2_1,R.drawable.s3_1,R.drawable.s4_1,R.drawable.s5_1};
            }
            break;
        }
        return result;
    }

    private static final int[] getHighStressImage(int moreValue) {
        int[] result = null;
        switch (moreValue){
            case 0:{
                result =  new int[]{R.drawable.s7_1,R.drawable.s8_1,R.drawable.s9_1,R.drawable.s10_1};
            }
            break;
            case 1:{
                result =  new int[]{R.drawable.s7_2,R.drawable.s8_2,R.drawable.s9_2,R.drawable.s10_2};
            }
            break;
            case 2:{
                result =  new int[]{R.drawable.s7_3,R.drawable.s8_3,R.drawable.s9_3,R.drawable.s10_3};
            }
            break;
            default:{
                result =  new int[]{R.drawable.s7_1,R.drawable.s8_1,R.drawable.s9_1,R.drawable.s10_1};
            }
            break;
        }
        return result;
    }

    private static final int[] getMediumStressImage(int moreValue) {
        int[] result = null;
        switch (moreValue){
            case 0:{
                result =  new int[]{R.drawable.s12_1,R.drawable.s13_1,R.drawable.s14_1,R.drawable.s15_1};
            }
            break;
            case 1:{
                result =  new int[]{R.drawable.s12_2,R.drawable.s13_2,R.drawable.s14_2,R.drawable.s15_2};
            }
            break;
            case 2:{
                result =  new int[]{R.drawable.s12_3,R.drawable.s13_3,R.drawable.s14_3,R.drawable.s15_3};
            }
            break;
            case 3:{
                result =  new int[]{R.drawable.s12_1,R.drawable.s13_1,R.drawable.s14_1,R.drawable.s15_1};
            }
            break;
            default:{
                result =  new int[]{R.drawable.s12_1,R.drawable.s13_1,R.drawable.s14_1,R.drawable.s15_1};
            }
            break;
        }
        return result;
    }

    public static void recordStress(Context baseContext, int stressLevel, int finalStressLevel) {
        // record data add to file
        Log.e("VVV", "Stress Initial :- " + stressLevel + " Stress Level Final :-" + finalStressLevel);
        try {
            FileUtils.appendToFile(System.currentTimeMillis(), stressLevel, finalStressLevel);
        }catch (Exception ex){
            Log.e("VVV",ex.getMessage());
        }
    }

    public static final void vibrate(Context context){
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    public static final String getFormattedData(long timeinMillis,String format){
        String result = null;
        SimpleDateFormat formattter = new SimpleDateFormat(format, Locale.getDefault());
        result = formattter.format(new Date(timeinMillis));
        return result;
    }
}
