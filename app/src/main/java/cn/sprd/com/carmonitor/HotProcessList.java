package cn.sprd.com.carmonitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by SPREADTRUM\joe.yu on 7/31/15.
 */
public class HotProcessList {
    UsageStatsManager mUm;
    private SimpleDateFormat mDataFomat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
    private String TAG = "HotProcessList";
    private static final int PROCESS_COUNT = 5;
    private List<UsageStats> mStats = new ArrayList<UsageStats>();
    private static final long PROCESS_POLL_DELAY = 3 * 60 * 1000;//3 hours
    private Context mContext;
    private Object mLock = new Object();
    private MonitorThread mThread = new MonitorThread();
    private Comparator<UsageStats> mComparator = new Comparator<UsageStats>() {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (int) (rhs.getTotalTimeInForeground() - lhs.getTotalTimeInForeground());
        }
    };
    private Comparator<UsageStats> mComparatorCounts = new Comparator<UsageStats>() {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return 0;//(int) (rhs.mLaunchCount - lhs.mLaunchCount);
        }
    };

    public HotProcessList(Context context) {
        mContext = context;
    }

    public void startMonitor() {
        mThread.start();
    }

    public boolean isHot(String packageName) {
        synchronized (mLock) {
            for (UsageStats usage : mStats) {
                if (usage.getPackageName().equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
    how to judge a Process is hot or not ?
        the process has been started top the most  PROCESS_COUNT  during PROCESS_POLL_DELAY.
        there are two kinds of hot processes ,we want the most frequncy used process can stay in
        the RAM as long as possible to get smoothly user experience.

    * */
    public void updateUseageStats() {
        if (mUm == null) {
            mUm = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        if (mUm != null) {
            int interval = UsageStatsManager.INTERVAL_WEEKLY;
            Calendar cal = Calendar.getInstance();
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_MONTH, -1);
            long beginTime = cal.getTimeInMillis();
            Log.e(TAG, "updateUseageStats:");
            Log.e(TAG, "begin = " + mDataFomat.format(beginTime));
            Log.e(TAG, "end = " + mDataFomat.format(endTime));
            List<UsageStats> tmp = mUm.queryUsageStats(interval, beginTime, endTime);
            if (tmp != null) {
                Collections.sort(tmp, mComparatorCounts); //sort by launch counts.
                synchronized (mLock) {
                    mStats.clear();
                    for (int i = 0; i < tmp.size() && i < PROCESS_COUNT; i++) {
                        UsageStats stat = tmp.get(i);
                        Log.e(TAG, "PackageName " + stat.getPackageName() + "Total time " + stat.getTotalTimeInForeground());
                        mStats.add(stat);
                    }
                }
            }
        }
    }

    class MonitorThread extends Thread {
        public MonitorThread() {
            super("hot process monitor");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    updateUseageStats();
                    sleep(PROCESS_POLL_DELAY);
                } catch (Exception e) {
                }
            }


        }
    }
}
