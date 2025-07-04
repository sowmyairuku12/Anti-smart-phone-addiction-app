package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Adaptor.AppAdaptor;
import com.example.antismartphoneaddictionapp.Dialog.AlertCustomTimeSettingsDialog;
import com.example.antismartphoneaddictionapp.Models.AppModel;
import com.example.antismartphoneaddictionapp.Utility.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

    ArrayList<AppModel> appModelArrayList = new ArrayList<>();

    RecyclerView appListRV;
    TextView totalTime;

    private Dialog dialog;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initObj();
    }

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    @SuppressLint("WrongConstant")
    private void initUI() {
//        getSupportActionBar().hide();

        // Load stored values when the app starts
        Constants.init(this);

        appListRV = findViewById(R.id.appListRV);
        totalTime = findViewById(R.id.totalTime);

        mPm = getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        } else {
            mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        }

        askPermission();

        long hour_in_mil = 1000 * 60 * 60; // In Milliseconds
        long end_time = System.currentTimeMillis();
        long start_time = end_time - hour_in_mil;

        Intent serviceIntent = new Intent(this, BackgroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void initObj() {
        context = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //adding settings menu for updating settings
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu); // Inflate the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // Handle the Settings menu item click
            dialog = new AlertCustomTimeSettingsDialog(context)
                    .openCustomSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//        getUsage();
//        showList();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mPackageStats.clear();  // Clear the old package stats to avoid duplication
        appModelArrayList.clear();  // Clear the list to prevent old data from staying
        getUsage();
        showList();
    }

    //    void askPermission() {
//        boolean granted = false;
//        AppOpsManager appOps = (AppOpsManager) this
//                .getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), this.getPackageName());
//
//        if (mode == AppOpsManager.MODE_DEFAULT) {
//            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
//        } else {
//            granted = (mode == AppOpsManager.MODE_ALLOWED);
//        }
//
//        if (!granted) {
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//        }
//    }
    void askPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        boolean usageAccessGranted = (mode == AppOpsManager.MODE_ALLOWED);
        boolean overlayPermissionGranted = Settings.canDrawOverlays(this);

        if (!usageAccessGranted) { //for getting information about apps usage timings
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required")
                    .setMessage("This app needs Usage Access permission to monitor app usage.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
        }

        if (!overlayPermissionGranted) { //for Navigating to Anti Smartphone Addiction app if foreground app is Restricted
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Overlay Permission Required")
                    .setMessage("This app needs permission to display a warning screen over restricted apps.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    void getUsage() {
        // Set calendar to today at 12:00 AM
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        cal.getTimeInMillis(), System.currentTimeMillis());

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            final UsageStats pkgStats = stats.get(i);
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);

                UsageStats existingStats = map.get(pkgStats.getPackageName());
                if (existingStats == null) {
                    map.put(pkgStats.getPackageName(), pkgStats);
                } else {
                    existingStats.add(pkgStats);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        mPackageStats.addAll(map.values());
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);
    }


    @SuppressLint("SetTextI18n")
    void showList() {
        mPm = getApplication().getPackageManager();
        long totalTimeList = 0;

        //Clear the existing List to avoid the duplication after the app is re-opened
        appModelArrayList.clear();

        for (int i = 0; i < mPackageStats.size(); i++) {
            UsageStats pkgStats = mPackageStats.get(i);
            if (pkgStats != null) {

                String timeUsed = String.valueOf(DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                if (!timeUsed.equalsIgnoreCase("00:00")) {
//                    Log.d("TAG", mAppLabelMap.get(pkgStats.getPackageName()) + "_________" + pkgStats.getTotalTimeInForeground());
                    appModelArrayList.add(new AppModel(mAppLabelMap.get(pkgStats.getPackageName()), pkgStats.getPackageName(), pkgStats.getTotalTimeInForeground()));

                }
                totalTimeList = totalTimeList + pkgStats.getTotalTimeInForeground();
            } else {
                Log.w("TAG", "No usage stats info for package:" + i);
            }
        }

        AppAdaptor appointmentAdaptor = new AppAdaptor(appModelArrayList, this);
        appListRV.setHasFixedSize(true);
        appListRV.setLayoutManager(new LinearLayoutManager(this));
        appListRV.setAdapter(appointmentAdaptor);
        totalTime.setText(DateUtils.formatElapsedTime(totalTimeList / 1000) + " Hours");
    }
}