package edu.dartmouth.cs.ms.stressmeter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends Activity {



    private static class ViewTag{
        private TextView txtDate;
        private TextView txtStress;
    }

    private static class HistoryAdapter extends BaseAdapter{
        private Context mContext;
        private List<HistoryData> mListData;

        public HistoryAdapter(Context mContext, List<HistoryData> mListData) {
            this.mContext = mContext;
            this.mListData = mListData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_history_row,null);
                ViewTag tag = new ViewTag();
                tag.txtDate = (TextView)convertView.findViewById(R.id.txtDate);
                tag.txtStress = (TextView)convertView.findViewById(R.id.txtStressLevel);
                convertView.setTag(tag);
            }
            ViewTag tag = (ViewTag)convertView.getTag();
            HistoryData data = (HistoryData)getItem(position);
            tag.txtDate.setText(Utils.getFormattedData(data.time,Utils.DATE_FORMAT_HISTORY));
            tag.txtStress.setText(""+data.getLevel());
            return convertView;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public int getCount() {
            return mListData.size();
        }
    }
    private ListView mLstHistory;
    private HistoryAdapter mHistoryAdapter ;
    private View mListHeader;
    private List<HistoryData> mListDataHistory = new ArrayList<>();
    private DataLoader mDataLoader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initViews();
    }

    private void initViews() {
        mHistoryAdapter = new HistoryAdapter(getBaseContext(),mListDataHistory);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListHeader = LayoutInflater.from(getBaseContext()).inflate(R.layout.layout_list_history_row, null);
                ((TextView) mListHeader.findViewById(R.id.txtDate)).setText(getString(R.string.date));
                ((TextView) mListHeader.findViewById(R.id.txtDate)).setGravity(Gravity.LEFT);
                ((TextView) mListHeader.findViewById(R.id.txtStressLevel)).setText(getString(R.string.stress_level));
                mLstHistory = (ListView) findViewById(R.id.lstHistory);
                mLstHistory.addHeaderView(mListHeader, null, false);
                mLstHistory.setEmptyView(findViewById(R.id.txtEmpty));
                mLstHistory.setAdapter(mHistoryAdapter);
                loadData();
            }
        });
    }
    private void loadData() {
        mDataLoader = new DataLoader();
        mDataLoader.execute();
    }
    private class DataLoader extends AsyncTask<Void,Void,List<HistoryData>>{
        private Context mContext;

        @Override
        protected List<HistoryData> doInBackground(Void... params) {
            List<HistoryData> result = null;
            try {
                result = FileUtils.getHistory();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  result;
        }

        @Override
        protected void onPostExecute(List<HistoryData> historyDatas) {
            if(historyDatas!=null){
                mListDataHistory.clear();
                mListDataHistory.addAll(historyDatas);
                mHistoryAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(historyDatas);
        }
    }
}
