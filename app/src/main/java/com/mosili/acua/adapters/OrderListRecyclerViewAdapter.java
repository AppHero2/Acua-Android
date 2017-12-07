package com.mosili.acua.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mosili.acua.R;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.interfaces.UserValueListener;
import com.mosili.acua.models.Order;
import com.mosili.acua.models.User;
import com.mosili.acua.utils.TimeUtil;
import com.mosili.acua.utils.Util;

import java.util.Date;
import java.util.List;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<OrderListRecyclerViewAdapter.ViewHolder> {

    private List<Order> orderList;

    public OrderListRecyclerViewAdapter(List<Order> items) {
        orderList = items;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = orderList.get(position);
        holder.updateData();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvUsername;
        public final TextView tvSchedule;
        public final TextView tvRemain;
        private ImageView imgProfile;

        public Order mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvUsername = (TextView) view.findViewById(R.id.tv_username);
            tvRemain = (TextView) view.findViewById(R.id.tv_remain);
            tvSchedule = (TextView) view.findViewById(R.id.tv_schedule);
            imgProfile = (ImageView) view.findViewById(R.id.img_profile);

        }

        public void updateData(){
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

        @Override
        public String toString() {
            return super.toString() + " '" + tvSchedule.getText() + "'";
        }
    }
}
