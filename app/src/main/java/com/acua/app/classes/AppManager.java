package com.acua.app.classes;

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

import com.acua.app.interfaces.NotificationListener;
import com.acua.app.models.Notification;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.acua.app.country.Country;
import com.acua.app.interfaces.CarTypeValueListener;
import com.acua.app.interfaces.MenuValueListener;
import com.acua.app.interfaces.OrderValueListener;
import com.acua.app.interfaces.ResultListener;
import com.acua.app.interfaces.UserValueListener;
import com.acua.app.interfaces.WashTypeValueListener;
import com.acua.app.models.CarType;
import com.acua.app.models.Order;
import com.acua.app.models.PayCard;
import com.acua.app.models.WashMenu;
import com.acua.app.models.User;
import com.acua.app.models.WashType;
import com.acua.app.utils.Const;
import com.acua.app.utils.References;
import com.onesignal.OneSignal;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.FormBody;
//import okhttp3.Headers;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

import static com.acua.app.utils.Const.KEY_STRIPE_KEY;

/**
 * Created by Administrator on 4/7/2017.
 */

public class AppManager {
    private static final AppManager ourInstance = new AppManager();
    private Context context;
    private Country country;

    private ValueEventListener trackUserListener, trackCarTypeListener, trackWashTypeListener, trackMenuListener, trackOrdersListener;
    private ChildEventListener trackNotificationListener;
    private UserValueListener userValueListenerMain;
    private CarTypeValueListener carTypeValueListener;
    private WashTypeValueListener washTypeValueListener;
    private MenuValueListener menuValueListener;
    private OrderValueListener orderValueListener;
    private NotificationListener notificationListener;

    public List<CarType> carTypes = new ArrayList<>();
    public List<WashType> washTypes = new ArrayList<>();
    public List<WashMenu> menuList = new ArrayList<>();
    public List<Order> orderList = new ArrayList<>();
    public List<Notification> notifications = new ArrayList<>();

    public Order currentOrder, focusedOrder;

//    private OkHttpClient client;

    public static AppManager getInstance() {
        return ourInstance;
    }

    private AppManager() {
//        client = new OkHttpClient.Builder()
//                .connectTimeout(1000, TimeUnit.MINUTES)
//                .readTimeout(1000, TimeUnit.MINUTES)
//                .writeTimeout(1000, TimeUnit.MINUTES)
//                .build();
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

    public static void getUser(String userId, final UserValueListener listener) {
        if (userId != null) {
            Query query = References.getInstance().usersRef.child(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                        User user = new User(userData);
                        if (listener != null) {
                            listener.onLoadedUser(user);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("TrackUser", databaseError.toString());
                }
            });
        } else {
            if (listener != null) {
                listener.onLoadedUser(null);
            }
        }
    }

    public void setUserValueListenerMain(UserValueListener userValueListenerMain) {
        this.userValueListenerMain = userValueListenerMain;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
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

    public void setOrderValueListener(OrderValueListener orderValueListener) {
        this.orderValueListener = orderValueListener;
    }


    public void startTrackingNotification(String uid){
        if (trackNotificationListener != null)
            return;

        trackNotificationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() !=  null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Notification notification = new Notification(data);
                    notifications.add(notification);

                    if (notificationListener != null)
                        notificationListener.onReceivedNotification(notification);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() !=  null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Notification notification = new Notification(data);
                    for (Notification notify:notifications) {
                        if (notification.getIdx().equals(notify.getIdx())) {
                            notifications.remove(notify);
                            break;
                        }
                    }

                    if (notificationListener != null)
                        notificationListener.onRemovedNotification(notification);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackNotification", databaseError.toString());
            }
        };

        References.getInstance().notificationsRef.child(uid).addChildEventListener(trackNotificationListener);
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

    public void sendPushNotificationToCustomer(String pushToken, String title, String message) {
        JSONArray receivers = new JSONArray();
        receivers.put(pushToken);
        sendOneSignalPush(receivers, title, message);
    }

    public void sendPushNotificationToService(final String title, final String message){
        Query query = References.getInstance().usersRef.orderByChild("userType").equalTo(1); // service
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    JSONArray receivers = new JSONArray();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Map<String, Object> data = (Map<String, Object>) child.getValue();
                        User user = new User(data);
                        String pushToken = user.getPushToken();
                        receivers.put(pushToken);
                    }

                    sendOneSignalPush(receivers, title, message);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendOneSignalPush(JSONArray receivers, String title, String message){
        try {

            JSONObject pushObject = new JSONObject();
            JSONObject contents = new JSONObject();
            contents.put("en", message);
            JSONObject headings = new JSONObject();
            headings.put("en", title);
            pushObject.put("headings", headings);
            pushObject.put("contents", contents);
            pushObject.put("include_player_ids", receivers);
            OneSignal.postNotification(pushObject, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    Log.d("Push", jsonObject.toString());
                }

                @Override
                public void onFailure(JSONObject jsonObject) {
                    Log.d("Push", jsonObject.toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        Gson gson = new Gson();
        String payCard = gson.toJson(user.getPayCard());
        editor.putString("payCard", payCard);
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
        String payCardData = sharedPreferences.getString("payCard", null);
        Map<String,Object> payCard = new Gson().fromJson(payCardData, new TypeToken<Map<String, Object>>(){}.getType());

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
            data.put("payCard", payCard);
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

    public void getCardToken(PayCard payCard, final ResultListener listener) {
        Card card = new Card(
                payCard.getNumber(),
                Integer.valueOf(payCard.getMonth()),
                Integer.valueOf(payCard.getYear()),
                payCard.getCvc()
        );

        new Stripe(this.context).createToken(
                card,
                KEY_STRIPE_KEY,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        if (listener != null) {
                            listener.onResponse(true, token.getId());
                        }
                    }
                    public void onError(Exception error) {
                        if (listener != null) {
                            listener.onResponse(false, error.getLocalizedMessage());
                        }
                    }
                });
    }


    public void makePayment(String phone, String token, String type, int amount, final ResultListener listener){

        String url = Const.URL_HEROKU_BASE + "payment/charge";

        final Map<String, String> params = new HashMap();
        params.put("phoneNumber", phone);
        params.put("stripeToken", token);
        params.put("serviceType", type);
        params.put("serviceCost", String.valueOf(amount));
        params.put("Content-Type", "application/x-www-form-urlencoded");

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if (listener != null) {
                            listener.onResponse(false, response);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (listener != null) {
                            listener.onResponse(false, error.getLocalizedMessage());
                        }
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };

        queue.add(strRequest);

        /*RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("phoneNumber", phone)
                .addFormDataPart("serviceType", type)
                .addFormDataPart("stripeToken", token)
                .addFormDataPart("serviceCost", String.valueOf(amount))
                .build();

        final Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(Const.URL_HEROKU_BASE+"payment/charge")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onResponse(false, e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    listener.onResponse(true, response.body().string());
                }
            }
        });*/
    }

}
