package com.acua.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acua.app.adapters.ViewPagerAdapter;
import com.acua.app.classes.AppManager;
import com.acua.app.fragments.AdminStatisticsFragment;
import com.acua.app.fragments.AppointmentsFragment;
import com.acua.app.fragments.BookingFragment;
import com.acua.app.interfaces.NotificationListener;
import com.acua.app.interfaces.ResultListener;
import com.acua.app.interfaces.UserValueListener;
import com.acua.app.models.Notification;
import com.acua.app.models.Order;
import com.acua.app.models.OrderServiceStatus;
import com.acua.app.models.User;
import com.acua.app.utils.References;
import com.acua.app.utils.Util;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.matrixxun.starry.badgetextview.MaterialBadgeTextView;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionObserver;
import com.onesignal.OSPermissionStateChanges;
import com.onesignal.OneSignal;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, UserValueListener, NotificationListener, RatingDialogListener,
        AppointmentsFragment.OnAppointmentsFragmentInteractionListener, BookingFragment.OnFragmentInteractionListener{

    private static final String TAG = "MainActivity";

    private User session;

    private ImageView nav_header_Profile;
    private TextView nav_header_Username, nav_header_Useremail;
    private MaterialBadgeTextView tvBadge;
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

        /*FloatingActionButton fab_contact = (FloatingActionButton) findViewById(R.id.fab_contact);
        fab_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactUs();
            }
        });
        if (this.session.getUserType()==1) fab_contact.setVisibility(View.GONE);*/

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
        Button btnWhere = (Button) findViewById(R.id.btn_menu_where); btnWhere.setOnClickListener(this);
        Button btnAgreements = (Button) findViewById(R.id.btn_menu_agreements); btnAgreements.setOnClickListener(this);

        RelativeLayout layout_feedback = (RelativeLayout) findViewById(R.id.layout_feedback);
        if (this.session.getUserType() == 1) {
            layout_feedback.setVisibility(View.GONE);
        }

        tvBadge = (MaterialBadgeTextView) findViewById(R.id.tv_badge); tvBadge.setVisibility(View.GONE);
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
        AppManager.getInstance().setNotificationListener(this);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        try
        {
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


        Query query = References.getInstance().ordersRef.orderByChild("customerId").equalTo(session.getIdx());
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("self order", dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear all notification
        OneSignal.clearOneSignalNotifications();

        // Refresh Notifications badge count
        refreshNotificationBadgeCount();

        SharedPreferences sharedPreferences = getSharedPreferences("Notification", Context.MODE_PRIVATE);
        boolean notificationOpened = sharedPreferences.getBoolean("notificationOpened", false);
        if (notificationOpened) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notificationOpened", false);
            editor.apply();
            editor.commit();
            startActivity(new Intent(this, NotificationsActivity.class));
        }
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
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
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
                if (session.getUserType() == 0) {

                    AppManager.getInstance().lastFeedbackOrder = null;
                    for (int i=AppManager.getInstance().selfOrders.size()-1; i>=0; i--) {
                        Order order = AppManager.getInstance().selfOrders.get(i);
                        if (order.serviceStatus == OrderServiceStatus.COMPLETED) {
                            AppManager.getInstance().lastFeedbackOrder = order;
                            break;
                        }
                    }

                    if (AppManager.getInstance().lastFeedbackOrder != null) {
                        startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                    } else {
                        Toast.makeText(this, "You have no previous appointment.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                }
            }
                break;
            case R.id.btn_menu_where:{
                startActivity(new Intent(MainActivity.this, WhereActivity.class));
            }
                break;
            case R.id.btn_menu_agreements:{
                startActivity(new Intent(MainActivity.this, AgreementsActivity.class));
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

    @Override
    public void onRemovedNotification(Notification notification) {
        refreshNotificationBadgeCount();
    }

    @Override
    public void onReceivedNotification(Notification notification) {
        refreshNotificationBadgeCount();
    }

    @Override
    public void didMakeOrder(Order order) {
        viewPager.setCurrentItem(1, true);
    }

    private void refreshNotificationBadgeCount(){
        int count = 0;
        for (Notification notification: AppManager.getInstance().notifications) {
            if (!notification.isRead()){
                count += 1;
            }
        }

        if (count < 1) {
            tvBadge.setVisibility(View.GONE);
        } else {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(count));
        }
    }

    private void initPushNotification(){

        /*OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(false)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenResult result) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                            }
                        });
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
                .init();*/

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
        nav_header_Username.setText(user.getFullName());
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
        else if (this.session.getUserType() == 2)
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
        } else if (this.session.getUserType() == 2) {
            tabTitle[0] = getString(R.string.tab_title_statistics);
            tabTitle[1] = getString(R.string.tab_title_appoint);
        }

        for(int i = 0; i < tabTitle.length; i++)
        {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }

    }

    private void shareThisApp(){
        String message = "Have your car professionally washed at home or at the office with Acuar.\n" +
                "\n" +
                "Download the Acuar App on:\n" +
                "the App Store: https://itunes.apple.com.us/app/acuar/id386096453?ls=1&mt=8\n" +
                "Google Play Store: https;//play.google.com/store/apps/details?id=com.acua.app\n" +
                "\n" +
                "Spend time on what matters";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        this.startActivity(sendIntent);
    }

    private void sendFeedback(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"acuacarwash@gmail.com"});
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
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"acuacarwash@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "regarding to acua");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRatingDiglog(){
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(4)
                .setTitle("Please rate our service")
                .setDescription("Please select some stars and give your feedback")
//                .setDefaultComment("")
                .setStarColor(R.color.colorRatingStart)
                .setNoteDescriptionTextColor(R.color.colorGrey)
                .setTitleTextColor(R.color.colorTextBlue)
                .setDescriptionTextColor(R.color.colorGrey)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.colorGrey)
                .setCommentTextColor(R.color.colorGrey)
                .setCommentBackgroundColor(R.color.colorRatingCommentBG)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .create(MainActivity.this)
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int rate, String comment) {
        // interpret results, send it to analytics etc...
        User session = AppManager.getSession();
        String title = session.getFullName() + " left service rating as " + rate + ".";
        String html = "<p>" + comment + "</p>";
        AppManager.getInstance().sendEmailPushToADMIN(title, title, html, new ResultListener() {
            @Override
            public void onResponse(boolean success, String response) {
                String result = "Your rating has been sent successfully.";
                if (!success) {
                    result = "Failed to send your rating. Please try again...";
                }
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

        /*AppManager.getInstance().sendPushNotificationToCustomer(ADMIN_PUSH_ID, title,  comment);
        DatabaseReference reference = References.getInstance().notificationsRef.child(ADMIN_USER_ID).push();
        String notificationId = reference.getKey();
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("idx", notificationId);
        notificationData.put("title", title);
        notificationData.put("message", comment);
        notificationData.put("createdAt", System.currentTimeMillis());
        notificationData.put("isRead", false);
        reference.setValue(notificationData);*/
    }

    @Override
    public void onNegativeButtonClicked() {
        Log.d("MainAC", "onNegativeButtonClicked: Cancel");
    }

    @Override
    public void onNeutralButtonClicked() {
        Log.d("MainAC", "onNeutralButtonClicked: Later");
    }

    @Override
    public void onClickRateServiceInAppointments() {
        showRatingDiglog();
    }

    @Override
    public void onClickFeedbackInAppointments() {
        AppManager.getInstance().lastFeedbackOrder = null;
        for (int i=AppManager.getInstance().selfOrders.size()-1; i>=0; i--) {
            Order order = AppManager.getInstance().selfOrders.get(i);
            if (order.serviceStatus == OrderServiceStatus.COMPLETED) {
                AppManager.getInstance().lastFeedbackOrder = order;
                break;
            }
        }

        if (AppManager.getInstance().lastFeedbackOrder != null) {
            startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
        } else {
            Toast.makeText(this, "You have no previous appointment.", Toast.LENGTH_SHORT).show();
        }
    }
}
