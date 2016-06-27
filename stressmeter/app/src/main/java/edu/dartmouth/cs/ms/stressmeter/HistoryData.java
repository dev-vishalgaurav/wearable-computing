package edu.dartmouth.cs.ms.stressmeter;

/**
 * Created by Vishal Gaurav
 */
public class HistoryData {
    public long time;
    public int stressInitial;
    public int stressFinal;

    public HistoryData(String line) {
        String[] values = line.split(",");
        time = Long.parseLong(values[0]);
        stressInitial = Integer.parseInt(values[1]);
        stressFinal = Integer.parseInt(values[2]);
    }

    public int getLevel(){
        int result = 0 ;
        switch (stressInitial){
            case 1:{
                result = stressFinal + 1;
            }
            break;
            case 2:{
                result = stressFinal + 6;
            }
            break;
            case 3:{
                result = stressFinal + 11;
            }
            break;

        }
        return result;
    }
}
