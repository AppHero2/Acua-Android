package com.acua.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.acua.app.classes.AppManager;
import com.acua.app.models.User;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class AppSplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2500L;

    private Handler mHandler;
    private Runnable mRunnable;

    private String currentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        initImageLoader(getApplicationContext());

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                dismissSplash();
            }
        };

        // allow user to click and dismiss the splash screen prematurely
        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSplash();
            }
        });

        TextView txtCopyright = (TextView) findViewById(R.id.txtCopyright);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            String versionCode = String.valueOf(pInfo.versionCode);
            currentVersion = versionName + "(" + versionCode + ") ";
            txtCopyright.setText(getString(R.string.app_name) + currentVersion + getString(R.string.side_menu_copyright));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            AppManager.getInstance().startTrackingUser(firebaseUser.getUid());
            AppManager.getInstance().startTrackingNotification(firebaseUser.getUid());
            AppManager.getInstance().startTrackingCarType();
            AppManager.getInstance().startTrackingWashType();
            AppManager.getInstance().startTrackingMenus();
            AppManager.getInstance().startTrackingOrders();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, SPLASH_DURATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    private void dismissSplash() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            User user = AppManager.getSession();
            if (user != null) {
                if (user.getFirstname().equals("?") || user.getLastname().equals("?")) {
                    startActivity(new Intent(this, RegisterUserActivity.class));
                } else {
                    startActivity(new Intent(AppSplashActivity.this, MainActivity.class));
                }
            } else {
                startActivity(new Intent(this, RegisterUserActivity.class));
            }
        } else {
            startActivity(new Intent(this, RegisterTermsActivity.class));
        }
        this.finish();
    }
}
