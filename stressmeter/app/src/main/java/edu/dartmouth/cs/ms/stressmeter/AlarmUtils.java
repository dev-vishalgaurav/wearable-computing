package edu.dartmouth.cs.ms.stressmeter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Vishal Gaurav
 */
public class AlarmUtils {

    public static final int ALARM_TIME_MIN = 2 ;
    public static final int SEC_IN_MIN = 60;
    public static final long MILLI_SECS = 1000;
    public static final long ALARM_TIME = ALARM_TIME_MIN * SEC_IN_MIN * MILLI_SECS ;

    public static final void setNextAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ALARM_TIME , getNext(context));
    }

    public static PendingIntent getNext(Context context){
        Intent intent = new Intent(context,IntitialScreening.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent result = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return  result;
    }
}
