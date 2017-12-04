package com.mosili.acua;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.country.Country;
import com.mosili.acua.utils.References;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by BKing on 11/28/2017.
 */

public class AppApplication extends Application{

    private FirebaseDatabase database;

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

        // Initialize image loader
        initImageLoader(this);
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
