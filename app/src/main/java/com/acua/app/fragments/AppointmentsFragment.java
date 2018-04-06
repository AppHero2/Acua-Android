package com.acua.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.acua.app.utils.References;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment {

    private OnAppointmentsFragmentInteractionListener mListener;

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
    SwipeRefreshLayout swipeRefreshLayout;

    User session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        session = AppManager.getSession();

        rvOrders = (RecyclerView) view.findViewById(R.id.rv_orders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));
        tvEmpty = (TextView) view.findViewById(R.id.tv_empty);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        refreshDataManually();
                    }
                }, 1000);
            }
        });

        rvOrders.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapter = new OrderListRecyclerViewAdapter(this);
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

    private void refreshDataManually(){
        ValueEventListener trackOrdersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AppManager.getInstance().orderList.clear();
                AppManager.getInstance().selfOrders.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey();
                    Map<String, Object> value = (Map<String, Object>) child.getValue();
                    Order order = new Order(value);
                    order.idx = key;
                    AppManager.getInstance().orderList.add(order);
                    if (order.customerId.equals(session.getIdx())) {
                        AppManager.getInstance().selfOrders.add(order);
                    }
                }

                updateStatus(AppManager.getInstance().orderList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Track Orders", databaseError.toString());
            }
        };

        References.getInstance().ordersRef.addListenerForSingleValueEvent(trackOrdersListener);
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

    public void onClickRatService() {
        if (mListener != null) {
            mListener.onClickRateServiceInAppointments();
        }
    }

    public void onClickFeedback(){
        if (mListener != null) {
            mListener.onClickFeedbackInAppointments();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAppointmentsFragmentInteractionListener) {
            mListener = (OnAppointmentsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAppointmentsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAppointmentsFragmentInteractionListener {
        void onClickRateServiceInAppointments();
        void onClickFeedbackInAppointments();
    }
}
