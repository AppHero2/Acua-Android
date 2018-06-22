package com.acua.app;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.acua.app.classes.AppManager;
import com.acua.app.interfaces.NotificationListener;
import com.acua.app.models.Notification;
import com.acua.app.models.User;
import com.acua.app.utils.References;
import com.acua.app.utils.TimeUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acua.app.utils.Const.ADMIN_PUSH_ID;
import static com.acua.app.utils.Const.ADMIN_USER_ID;

public class NotificationsActivity extends AppCompatActivity implements RatingDialogListener{

    private ChildEventListener trackNotificationListener;

    private List<Notification> notifications = new ArrayList<>();
    private NotificationAdapter adapter;
    private User session;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        session = AppManager.getSession();

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle); txtTitle.setText(getString(R.string.notifications));
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationsActivity.this.finish();
            }
        });

        ListView listView = (ListView) findViewById(R.id.lst_notification);
        TextView emptyView = (TextView) findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cell cell = (Cell) view.getTag();
                final Notification news = cell.getNews();
                if (!news.isRead()){
                    References.getInstance().notificationsRef.child(session.getIdx()).child(news.getIdx()).child("isRead").setValue(true);

                    if (news.getTitle().equals("Please Rate our Service")){
                        NotificationsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRatingDiglog();
                            }
                        });
                    }
                }
            }
        });

        notifications = AppManager.getInstance().notifications;
        adapter = new NotificationAdapter(this, notifications);
        listView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        refreshNotificationData();
                    }
                }, 1000);
            }
        });

        startTrackingNotification(session.getIdx());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTrackingNotification(session.getIdx());
    }

    @Override
    public void onPositiveButtonClicked(int rate, String comment) {
        // interpret results, send it to analytics etc...
        User session = AppManager.getSession();
        String title = session.getFullName() + " left service rating as " + rate + ".";
        AppManager.getInstance().sendPushNotificationToCustomer(ADMIN_PUSH_ID, title,  comment);
        DatabaseReference reference = References.getInstance().notificationsRef.child(ADMIN_USER_ID).push();
        String notificationId = reference.getKey();
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("idx", notificationId);
        notificationData.put("title", title);
        notificationData.put("message", comment);
        notificationData.put("createdAt", System.currentTimeMillis());
        notificationData.put("isRead", false);
        reference.setValue(notificationData);
    }

    @Override
    public void onNegativeButtonClicked() {
        Log.d("NotificationsActivity", "onNegativeButtonClicked: Cancel");
    }

    @Override
    public void onNeutralButtonClicked() {
        Log.d("NotificationsActivity", "onNeutralButtonClicked: Later");
    }

    private void showRatingDiglog(){
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(4)
                .setTitle("Please rate our service")
                .setDescription("Please select some stars and give your feedback")
//                .setDefaultComment("")
                .setStarColor(R.color.colorRatingStart)
                .setNoteDescriptionTextColor(R.color.colorGrey)
                .setTitleTextColor(R.color.colorTextBlue)
                .setDescriptionTextColor(R.color.colorGrey)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.colorGrey)
                .setCommentTextColor(R.color.colorGrey)
                .setCommentBackgroundColor(R.color.colorRatingCommentBG)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .create(NotificationsActivity.this)
                .show();
    }

    private void refreshNotificationData(){
        notifications = AppManager.getInstance().notifications;
        adapter.notifyDataSetChanged();
    }

    public void stopTrackingNotification (String uid){
        if (trackNotificationListener != null)
            References.getInstance().notificationsRef.child(uid).removeEventListener(trackNotificationListener);
    }

    public void startTrackingNotification(String uid){
        if (trackNotificationListener != null)
            return;

        trackNotificationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() !=  null) {
                    refreshNotificationData();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    refreshNotificationData();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() !=  null) {
                    refreshNotificationData();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TrackNotification", databaseError.toString());
            }
        };

        References.getInstance().notificationsRef.child(uid).addChildEventListener(trackNotificationListener);
    }


    private class NotificationAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater layoutInflater;
        private List<Notification> items = new ArrayList<>();
        private List<Cell> cells = new ArrayList<>();

        public NotificationAdapter(Context context, List<Notification> items){
            this.context = context;
            this.items = items;
            this.layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Notification getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            Cell cell;
            View cellView = convertView;
            if (convertView == null) {
                cellView = this.layoutInflater.inflate(R.layout.row_notification, null);
                cell = new Cell(cellView);
                cellView.setTag(cell);
            } else {
                cell = (Cell) cellView.getTag();
            }

            cell.setData(getItem(position));

            return cellView;
        }
    }

    private class Cell {

        private View mView;
        private TextView tvTitle, tvContent, tvDate;
        private View badgeView;

        private Notification news;

        public Notification getNews() {
            return news;
        }

        public Cell(View itemView) {
            mView = itemView;
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            badgeView = (View) itemView.findViewById(R.id.badgeView);
        }

        public void setData(Notification news){
            this.news = news;
            tvTitle.setText(news.getTitle());
            tvContent.setText(news.getMessage());
            String date = TimeUtil.getDateString(news.getCreatedAt());
            String todayDate = TimeUtil.getDateString(System.currentTimeMillis());

            if (todayDate.compareTo(date) == 0) {
                tvDate.setText(TimeUtil.getUserTime(news.getCreatedAt()));
            } else {
                tvDate.setText(TimeUtil.getUserFriendlyDate(
                        mView.getContext(),
                        news.getCreatedAt()
                ));
            }

            if (news.isRead()) {
                badgeView.setVisibility(View.GONE);
            } else {
                badgeView.setVisibility(View.VISIBLE);
            }
        }
    }
}
