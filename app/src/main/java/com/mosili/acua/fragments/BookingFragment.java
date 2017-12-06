package com.mosili.acua.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mosili.acua.R;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.models.CarType;
import com.mosili.acua.models.OrderLocation;
import com.mosili.acua.models.WashType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class BookingFragment extends Fragment {

    private final int PLACE_PICKER_REQUEST = 999;

    private Spinner spinnerCarType, spinnerWashType;
    private RadioGroup radioTap, radioPlug;
    private OnFragmentInteractionListener mListener;

    private Calendar calendar = Calendar.getInstance();
    private TextView txtDate, txtTime;
    private int year, month, day, hour, minute;

    private TextView txtAddress;
    private OrderLocation location;

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

        List<String> washNames = new ArrayList<>();
        for (WashType washType : AppManager.getInstance().washTypes) {
            washNames.add(washType.getName());
        }
        List<String> carNames = new ArrayList<>();
        for (CarType carType : AppManager.getInstance().carTypes) {
            carNames.add(carType.getName());
        }

        ArrayAdapter<String>adapterWashType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, washNames);
        adapterWashType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWashType.setAdapter(adapterWashType);
        spinnerWashType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("BOOKING", adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("BOOKING", "onNothingSelected");
            }
        });

        ArrayAdapter<String>adapterCarType = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, carNames);
        adapterWashType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarType.setAdapter(adapterCarType);
        spinnerCarType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("BOOKING", adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("BOOKING", "onNothingSelected");
            }
        });

        radioTap = (RadioGroup) view.findViewById(R.id.radioTap);
        radioPlug = (RadioGroup) view.findViewById(R.id.radioPlug);
        int selectedTapId = radioTap.getCheckedRadioButtonId();
        int selectedPlugId = radioPlug.getCheckedRadioButtonId();

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

        AppCompatButton btnConfirm = (AppCompatButton) view.findViewById(R.id.btnOrder);

        return view;
    }

    private void showDate() {
        txtDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private void showTime() {
        txtTime.setText(hour + ":" + minute);
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
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;
                    location = new OrderLocation(place.getName().toString(), latitude, longitude);
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
