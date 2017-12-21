package com.mosili.acua;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.mosili.acua.adapters.ViewPagerAdapter;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.fragments.AdminStatisticsFragment;
import com.mosili.acua.fragments.AppointmentsFragment;
import com.mosili.acua.fragments.BookingFragment;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.User;
import com.mosili.acua.utils.References;
import com.mosili.acua.utils.Util;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionObserver;
import com.onesignal.OSPermissionStateChanges;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, UserValueListener{

    private static final String TAG = "MainActivity";

    private User session;

    private ImageView nav_header_Profile;
    private TextView nav_header_Username, nav_header_Useremail;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String[] tabTitle = new String[2];
    int[] unreadData ={0, 0};

    private AdminStatisticsFragment adminStatisticsFragment;
    private BookingFragment bookingFragment;
    private AppointmentsFragment appointmentsFragment;

    private String currentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.session = AppManager.getSession();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_contact = (FloatingActionButton) findViewById(R.id.fab_contact);
        fab_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactUs();
            }
        });
        if (this.session.getUserType()==1) fab_contact.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        nav_header_Profile = headerView.findViewById(R.id.imgProfile);
        nav_header_Username = headerView.findViewById(R.id.txtUsername);
        nav_header_Useremail = headerView.findViewById(R.id.txtUseremail);

        User session = AppManager.getSession();
        updateUserInfoOnNavHeader(session);

        Button btnProfile = (Button) findViewById(R.id.btn_menu_profile); btnProfile.setOnClickListener(this);
        Button btnNotification = (Button) findViewById(R.id.btn_menu_notification); btnNotification.setOnClickListener(this);
        Button btnPayment = (Button) findViewById(R.id.btn_menu_payment); btnPayment.setOnClickListener(this);
        Button btnShare = (Button) findViewById(R.id.btn_menu_share); btnShare.setOnClickListener(this);
        Button btnFeedback = (Button) findViewById(R.id.btn_menu_feedback); btnFeedback.setOnClickListener(this);

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

        AppManager.getInstance().setUserValueListenerMain(this);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        try{
            setupTabIcons();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position,false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(0);

        initPushNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear all notification
        OneSignal.clearOneSignalNotifications();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int idx = view.getId();
        switch (idx) {
            case R.id.btn_menu_profile:{
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
            break;
            case R.id.btn_menu_notification:{

            }
            break;
            case R.id.btn_menu_payment:{
                startActivity(new Intent(MainActivity.this, PaymentActivity.class));
            }
                break;
            case R.id.btn_menu_share:{
                shareThisApp();
            }
                break;
            case R.id.btn_menu_feedback:{
                sendFeedback();
            }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onLoadedUser(User user) {
        if (user != null) {
            this.session = user;
            updateUserInfoOnNavHeader(this.session);
        }
    }

    private void initPushNotification(){

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(false)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenResult result) {
                        Log.d("Notification", result.toString());
                    }
                })
                .setNotificationReceivedHandler(new OneSignal.NotificationReceivedHandler() {
                    @Override
                    public void notificationReceived(OSNotification notification) {
                        if (isAppOnForeground(MainActivity.this)){
                            // Clear all notification
                            //OneSignal.clearOneSignalNotifications();
                        }else{
                            Log.d("Notification", notification.toString());
                        }
                    }
                })
                .init();

        OneSignal.addPermissionObserver(new OSPermissionObserver() {
            @Override
            public void onOSPermissionChanged(OSPermissionStateChanges stateChanges) {
                if (stateChanges.getFrom().getEnabled() &&
                        !stateChanges.getTo().getEnabled()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Notifications Disabled!")
                            .show();
                }
                Log.i(TAG, "onOSPermissionChanged: " + stateChanges);
            }
        });

        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("OneSignal", "PlayerID: " + userId + "\nPushToken: " + registrationId);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                Map<String, Object> pushToken = new HashMap<>();
                pushToken.put("pushToken", userId);
                References.getInstance().usersRef.child(session.getIdx()).updateChildren(pushToken);
                database.getReference("PushTokens").child(session.getIdx()).setValue(userId);
            }
        });
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void updateUserInfoOnNavHeader(User user){
        nav_header_Username.setText(user.getFirstname() + " " + user.getLastname());
        nav_header_Useremail.setText(user.getEmail());
        Util.setProfileImage(user.getPhoto(), nav_header_Profile);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (this.session.getUserType() == 0)
        {
            bookingFragment = new BookingFragment();
            appointmentsFragment = new AppointmentsFragment();
            adapter.addFragment(bookingFragment, getString(R.string.tab_title_booking));
            adapter.addFragment(appointmentsFragment, getString(R.string.tab_title_appoint));
        }
        else if (this.session.getUserType() == 1)
        {
            adminStatisticsFragment = new AdminStatisticsFragment();
            appointmentsFragment = new AppointmentsFragment();
            adapter.addFragment(adminStatisticsFragment, getString(R.string.tab_title_statistics));
            adapter.addFragment(appointmentsFragment, getString(R.string.tab_title_appoint));
        }

        viewPager.setAdapter(adapter);
    }

    private View prepareTabView(int pos) {
        View view = getLayoutInflater().inflate(R.layout.custom_tab,null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(tabTitle[pos]);
        if(unreadData[pos] > 0)
        {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText(""+ unreadData[pos]);
        }
        else
            tv_count.setVisibility(View.GONE);

        return view;
    }

    private void setupTabIcons()
    {
        if (this.session.getUserType() == 0) {
            tabTitle[0] = getString(R.string.tab_title_booking);
            tabTitle[1] = getString(R.string.tab_title_appoint);
        } else if (this.session.getUserType() == 1) {
            tabTitle[0] = getString(R.string.tab_title_statistics);
            tabTitle[1] = getString(R.string.tab_title_appoint);
        }

        for(int i = 0; i < tabTitle.length; i++)
        {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }

    }

    private void shareThisApp(){
        final String appPackageName = this.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out acua App at: https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");
        this.startActivity(sendIntent);
    }

    private void sendFeedback(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mosili.pebane@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "feedback for acua " + currentVersion);
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "Feedback"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactUs(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mosili.pebane@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "regarding to acua");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
