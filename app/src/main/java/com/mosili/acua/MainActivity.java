package com.mosili.acua;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.User;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, UserValueListener{

    private ImageView nav_header_Profile;
    private TextView nav_header_Username, nav_header_Useremail;

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
    }

    //    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
}
