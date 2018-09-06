package com.acua.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.acua.app.alertView.AlertView;
import com.acua.app.alertView.OnItemClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.acua.app.classes.AppManager;
import com.acua.app.fragments.BookingFragment;
import com.acua.app.models.CarType;
import com.acua.app.models.Order;
import com.acua.app.models.OrderLocation;
import com.acua.app.models.User;
import com.acua.app.models.WashMenu;
import com.acua.app.models.WashType;
import com.acua.app.utils.IntervalTimePickerDialog;
import com.acua.app.utils.References;
import com.acua.app.utils.TimeUtil;
import com.acua.app.utils.Util;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.acua.app.utils.Const.ServiceTimeEnd;
import static com.acua.app.utils.Const.ServiceTimeStart;

public class EditOrderActivity extends AppCompatActivity {

    private final int PLACE_PICKER_REQUEST = 999;

    private Spinner spinnerCarType, spinnerWashType;
    private RadioGroup groupTap, groupPlug;
    private BookingFragment.OnFragmentInteractionListener mListener;

    private Calendar calendar = Calendar.getInstance();
    private TextView txtDate, txtTime,  txtCost;
    private int year, month, day, hour, minute;

    private TextView txtAddress;

    private OrderLocation curLocation;
    private CarType carType;
    private WashType washType;
    private WashMenu curMenu;
    private boolean hasTap = true, hasPlug = true;
    private List<String> washNames = new ArrayList<>();
    private List<String> carNames = new ArrayList<>();

