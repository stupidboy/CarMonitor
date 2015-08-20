package cn.sprd.com.carmonitor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.io.File;

/**
 * Created by SPREADTRUM\joe.yu on 8/18/15.
 */
public class MiniFileExplorer extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    PreferenceScreen mScreen;
    private static String DIR_PATH = "/sdcard/rec/";
    private File mDirFile ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.fileexplorer);
        mScreen = this.getPreferenceScreen();
        mDirFile = new File(DIR_PATH);
        String [] allFiles = mDirFile.list();
        for(String tmp :allFiles){
            Preference pref = new Preference(this);
            pref.setTitle(tmp);
            pref.setKey(DIR_PATH+tmp);
            mScreen.addPreference(pref);
            pref.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Uri uri = Uri.fromFile(new File(preference.getKey()));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/mpeg4");
        startActivity(intent);
        return false;
    }
}
