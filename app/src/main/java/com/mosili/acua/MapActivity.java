package com.mosili.acua;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.OrderLocation;
import com.mosili.acua.models.User;
import com.mosili.acua.utils.TimeUtil;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap;

    private Order order;
    private OrderLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Location");
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.this.finish();
            }
        });

        order = AppManager.getInstance().focusedOrder;
        location = order.location;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (location != null){
            showCustomerLocation(location);
            Log.d("Map", location.toString());
        }
    }

    private void showCustomerLocation(final OrderLocation location){
        final LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        try{
            mGoogleMap.setMyLocationEnabled(true);
        }catch (SecurityException e){
            e.printStackTrace();
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(15).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        AppManager.getUser(order.customerId, new UserValueListener() {
            @Override
            public void onLoadedUser(final User user) {
                MapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MarkerOptions options = new MarkerOptions()
                                .position(position)
                                .title(user.getFullName())
                                .snippet(TimeUtil.getSimpleDateString(order.beginAt))
                                .anchor(0.5f, 1);
                        Marker marker =  mGoogleMap.addMarker(options);
                        marker.setTag(order.idx);
                    }
                });
            }
        });
    }
}
