package com.mosili.acua;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import com.mosili.acua.adapters.ViewPagerAdapter;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.fragments.AppointmentsFragment;
import com.mosili.acua.fragments.BookingFragment;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.User;
import com.mosili.acua.utils.Util;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, UserValueListener{

    private static final String TAG = "MainActivity";

    private ImageView nav_header_Profile;
    private TextView nav_header_Username, nav_header_Useremail;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String[] tabTitle = new String[2];
    int[] unreadData ={0, 0};

    private BookingFragment bookingFragment;
    private AppointmentsFragment appointmentsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
//        navigationView.setNavigationItemSelectedListener(this);


        Button btnProfile = (Button) findViewById(R.id.btn_menu_profile); btnProfile.setOnClickListener(this);
        Button btnNotification = (Button) findViewById(R.id.btn_menu_notification); btnNotification.setOnClickListener(this);
        Button btnPayment = (Button) findViewById(R.id.btn_menu_payment); btnPayment.setOnClickListener(this);
        Button btnShare = (Button) findViewById(R.id.btn_menu_share); btnShare.setOnClickListener(this);
        Button btnFeedback = (Button) findViewById(R.id.btn_menu_feedback); btnFeedback.setOnClickListener(this);

        AppManager.getInstance().setUserValueListenerMain(this);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
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

    }

    @Override
    public void onLoadedUser(User user) {
        if (user != null) {
            updateUserInfoOnNavHeader(user);
        }
    }

    private void updateUserInfoOnNavHeader(User user){
        nav_header_Username.setText(user.getFirstname() + " " + user.getLastname());
        nav_header_Useremail.setText(user.getEmail());
        Util.setProfileImage(user.getPhoto(), nav_header_Profile);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        bookingFragment = new BookingFragment();
        appointmentsFragment = new AppointmentsFragment();
        adapter.addFragment(bookingFragment,"BOOKING");
        adapter.addFragment(appointmentsFragment, "APPOINTMENT");
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
        tabTitle[0] = getString(R.string.tab_title_booking);
        tabTitle[1] = getString(R.string.tab_title_appoint);

        for(int i = 0; i < tabTitle.length; i++)
        {
            /*TabLayout.Tab tabitem = tabLayout.newTab();
            tabitem.setCustomView(prepareTabView(i));
            tabLayout.addTab(tabitem);*/

            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }

    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/
}
