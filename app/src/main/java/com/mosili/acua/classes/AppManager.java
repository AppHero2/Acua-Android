package com.mosili.acua.classes;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mosili.acua.country.Country;
import com.mosili.acua.interfaces.CarTypeValueListener;
import com.mosili.acua.interfaces.MenuValueListener;
import com.mosili.acua.interfaces.OrderValueListener;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.interfaces.WashTypeValueListener;
import com.mosili.acua.models.CarType;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.WashMenu;
import com.mosili.acua.models.User;
import com.mosili.acua.models.WashType;
import com.mosili.acua.utils.References;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 4/7/2017.
 */

public class AppManager {
    private static final AppManager ourInstance = new AppManager();
    private Context context;
    private Country country;

    private ValueEventListener trackUserListener, trackCarTypeListener, trackWashTypeListener, trackMenuListener, trackOrdersListener;
    private UserValueListener userValueListenerMain;
    private CarTypeValueListener carTypeValueListener;
    private WashTypeValueListener washTypeValueListener;
    private MenuValueListener menuValueListener;
    private OrderValueListener orderValueListener;

    public List<CarType> carTypes = new ArrayList<>();
    public List<WashType> washTypes = new ArrayList<>();
    public List<WashMenu> menuList = new ArrayList<>();
    public List<Order> orderList = new ArrayList<>();

    public static AppManager getInstance() {
        return ourInstance;
    }

    private AppManager() {

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public static boolean checkPermission(final Context context, final String permission, final int key) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    if (permission.contains(Manifest.permission.READ_EXTERNAL_STORAGE)){
                        alertBuilder.setMessage("External storage permission is necessary");
                    }
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, key);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, key);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }

    public void setUserValueListenerMain(UserValueListener userValueListenerMain) {
        this.userValueListenerMain = userValueListenerMain;
    }

    public void setCarTypeValueListener(CarTypeValueListener carTypeValueListener) {
        this.carTypeValueListener = carTypeValueListener;
    }

    public void setWashTypeValueListener(WashTypeValueListener washTypeValueListener) {
        this.washTypeValueListener = washTypeValueListener;
    }

    public void setMenuValueListener(MenuValueListener menuValueListener) {
        this.menuValueListener = menuValueListener;
    }

    /**
     * this method is used to track user
     * @param uid : user identity
     */
    public void startTrackingUser(String uid) {
        if (trackUserListener != null)
            return;
        trackUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                    User user = new User(userData);
                    AppManager.saveSession(user);
                    if (userValueListenerMain != null) {
                        userValueListenerMain.onLoadedUser(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackUser", databaseError.toString());
            }
        };
        References.getInstance().usersRef.child(uid).addValueEventListener(trackUserListener);
    }

    public void stopTrackingUser(String uid){
        if (trackUserListener != null)
            References.getInstance().usersRef.child(uid).removeEventListener(trackUserListener);
    }

    public void startTrackingCarType(){
        if (trackCarTypeListener != null) return;

        trackCarTypeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    carTypes.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        CarType type = new CarType(child.getKey(), (String)child.getValue());
                        carTypes.add(type);
                    }
                    if (carTypeValueListener != null) {
                        carTypeValueListener.onLoadedCarTypes(carTypes);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackCarType", databaseError.toString());
            }
        };

        References.getInstance().carTypeRef.addValueEventListener(trackCarTypeListener);
    }

    public void startTrackingWashType(){
        if (trackWashTypeListener != null) return;

        trackWashTypeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    washTypes.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        WashType type = new WashType(child.getKey(), (String)child.getValue());
                        washTypes.add(type);
                    }
                    if (washTypeValueListener != null) {
                        washTypeValueListener.onLoadedWashTypes(washTypes);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackWashType", databaseError.toString());
            }
        };

        References.getInstance().washTypeRef.addValueEventListener(trackWashTypeListener);
    }

    public void startTrackingMenus(){
        if (trackMenuListener != null) return;

        trackMenuListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    menuList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String key = child.getKey();
                        Map<String, Object> value = (Map<String, Object>) child.getValue();
                        Number price = (Number) value.get("price");
                        Number duration = (Number) value.get("duration");
                        WashMenu menu = new WashMenu(key, price.doubleValue(), duration.longValue());
                        menuList.add(menu);
                    }
                    if (menuValueListener != null) {
                        menuValueListener.onLoadedMenu(menuList);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackMenu", databaseError.toString());
            }
        };

        References.getInstance().washMenuRef.addValueEventListener(trackMenuListener);
    }

    public void startTrackingOrders(){
        if (trackOrdersListener != null) return;

        trackOrdersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    orderList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String key = child.getKey();
                        Map<String, Object> value = (Map<String, Object>) child.getValue();
                        Order order = new Order(value);
                        order.idx = key;
                        orderList.add(order);
                    }
                    if (orderValueListener != null) {
                        orderValueListener.onLoadedOrder(orderList);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Track Orders", databaseError.toString());
            }
        };

        References.getInstance().ordersRef.addValueEventListener(trackOrdersListener);
    }

    /**
     * save user data to local storage
     * @param user
     */
    public static void saveSession(User user){
        Context context = AppManager.getInstance().context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid", user.getIdx());
        editor.putString("bio", user.getBio());
        editor.putString("firstname", user.getFirstname());
        editor.putString("lastname", user.getLastname());
        editor.putString("email", user.getEmail());
        editor.putString("photo", user.getPhoto());
        editor.putString("phone", user.getPhone());
        editor.putString("pushToken", user.getPushToken());
        editor.putInt("userType", user.getUserType());
        editor.commit();
    }

    /**
     * get user data from local storage
     * @return user
     */
    public static User getSession() {
        Context context = AppManager.getInstance().context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);
        String firstname = sharedPreferences.getString("firstname", "?");
        String lastname = sharedPreferences.getString("lastname", "?");
        String email = sharedPreferences.getString("email", "?");
        String photo = sharedPreferences.getString("photo", "?");
        String phone = sharedPreferences.getString("phone", "");
        String bio = sharedPreferences.getString("bio", "?");
        String pushToken = sharedPreferences.getString("pushToken", "?");
        int userType = sharedPreferences.getInt("userType", 0);

        if (uid != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("uid", uid);
            data.put("firstname", firstname);
            data.put("lastname", lastname);
            data.put("email", email);
            data.put("photo", photo);
            data.put("phone", phone);
            data.put("bio", bio);
            data.put("pushToken", pushToken);
            data.put("userType", userType);
            User user = new User(data);
            return user;
        } else {
            return null;
        }
    }

    /**
     * delete user data from local storage
     * this method might be used when user logout
     * @param context
     */
    public static void deleteSession(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid", null);
        editor.commit();
    }

}