    private Order currentOrder ;

    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this,R.color.colorTransparency))
                .setDimAmount(0.5f);

        currentOrder = AppManager.getInstance().currentOrder;

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.book_update_title));
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditOrderActivity.this.finish();
            }
        });

        spinnerWashType = (Spinner) findViewById(R.id.spinerWashTypes);
        spinnerCarType = (Spinner) findViewById(R.id.spinerCarTypes);

        washNames.clear();
        for (WashType washType : AppManager.getInstance().washTypes) {
            washNames.add(washType.getName());
        }
        if (AppManager.getInstance().washTypes.size()>0)
            washType = AppManager.getInstance().washTypes.get(0);
        carNames.clear();
        for (CarType carType : AppManager.getInstance().carTypes) {
            carNames.add(carType.getName());
        }
        if (AppManager.getInstance().carTypes.size()>0)
            carType = AppManager.getInstance().carTypes.get(0);

        ArrayAdapter<String> adapterWashType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, washNames);
        adapterWashType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWashType.setAdapter(adapterWashType);
        spinnerWashType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String washName = adapterView.getItemAtPosition(i).toString();
                for (WashType type : AppManager.getInstance().washTypes){
                    if (type.getName().equals(washName)) {
                        washType = type;
                        break;
                    }
                }

                updateCost();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("BOOKING", "onNothingSelected");
            }
        });

        // select wash type
        for (int i = 0; i < AppManager.getInstance().washTypes.size(); i++) {
            WashType type = AppManager.getInstance().washTypes.get(i);
            if (currentOrder.menu.getIdx().contains(type.getIdx())){
                spinnerWashType.setSelection(i);
                break;
            }
        }

        ArrayAdapter<String>adapterCarType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, carNames);
        adapterCarType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarType.setAdapter(adapterCarType);
        spinnerCarType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String carName = adapterView.getItemAtPosition(i).toString();
                for (CarType type : AppManager.getInstance().carTypes){
                    if (type.getName().equals(carName)) {
                        carType = type;
                        break;
                    }
                }

                updateCost();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("BOOKING", "onNothingSelected");
            }
        });

        // select car type
        for (int i = 0; i < AppManager.getInstance().carTypes.size(); i++) {
            CarType type = AppManager.getInstance().carTypes.get(i);
            if (currentOrder.menu.getIdx().contains(type.getIdx())){
                spinnerCarType.setSelection(i);
                break;
            }
        }

        groupTap = (RadioGroup) findViewById(R.id.groupTap);
        groupTap.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_tap_yes:
                        hasTap = true;
                        break;
                    case R.id.rb_tap_no:
                        hasTap = false;
                        break;
                }

                checkRadioButtons();
            }
        });
        groupPlug = (RadioGroup) findViewById(R.id.groupPlug);
        groupPlug.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_plug_yes:
                        hasPlug = true;
                        break;
                    case R.id.rb_plug_no:
                        hasPlug = false;
                        break;
                }

                checkRadioButtons();
            }
        });

        // select radio group options
        if (currentOrder.hasTap){
            ((RadioButton) groupTap.getChildAt(0)).setChecked(true);
        } else {
            ((RadioButton) groupTap.getChildAt(1)).setChecked(true);
        }

        if (currentOrder.hasPlug) {
            ((RadioButton) groupPlug.getChildAt(0)).setChecked(true);
        } else {
            ((RadioButton) groupPlug.getChildAt(1)).setChecked(true);
        }

        txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditOrderActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearOf, int monthOfYear, int dayOfMonth) {
                        year = yearOf;  month = monthOfYear;  day = dayOfMonth;
                        calendar.set(year, month, day);
                        showDateTime(calendar);
                    }
                }, year, month, day).show();
            }
        });

        txtTime = (TextView) findViewById(R.id.txtTime);
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Time Picker Dialog
                IntervalTimePickerDialog timePickerDialog = new IntervalTimePickerDialog(EditOrderActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                                hour = hourOfDay;
                                minute = minuteOfHour;
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minuteOfHour);
                                showDateTime(calendar);
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentOrder.beginAt);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        showDateTime(calendar);

        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hud.show();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(EditOrderActivity.this), PLACE_PICKER_REQUEST); // for activity
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        // set Address of Current Order
        txtAddress.setText(currentOrder.location.getName());
        curLocation = currentOrder.location;

        ImageView btnAddress = (ImageView) findViewById(R.id.imgAddress);
        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hud.show();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(EditOrderActivity.this), PLACE_PICKER_REQUEST); // for activity
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        txtCost = (TextView) findViewById(R.id.txtCost);
        // set Price of Current Order
        txtCost.setText(String.valueOf(currentOrder.menu.getPrice()));
        AppCompatButton btnConfirm = (AppCompatButton) findViewById(R.id.btnOrder);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Date bookedAt = Util.getDate(year, month, day, hour, minute, 0);
                currentOrder.beginAt = bookedAt.getTime();
                currentOrder.endAt = currentOrder.beginAt + curMenu.getDuration()*1000;
                currentOrder.menu = curMenu;
                currentOrder.location = curLocation;
                currentOrder.hasTap = hasTap;
                currentOrder.hasPlug = hasPlug;

                if (isValidBooking(currentOrder)) {
                    if (isExistingTwo(currentOrder.beginAt)) {
                        final long validTime = generateValidTime(currentOrder.beginAt);
                        String validTimeString = TimeUtil.getFullTimeString(validTime);
                        String title = "Note";
                        String message = "Dear valued customer, this time slot is currently unavailable. The next available time slot is " + validTimeString + " Would you like to book this slot?";
                        AlertView alertView = new AlertView(title, message, getString(R.string.alert_button_cancel), new String[]{getString(R.string.alert_button_okay)}, null, EditOrderActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                if (position == 0) // ok button
                                {
                                    currentOrder.beginAt = validTime;
                                    currentOrder.endAt = currentOrder.beginAt + curMenu.getDuration()*1000;
                                    makeOrder(currentOrder);
                                    calendar.setTimeInMillis(currentOrder.beginAt);
                                    showDateTime(calendar);
                                }

                            }
                        });
                        alertView.show();
                    } else {
                        makeOrder(currentOrder);
                    }
                }
            }
        });

        updateCost();
    }

    private void makeOrder(Order order) {
        User session = AppManager.getSession();
        if (session != null) {
            if (session.getCardStatus() == 1 && !session.getCardToken().isEmpty()) {
                final String push_title = session.getFullName() + " has updated an offer.";
                final String push_message = carType.getName() + ", " + washType.getName() + " at " + TimeUtil.getSimpleDateString(order.beginAt);

                References.getInstance().ordersRef.child(order.idx).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditOrderActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        AppManager.getInstance().sendPushNotificationToService(push_title, push_message);
                    }
                });
            }
            else {
                Util.showAlert("Note", "Please verify your payment first", this);
            }
        }
    }

    private void showDateTime(Calendar calendar) {
        String fullyDateFormat = TimeUtil.getComplexTimeString(calendar);
        txtDate.setText(fullyDateFormat);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        txtTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
    }

    private void updateCost(){
        if (carType!=null && washType!=null){
            String key = washType.getIdx() + "_" + carType.getIdx();
            for (WashMenu cost : AppManager.getInstance().menuList) {
                if (cost.getIdx().equals(key)) {
                    curMenu = cost;
                    break;
                }
            }

            if (curMenu != null) {
                DecimalFormat df = new DecimalFormat("#,###,##0.00");
                txtCost.setText(String.valueOf(df.format(curMenu.getPrice())));
            } else {
                txtCost.setText(String.valueOf(0));
            }
        }
    }

    private void checkRadioButtons(){

        String title = "";
        if (hasTap && hasPlug) {
            spinnerWashType.setEnabled(true);
            return;
        } else if (!hasTap && hasPlug) {
            title = getString(R.string.book_error_no_tap);
        } else if (hasTap && !hasPlug) {
            title = getString(R.string.book_error_no_plug);
        } else if (!hasTap && !hasPlug) {
            title = getString(R.string.book_error_both);
        }

        showBottomAlert(title);
    }

    private void showBottomAlert(String title){
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_bottom_alert, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle); txtTitle.setText(title);
        AppCompatButton btnCancel = (AppCompatButton) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((RadioButton) groupTap.getChildAt(0)).setChecked(true);
                ((RadioButton) groupPlug.getChildAt(0)).setChecked(true);
                spinnerWashType.setSelection(0);

                dialog.dismiss();
            }
        });

        AppCompatButton btnOkay = (AppCompatButton) view.findViewById(R.id.btnOkay);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!hasTap && hasPlug) {
                    spinnerWashType.setSelection(3);
                } else if (hasTap && !hasPlug) {
                    spinnerWashType.setSelection(2);
                } else if (!hasTap && !hasPlug) {
                    spinnerWashType.setSelection(2);
                }
                spinnerWashType.setEnabled(false);
                dialog.dismiss();
            }
        });
    }

    private long generateValidTime(long time){
        long value = time - 3600 * 1000;
        while (true) {
            value += 3600 * 1000;
            if (TimeUtil.checkAvailableTimeRange(value) && ! isExistingTwo(value)) {
                break;
            }
        }
        return value;
    }

    private boolean isExistingTwo(long time){
        int existCount = 0;
        for (Order theOrder: AppManager.getInstance().orderList) {
            if (theOrder.beginAt <= time && time <= theOrder.endAt) {
                existCount += 1;
            }
        }

        return existCount >= 2;
    }

    private boolean isPastTime(long time) {
        boolean isPastTime = true;
        if (System.currentTimeMillis() < time) {
            isPastTime = false;
        }
        return isPastTime;
    }

    private Boolean isValidBooking(Order order) {

        if (curMenu == null) {
            Util.showAlert("Note!", "Please check your network, and reopen acuar", this);
            return false;
        }

        if (order.location == null) {
            txtAddress.setError("Please select location.");
            Toast.makeText(this, "Please select location.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TimeUtil.checkAvailableTimeRange(order.beginAt)) {
//            Toast.makeText(this, "The operating hours for the car wash is " + ServiceTimeStart +":00 to "+ServiceTimeEnd +":00.", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Dear valued client, our winter operating hours are " + ServiceTimeStart +":00 to "+ServiceTimeEnd +":00.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isPastTime(order.beginAt)) {
            Toast.makeText(this, "You can only book dates and times that are in the future.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        hud.dismiss();
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case PLACE_PICKER_REQUEST:
                    Place place = PlacePicker.getPlace(this, data);
                    txtAddress.setText(place.getAddress());
                    txtAddress.setError(null);
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;
                    curLocation = new OrderLocation(place.getAddress().toString(), latitude, longitude);
                    break;
                default:
                    break;
            }
        }
    }
}
