package com.acua.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationListener {

    private List<Notification> notifications = new ArrayList<>();
    private NotificationAdapter adapter;
    private User session;

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
                }
            }
        });

        notifications = AppManager.getInstance().notifications;
        adapter = new NotificationAdapter(this, notifications);
        listView.setAdapter(adapter);

        AppManager.getInstance().setNotificationListener(this);
    }

    @Override
    public void onReceivedNotification(Notification notification) {
        notifications = AppManager.getInstance().notifications;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRemovedNotification(Notification notification) {
        notifications = AppManager.getInstance().notifications;
        adapter.notifyDataSetChanged();
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
