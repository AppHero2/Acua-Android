package com.mosili.acua.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mosili.acua.EditOrderActivity;
import com.mosili.acua.MapActivity;
import com.mosili.acua.R;
import com.mosili.acua.alertView.AlertView;
import com.mosili.acua.alertView.OnDismissListener;
import com.mosili.acua.alertView.OnItemClickListener;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.CarType;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.OrderServiceStatus;
import com.mosili.acua.models.User;
import com.mosili.acua.models.WashType;
import com.mosili.acua.utils.References;
import com.mosili.acua.utils.TimeUtil;
import com.mosili.acua.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Order> orderList = new ArrayList<>();
    private User session;
    private Activity activity;
    private int type = 0;
    private List<RecyclerView.ViewHolder> holders = new ArrayList<>();
    private Handler mHandler = new Handler();
    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (holders) {
                long currentTime = System.currentTimeMillis();
                for (RecyclerView.ViewHolder holder: holders){
                    switch (holder.getItemViewType()) {
                        case 0: {
                            ViewHolder0 viewHolder0 = (ViewHolder0)holder;
                            viewHolder0.updateTimeRemaining(currentTime);
                        }
                        break;
                        case 1: {
                            ViewHolder1 viewHolder1 = (ViewHolder1)holder;
                            viewHolder1.updateTimeRemaining(currentTime);
                        }
                        break;
                    }
                }
            }
        }
    };

    public void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }

    public OrderListRecyclerViewAdapter(User session, Activity activity) {
        this.session = session;
        this.type = session.getUserType();
        this.activity = activity;
    }

    public void setOrderList(List<Order> orders) {
        orderList.clear();

        if ( type == 0) // customer
        {
            for (Order order : orders) {
                if (order.customerId.equals(session.getIdx())) {
                    orderList.add(order);
                }
            }
        }
        else if (type == 1) // service
        {
            for (Order order: orders) {
                orderList.add(order);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:{
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_order_self, parent, false);
                ViewHolder0 viewholder  = new ViewHolder0(view);
                synchronized (holders) {
                    holders.add(viewholder);
                }
                return viewholder;
            }
            case 1:{
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_order_admin, parent, false);
                ViewHolder1 viewholder  = new ViewHolder1(view);
                synchronized (holders) {
                    holders.add(viewholder);
                }
                return viewholder;
            }
            default:{
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_order_admin, parent, false);
                ViewHolder0 viewholder  = new ViewHolder0(view);
                synchronized (holders) {
                    holders.add(viewholder);
                }
                return viewholder;
            }
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:{
                ViewHolder0 viewHolder0 = (ViewHolder0)holder;
                viewHolder0.mItem = orderList.get(position);
                viewHolder0.updateData();
            }
                break;
            case 1: {
                ViewHolder1 viewHolder1 = (ViewHolder1)holder;
                viewHolder1.mItem = orderList.get(position);
                viewHolder1.updateData();
            }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }


    /// customer cell
    public class ViewHolder0 extends RecyclerView.ViewHolder {
        public View mView;
        public TextView tvTypes, tvAddress, tvSchedule, tvRemain, tvStatus;

        private int alertButtonPostion = AlertView.CANCELPOSITION;

        public Order mItem;

        public ViewHolder0(View view) {
            super(view);
            mView = view;
            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertView actionSheet = new AlertView.Builder().setContext(view.getContext())
                            .setStyle(AlertView.Style.ActionSheet)
                            .setTitle("Please confirm your booking")
                            .setMessage(null)
                            .setCancelText("Dismiss")
                            .setDestructive("Update Booking", "Withdraw Booking")
                            .setOthers(null)
                            .setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(Object o, int position) {
                                    alertButtonPostion = position;
                                }
                            })
                            .build();

                    actionSheet.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(Object o) {
                            if (alertButtonPostion != AlertView.CANCELPOSITION) {
                                if (alertButtonPostion == 0){
                                    AppManager.getInstance().currentOrder = mItem;
                                    activity.startActivity(new Intent(activity, EditOrderActivity.class));
                                }else if (alertButtonPostion == 1){
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertView alertView = new AlertView("Are you sure?", "Do you really want to withdraw this order?", "Cancel", null, new String[]{"Okay"}, activity, AlertView.Style.Alert, new OnItemClickListener() {
                                                @Override
                                                public void onItemClick(Object o, int position) {
                                                    if (position != AlertView.CANCELPOSITION){
                                                        References.getInstance().ordersRef.child(mItem.idx).removeValue();
                                                    }
                                                }
                                            });
                                            alertView.show();
                                        }
                                    });
                                }
                            }
                        }
                    });

                    actionSheet.show();

                    return true;
                }
            });
            tvTypes = (TextView) view.findViewById(R.id.tv_types);
            tvAddress = (TextView) view.findViewById(R.id.tv_address);
            tvRemain = (TextView) view.findViewById(R.id.tv_remain);
            tvSchedule = (TextView) view.findViewById(R.id.tv_schedule);
            tvStatus = (TextView) view.findViewById(R.id.tv_status);
        }

        public void updateData(){
            String[] types = mItem.menu.getIdx().split("_");
            String washTypeId = types[0];
            String carTypeId = types[1];
            String washType = "";
            String carType = "";
            for (WashType type : AppManager.getInstance().washTypes) {
                if (type.getIdx().equals(washTypeId)) {
                    washType = type.getName();
                    break;
                }
            }
            for (CarType type : AppManager.getInstance().carTypes) {
                if (type.getIdx().equals(carTypeId)) {
                    carType = type.getName();
                    break;
                }
            }
            tvTypes.setText(carType + ", " + washType);
            tvAddress.setText(mItem.location.getName());
            String startAt = TimeUtil.getSimpleDateString(mItem.beginAt);
            String endAt = TimeUtil.getSimpleTimeString(mItem.endAt);
            String schedule = startAt + " ~ " + endAt;
            tvSchedule.setText(schedule);
            tvStatus.setText(String.valueOf(mItem.serviceStatus));
        }

        private boolean isExpired = false;
        public boolean isExpired() {
            return isExpired;
        }

        public void updateTimeRemaining(long currentTime) {

            if (mItem != null){
                if (mItem.serviceStatus == OrderServiceStatus.COMPLETED) {
                    tvRemain.setText("service completed");
                    return;
                }
            }

            long timeDiff = mItem.endAt - currentTime;
            if (timeDiff > 0) {
                String remaining = TimeUtil.formatHMSM(timeDiff);
                tvRemain.setText(remaining);
                this.isExpired = false;
            } else {
                tvRemain.setText("Expired!!");
                this.isExpired = true;
            }
        }
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        public View mView;
        public TextView tvUsername, tvTypes, tvAddress, tvSchedule, tvRemain, tvStatus;
        private ImageView imgProfile;
        private ImageView btnLocation, btnPhoneCall;
        private AppCompatButton btnAction;

        public Order mItem;
        private User mUser;

        public ViewHolder1(View view) {
            super(view);
            mView = view;
            tvUsername = (TextView) view.findViewById(R.id.tv_username);
            tvTypes = (TextView) view.findViewById(R.id.tv_types);
            tvAddress = (TextView) view.findViewById(R.id.tv_address);
            tvRemain = (TextView) view.findViewById(R.id.tv_remain);
            tvSchedule = (TextView) view.findViewById(R.id.tv_schedule);
            tvStatus = (TextView) view.findViewById(R.id.tv_status);
            imgProfile = (ImageView) view.findViewById(R.id.img_profile);

            btnLocation = (ImageView) view.findViewById(R.id.btn_location);
            btnLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppManager.getInstance().focusedOrder = mItem;
                    activity.startActivity(new Intent(activity, MapActivity.class));
                }
            });
            btnPhoneCall = (ImageView) view.findViewById(R.id.btn_phone_call);
            btnPhoneCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mUser != null) {
                        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mUser.getPhone()));
                        try {
                            activity.startActivity(in);
                        } catch (SecurityException ex) {
                            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            btnAction = (AppCompatButton) view.findViewById(R.id.btn_action);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItem.serviceStatus == OrderServiceStatus.PENDING) {
                        mItem.serviceStatus = OrderServiceStatus.ACCEPTED;
                        References.getInstance().ordersRef.child(mItem.idx).setValue(mItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                AppManager.getInstance().sendPushNotificationToCustomer(mUser.getPushToken(), "Accepted your offer!",  session.getFullName() + " has accepted your offer!");
                            }
                        });
                    } else if (mItem.serviceStatus == OrderServiceStatus.ACCEPTED) {
                        mItem.serviceStatus = OrderServiceStatus.COMPLETED;
                        References.getInstance().ordersRef.child(mItem.idx).setValue(mItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                AppManager.getInstance().sendPushNotificationToCustomer(mUser.getPushToken(), "Completed your offer!",  session.getFullName() + " has completed your offer! You need to pay for the service");
                            }
                        });
                    } else {

                    }
                }
            });

        }

        public void updateData(){
            String[] types = mItem.menu.getIdx().split("_");
            String washTypeId = types[0];
            String carTypeId = types[1];
            String washType = "";
            String carType = "";
            for (WashType type : AppManager.getInstance().washTypes) {
                if (type.getIdx().equals(washTypeId)) {
                    washType = type.getName();
                    break;
                }
            }
            for (CarType type : AppManager.getInstance().carTypes) {
                if (type.getIdx().equals(carTypeId)) {
                    carType = type.getName();
                    break;
                }
            }
            tvTypes.setText(carType + ", " + washType);
            tvAddress.setText(mItem.location.getName());
            String userId = mItem.customerId;
            AppManager.getUser(userId, new UserValueListener() {
                @Override
                public void onLoadedUser(User user) {
                    mUser = user;
                    tvUsername.setText(user.getFirstname() + " " + user.getLastname());
                    Util.setProfileImage(user.getPhoto(), imgProfile);
                    String startAt = TimeUtil.getSimpleDateString(mItem.beginAt);
                    String endAt = TimeUtil.getSimpleTimeString(mItem.endAt);
                    String schedule = startAt + " ~ " + endAt;
                    tvSchedule.setText(schedule);
                }
            });

            if (mItem.serviceStatus == OrderServiceStatus.PENDING) {
                tvStatus.setText("In complete");
                btnAction.setText("Engage");
            } else if (mItem.serviceStatus == OrderServiceStatus.ACCEPTED) {
                tvStatus.setText("In Progress");
                btnAction.setText("Done");
            } else {
                tvStatus.setText("Completed");
                btnAction.setVisibility(View.GONE);
            }
        }

        private boolean isExpired = false;
        public boolean isExpired() {
            return isExpired;
        }

        public void updateTimeRemaining(long currentTime) {

            if (mItem != null){
                if (mItem.serviceStatus == OrderServiceStatus.COMPLETED) {
                    tvRemain.setText("service completed");
                    return;
                }
            }

            long timeDiff = mItem.endAt - currentTime;
            if (timeDiff > 0) {
                String remaining = TimeUtil.formatHMSM(timeDiff);
                tvRemain.setText(remaining);
                this.isExpired = false;
            } else {
                tvRemain.setText("Expired!!");
                this.isExpired = true;
            }
        }
    }
}
