package com.mosili.acua.adapters;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mosili.acua.R;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.CarType;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.User;
import com.mosili.acua.models.WashType;
import com.mosili.acua.utils.TimeUtil;
import com.mosili.acua.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Order> orderList = new ArrayList<>();
    private User session;
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
                        case 0:{
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

    public OrderListRecyclerViewAdapter(User session) {
        this.session = session;
        this.type = session.getUserType();
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
                ViewHolder0 viewholder  =new ViewHolder0(view);
                synchronized (holders) {
                    holders.add(viewholder);
                }
                return viewholder;
            }
            case 1:{
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_order, parent, false);
                ViewHolder1 viewholder  = new ViewHolder1(view);
                synchronized (holders) {
                    holders.add(viewholder);
                }
                return viewholder;
            }
            default:{
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_order, parent, false);
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

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvUsername;
        public final TextView tvTypes;
        public final TextView tvAddress;
        public final TextView tvSchedule;
        public final TextView tvRemain;
        private ImageView imgProfile;

        public Order mItem;

        public ViewHolder1(View view) {
            super(view);
            mView = view;
            tvUsername = (TextView) view.findViewById(R.id.tv_username);
            tvTypes = (TextView) view.findViewById(R.id.tv_types);
            tvAddress = (TextView) view.findViewById(R.id.tv_address);
            tvRemain = (TextView) view.findViewById(R.id.tv_remain);
            tvSchedule = (TextView) view.findViewById(R.id.tv_schedule);
            imgProfile = (ImageView) view.findViewById(R.id.img_profile);
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
                    tvUsername.setText(user.getFirstname() + " " + user.getLastname());
                    Util.setProfileImage(user.getPhoto(), imgProfile);
                    String startAt = TimeUtil.getSimpleDateString(mItem.beginAt);
                    String endAt = TimeUtil.getSimpleTimeString(mItem.endAt);
                    String schedule = startAt + " ~ " + endAt;
                    tvSchedule.setText(schedule);
                }
            });
        }

        private boolean isExpired = false;
        public boolean isExpired() {
            return isExpired;
        }

        public void updateTimeRemaining(long currentTime) {
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

    public class ViewHolder0 extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvTypes;
        public final TextView tvAddress;
        public final TextView tvSchedule;
        public final TextView tvRemain;

        public Order mItem;

        public ViewHolder0(View view) {
            super(view);
            mView = view;
            tvTypes = (TextView) view.findViewById(R.id.tv_types);
            tvAddress = (TextView) view.findViewById(R.id.tv_address);
            tvRemain = (TextView) view.findViewById(R.id.tv_remain);
            tvSchedule = (TextView) view.findViewById(R.id.tv_schedule);
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
        }

        private boolean isExpired = false;
        public boolean isExpired() {
            return isExpired;
        }

        public void updateTimeRemaining(long currentTime) {
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
