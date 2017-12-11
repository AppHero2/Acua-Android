package com.mosili.acua.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mosili.acua.R;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.models.CarType;
import com.mosili.acua.models.WashMenu;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.OrderLocation;
import com.mosili.acua.models.WashType;
import com.mosili.acua.utils.References;
import com.mosili.acua.utils.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class BookingFragment extends Fragment {

    private final int PLACE_PICKER_REQUEST = 999;

    private Spinner spinnerCarType, spinnerWashType;
    private RadioGroup radioTap, radioPlug;
    private OnFragmentInteractionListener mListener;

    private Calendar calendar = Calendar.getInstance();
    private TextView txtDate, txtTime,  txtCost;
    private int year, month, day, hour, minute;

    private TextView txtAddress;

    private OrderLocation curLocation;
    private CarType carType;
    private WashType washType;
    private WashMenu curMenu;

    public BookingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        spinnerWashType = (Spinner) view.findViewById(R.id.spinerWashTypes);
        spinnerCarType = (Spinner) view.findViewById(R.id.spinerCarTypes);

        final List<String> washNames = new ArrayList<>();
        for (WashType washType : AppManager.getInstance().washTypes) {
            washNames.add(washType.getName());
        }
        if (AppManager.getInstance().washTypes.size()>0)
            washType = AppManager.getInstance().washTypes.get(0);
        List<String> carNames = new ArrayList<>();
        for (CarType carType : AppManager.getInstance().carTypes) {
            carNames.add(carType.getName());
        }
        if (AppManager.getInstance().carTypes.size()>0)
            carType = AppManager.getInstance().carTypes.get(0);

        ArrayAdapter<String>adapterWashType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, washNames);
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

        ArrayAdapter<String>adapterCarType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, carNames);
        adapterWashType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        radioTap = (RadioGroup) view.findViewById(R.id.radioTap);
        radioPlug = (RadioGroup) view.findViewById(R.id.radioPlug);

        txtDate = (TextView) view.findViewById(R.id.txtDate);
        txtTime = (TextView) view.findViewById(R.id.txtTime);
        ImageView btnCalendar = (ImageView) view.findViewById(R.id.imgDay);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearOf, int monthOfYear, int dayOfMonth) {
                        year = yearOf;  month = monthOfYear;  day = dayOfMonth;
                        showDate();
                    }
                }, year, month, day).show();
            }
        });

        ImageView btnTimer = (ImageView) view.findViewById(R.id.imgTime);
        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                                hour = hourOfDay;
                                minute = minuteOfHour;
                                showTime();
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        showDate();
        showTime();

        txtAddress = (TextView) view.findViewById(R.id.txtAddress);
        ImageView btnAddress = (ImageView) view.findViewById(R.id.imgAddress);
        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST); // for activty
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        txtCost = (TextView) view.findViewById(R.id.txtCost);
        AppCompatButton btnConfirm = (AppCompatButton) view.findViewById(R.id.btnOrder);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order order = new Order();
                order.menu = curMenu;
                order.location = curLocation;
                order.customerId = AppManager.getSession().getIdx();
                Date bookedAt = Util.getDate(year, month, day, hour, minute, 0);
                order.beginAt = bookedAt.getTime();
                order.endAt = bookedAt.getTime() + curMenu.getDuration()*1000;

                if (isValidBooking(order)) {
                    References.getInstance().ordersRef.child(String.valueOf(order.beginAt)).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // TODO: send notification to service
                            Log.d("BOOKING", "Booked successfully");
                            Toast.makeText(getActivity(), "Booked successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        updateCost();

        return view;
    }

    private void showDate() {
        txtDate.setText(day + "/" + (month+1) + "/" + year);
    }

    private void showTime() {
        txtTime.setText(hour + ":" + minute);
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
                txtCost.setText(String.valueOf(curMenu.getPrice()));
            } else {
                txtCost.setText(String.valueOf(0));
            }
        }
    }

    private Boolean isValidBooking(Order order) {

        if (curMenu == null) {
            Util.showAlert("Note!", "Please check your network, and reopen acua", getActivity());
            return false;
        }

        if (order.location == null) {
            txtAddress.setError("Please select location.");
            Toast.makeText(getActivity(), "Please select location.", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (Order theOrder: AppManager.getInstance().orderList) {
            if (theOrder.beginAt <= order.beginAt && order.beginAt <= theOrder.endAt) {
                Util.showAlert("Note!", "You could not book at the moment because another customer made already booking before you.", getActivity());
                return false;
            }
        }

        return true;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        checkPermissionOnActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case PLACE_PICKER_REQUEST:
                    Place place = PlacePicker.getPlace(getActivity(), data);
                    txtAddress.setText(place.getName());
                    txtAddress.setError(null);
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;
                    curLocation = new OrderLocation(place.getName().toString(), latitude, longitude);
                break;
                default:
                    break;
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
