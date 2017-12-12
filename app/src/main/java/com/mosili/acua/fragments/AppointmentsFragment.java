package com.mosili.acua.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mosili.acua.R;
import com.mosili.acua.adapters.OrderListRecyclerViewAdapter;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.OrderValueListener;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.User;

import java.util.List;

public class AppointmentsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AppointmentsFragment() {
        // Required empty public constructor
    }

    public static AppointmentsFragment newInstance(String param1, String param2) {
        AppointmentsFragment fragment = new AppointmentsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    RecyclerView rvOrders;
    TextView tvEmpty;

    OrderListRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        rvOrders = (RecyclerView) view.findViewById(R.id.rv_orders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));
        tvEmpty = (TextView) view.findViewById(R.id.tv_empty);

        User session = AppManager.getSession();
        adapter = new OrderListRecyclerViewAdapter(session);
        adapter.startUpdateTimer();
        rvOrders.setAdapter(adapter);
        updateStatus(AppManager.getInstance().orderList);

        AppManager.getInstance().setOrderValueListener(new OrderValueListener() {
            @Override
            public void onLoadedOrder(List<Order> orders) {
                updateStatus(orders);
            }
        });

        return view;
    }

    private void updateStatus(List<Order> orders){

        adapter.setOrderList(orders);
        adapter.notifyDataSetChanged();

        if (orders.size() == 0) {
            rvOrders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

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
