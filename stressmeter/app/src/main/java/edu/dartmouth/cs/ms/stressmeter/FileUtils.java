package edu.dartmouth.cs.ms.stressmeter;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vishal Gaurav
 */
public class FileUtils {
    public static final String FILE_STRESS_DATA = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"stress_data.csv";

    public static void appendToFile(long time, int stressLevel, int finalStressLevel) throws IOException {
        File file = new File(FILE_STRESS_DATA);
        Log.e("vvv","writting to  :- " + file.toString());
        if(!file.exists()){
            file.createNewFile();
        }
        String writeData = time + "," + stressLevel + "," + finalStressLevel + "\n";
        FileWriter writer = new FileWriter(file,true);
        writer.append(writeData);
        writer.flush();
        writer.close();
    }

    public static List<HistoryData> getHistory() throws  IOException{
        File file = new File(FILE_STRESS_DATA);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        Log.e("vvv","reading from :- " + file.toString());
        String line = null;
        List<HistoryData> resultList = new ArrayList<>();
        while((line = reader.readLine()) != null){
            Log.e("vvv","line read :- " + line);
            resultList.add(0,new HistoryData(line)); // to reverse sort .. not a good way though
        }
        Log.e("vvv","result list size :- " + resultList.size());
        return resultList;
    }

}
