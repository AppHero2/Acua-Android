package com.acua.app.fragments;

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

import com.acua.app.R;
import com.acua.app.adapters.OrderListRecyclerViewAdapter;
import com.acua.app.classes.AppManager;
import com.acua.app.interfaces.OrderValueListener;
import com.acua.app.models.Order;
import com.acua.app.models.User;

import java.util.List;

public class AppointmentsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AppointmentsFragment() {
        // Required empty public constructor
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

        adapter = new OrderListRecyclerViewAdapter(getActivity());
        adapter.startUpdateTimer();
        rvOrders.setAdapter(adapter);
        updateStatus(AppManager.getInstance().selfOrders);

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
