package com.acua.app;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.acua.app.classes.AppManager;
import com.acua.app.country.Country;
import com.acua.app.utils.References;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

/**
 * Created by BKing on 11/28/2017.
 */

public class AppApplication extends Application{

    private FirebaseDatabase database;

    private static AppApplication sInstance;
    public AppApplication() {
        sInstance = this;
    }
    public static synchronized AppApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(database==null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }

        database = FirebaseDatabase.getInstance();
        References.init(this, database);

        AppManager.getInstance().setContext(getApplicationContext());
        AppManager.getInstance().setCountry(Country.getCountryFromSIM(getApplicationContext()));
        AppManager.getInstance().session = AppManager.getSession();

        // Initialize image loader
        initImageLoader(this);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(false)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenResult result) {
                        Log.d("Application", "notificationOpened: " + result);
                        try {
                            OSNotification notification = result.notification;
                            String fullMessage = notification.payload.body;
                            if (!fullMessage.equals("Please Rate our Service")) {
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Notification", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("notificationOpened", true);
                                editor.apply();
                                editor.commit();
                            }

                            Intent intent = new Intent(getApplicationContext(), AppSplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                })
                .setNotificationReceivedHandler(new OneSignal.NotificationReceivedHandler() {
                    @Override
                    public void notificationReceived(OSNotification notification) {

                    }
                })
                .init();
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        //config.writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());
    }
}
