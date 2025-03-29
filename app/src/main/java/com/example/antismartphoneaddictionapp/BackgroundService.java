package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.antismartphoneaddictionapp.Models.LocalAppModel;
import com.example.antismartphoneaddictionapp.Models.RestrictedAppModel;
import com.example.antismartphoneaddictionapp.Utility.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.*;

public class BackgroundService extends Service {
    public static final String CHANNEL_ID = "usage_notification_channel";
    private Handler handler = new Handler();
    private Timer timer;
    private TimerTask timerTask;
    private DatabaseHandler db;
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

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

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHandler(this);
        mPm = getPackageManager();
        mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

        startForegroundService();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    Log.d("TAG", "Background service running...");
                    getUsage();
                    showList();
                    checkForegroundApp(); // New method to check and restrict app usage
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);  // 1 seconds interval
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel(this, CHANNEL_ID);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Usage Monitoring")
                .setContentText("Monitoring App Usage")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        startForeground(123, notification);
        Log.d("TAG", "Foreground Service Started");
    }


    public static void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Usage Alert";
            String description = "Alerts for excessive app usage";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void showNotification(String title, String message) {
        Log.d("TAG", "Showing Notification: " + title + " - " + message);

        int randomId = new Random().nextInt(2000);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, randomId, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(this, CHANNEL_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(randomId, builder.build());
    }

    void getUsage() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                cal.getTimeInMillis(), System.currentTimeMillis());

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        for (UsageStats pkgStats : stats) {
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
        mPackageStats.clear();
        mPackageStats.addAll(map.values());
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);
    }

    void showList() {
        for (UsageStats pkgStats : mPackageStats) {
            if (pkgStats != null && pkgStats.getTotalTimeInForeground() > Constants.FOREGROUND_CHECK_TIME) { // 5 minutes
//            if (pkgStats != null && pkgStats.getTotalTimeInForeground() > 7200000) { // 120 minutes
                String appName = mAppLabelMap.get(pkgStats.getPackageName()).toUpperCase();
                List<LocalAppModel> appModels = db.getAllApps();
                boolean shouldNotify = true;

                for (LocalAppModel appModel : appModels) {
                    if (appModel.getPackageName().equalsIgnoreCase(pkgStats.getPackageName())
                            && getFormattedDate().equalsIgnoreCase(appModel.getDateTime())) {
                        shouldNotify = false;
                        break;
                    }
                }

                if (shouldNotify) {
                    showNotification("Over Usage Detected", "Please stop using " + appName);

                    // Creating RestrictedAppModel with current data
                    RestrictedAppModel restrictedAppModel = new RestrictedAppModel();
                    restrictedAppModel.setPackageName(pkgStats.getPackageName());
                    restrictedAppModel.setDate(getFormattedDate());
                    restrictedAppModel.setTime(getFormattedTime());
                    restrictedAppModel.setExpiryTime(getExpiryTime());  // You may define the expiry time logic
                    db.addRestrictedApp(restrictedAppModel);    // Add restricted app to the database

                    db.addApp(new LocalAppModel(pkgStats.getPackageName(), getFormattedDate()));
                }
            }
        }
    }

    private String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getExpiryTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, Constants.EXPIRY_TIME);  // Set expiry time to 1 hours from now
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String getFormattedDate() {
        return new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    void checkForegroundApp() {
        String foregroundApp = getForegroundApp();
        if (foregroundApp == null || foregroundApp.equalsIgnoreCase("com.miui.home")
                || foregroundApp.equalsIgnoreCase("com.example.antismartphoneaddictionapp")) {
            return; // Ignore if it's the home screen, or AntiSmartPhoneAddictionApp
        }
//        here also check time for temporary unload time keep the logic for expiry time as it is if unload time is remaining then app must be unlocked for that time
        List<RestrictedAppModel> restrictedApps = db.getAllRestrictedApps();  // Fetch all restricted apps
        for (RestrictedAppModel app : restrictedApps) { //        // Check if temporary unlock time is still valid
//            if (foregroundApp.equalsIgnoreCase(app.getPackageName()) &&
//                    System.currentTimeMillis() < parseTime(app.getExpiryTime())) {
//
//                Log.d("TAG", "Restricted App Detected: " + foregroundApp);
//                String appName = mAppLabelMap.get(foregroundApp); // Get the app's user-friendly name
//                showNotification("Restricted App Blocked", "You are not allowed to use " + foregroundApp);
//                launchAntiSmartphoneAddictionApp(appName, app.getPackageName(), app.getId()); // Launch your app
//                break;
//            }

            // Check if temporary unlock time is still valid
            if (foregroundApp.equalsIgnoreCase(app.getPackageName())) {
                long currentTime = System.currentTimeMillis();

                // Check if tempUnlockExpiryTime is not null or empty before parsing
                String tempUnlockExpiryTimeStr = app.getTempUnlockExpiryTime();
                if (tempUnlockExpiryTimeStr != null && !tempUnlockExpiryTimeStr.isEmpty()) {
                    long tempUnlockExpiryTime = parseTime(tempUnlockExpiryTimeStr);
                    if (currentTime < tempUnlockExpiryTime) {
                        // Allow access to the app until the unlock time expires
                        return;
                    }
                }

                // If it's not temporarily unlocked, check the normal expiry time
                long expiryTime = parseTime(app.getExpiryTime());
                if (currentTime < expiryTime) {
                    Log.d("TAG", "Restricted App Detected: " + foregroundApp);
                    String appName = mAppLabelMap.get(foregroundApp); // Get the app's user-friendly name
                    showNotification("Restricted App Blocked", "You are not allowed to use " + foregroundApp);
                    launchAntiSmartphoneAddictionApp(appName, app.getPackageName(), app.getId());
                    break;
                }
            }
        }
    }

    String getForegroundApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 5000; // Last 5 seconds

            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
            if (stats != null) {
                UsageStats recentStats = null;
                for (UsageStats usageStats : stats) {
                    if (recentStats == null || usageStats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                        recentStats = usageStats;
                    }
                }
                if (recentStats != null) {
                    return recentStats.getPackageName();
                }
            }
        }
        return null;
    }

    void launchAntiSmartphoneAddictionApp(String appName, String packageName, int appId) {
        Intent intent = new Intent(this, RestrictedAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("APP_NAME", appName); // Pass the app name
        intent.putExtra("PACKAGE_NAME", packageName);  // Pass the package name
        intent.putExtra("APP_ID", appId);  // Pass the app ID
        startActivity(intent);
//        Intent homeIntent = new Intent(Intent.ACTION_MAIN); //to launch the Home of device
//        homeIntent.addCategory(Intent.CATEGORY_HOME);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(homeIntent);
    }

    long parseTime(String expiryTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(expiryTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
