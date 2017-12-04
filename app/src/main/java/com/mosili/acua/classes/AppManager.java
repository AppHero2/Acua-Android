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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.mosili.acua.country.Country;
import com.mosili.acua.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 4/7/2017.
 */

public class AppManager {
    private static final AppManager ourInstance = new AppManager();
    private Context context;
    private Country country;
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

    public static boolean checkPermission(final Context context, final String permission, final int key)
    {
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
        String name = sharedPreferences.getString("name", "?");
        String photo = sharedPreferences.getString("photo", "?");
        String phone = sharedPreferences.getString("phone", "");
        String bio = sharedPreferences.getString("bio", "?");
        String pushToken = sharedPreferences.getString("pushToken", "?");

        if (uid != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("uid", uid);
            data.put("name", name);
            data.put("photo", photo);
            data.put("phone", phone);
            data.put("bio", bio);
            data.put("pushToken", pushToken);
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
