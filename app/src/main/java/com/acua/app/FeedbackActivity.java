package com.acua.app;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acua.app.classes.AppManager;
import com.acua.app.interfaces.ResultListener;
import com.acua.app.interfaces.UserValueListener;
import com.acua.app.models.CarType;
import com.acua.app.models.Feedback;
import com.acua.app.models.Order;
import com.acua.app.models.User;
import com.acua.app.models.WashType;
import com.acua.app.utils.References;
import com.acua.app.utils.TimeUtil;
import com.acua.app.utils.Util;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acua.app.utils.Const.ADMIN_PUSH_ID;
import static com.acua.app.utils.Const.ADMIN_USER_ID;

public class FeedbackActivity extends AppCompatActivity {

    private View positiveAction;
    private EditText etFeedback;
    private TextView tvMessage;

    private int issueType = 0;
    private Order lastOrder ;
    private User washer;


    private ChildEventListener trackFeedbackListener;

    private List<Feedback> feedbacks = new ArrayList<>();
    private FeedbackAdapter adapter;
    private User session;
    private String washType = "";
    private String carType = "";

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        session = AppManager.getSession();

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle); txtTitle.setText(getString(R.string.feedback));
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.this.finish();
            }
        });

        RelativeLayout layoutCustomer = findViewById(R.id.layout_customer);
        RelativeLayout layoutOperator = findViewById(R.id.layout_operator);

        if (AppManager.getInstance().session.getUserType() == 0) // normal user
        {
            layoutCustomer.setVisibility(View.VISIBLE);
            layoutOperator.setVisibility(View.GONE);

            lastOrder = AppManager.getInstance().lastFeedbackOrder;

            TextView tvDate = findViewById(R.id.tv_date);
            TextView tvType = findViewById(R.id.tv_type);
//            final TextView tvOperator = findViewById(R.id.tv_operator);

            tvDate.setText(TimeUtil.getDateString(lastOrder.completedAt) + " at " + TimeUtil.getUserTime(lastOrder.completedAt));

            String[] types = lastOrder.menu.getIdx().split("_");
            String washTypeId = types[0];
            String carTypeId = types[1];

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
            tvType.setText(carType + ", " + washType + "  ZAR " + lastOrder.menu.getPrice());

            RadioGroup groupReport = (RadioGroup) findViewById(R.id.groupReport);
            groupReport.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {

                    switch (i) {
                        case R.id.rb_1:
                            issueType = 1;
                            break;
                        case R.id.rb_2:
                            issueType = 2;
                            break;
                        case R.id.rb_3:
                            issueType = 3;
                            break;
                        case R.id.rb_4:
                            issueType = 4;
                            break;
                        case R.id.rb_5:
                            issueType = 5;
                            break;
                        case R.id.rb_6:
                            issueType = 6;
                            break;
                    }

                    String title = getString(R.string.feedback);
                    String message = Feedback.getFeedbackContent(FeedbackActivity.this, issueType);
                    String positive = getString(R.string.feedback_submit);
                    String negative = getString(R.string.feedback_cancel);

                    showFeedbackDialog(title, message, positive, negative);

                }
            });

        }
        else // admin & operator
        {
            layoutCustomer.setVisibility(View.GONE);
            layoutOperator.setVisibility(View.VISIBLE);

            ListView listView = (ListView) findViewById(R.id.listFeedback);
            TextView emptyView = (TextView) findViewById(R.id.emptyView);
            listView.setEmptyView(emptyView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Cell cell = (Cell) view.getTag();
                    final Feedback feedback = cell.getFeedback();

                }
            });

            adapter = new FeedbackAdapter(this, feedbacks);
            listView.setAdapter(adapter);

            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 1000);
                }
            });

            startTrackingFeedback(session.getIdx());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (session != null) stopTrackingNotification(session.getIdx());
    }

    public void stopTrackingNotification (String uid){
        if (trackFeedbackListener != null)
            References.getInstance().notificationsRef.child(uid).removeEventListener(trackFeedbackListener);
    }

    public void startTrackingFeedback(String uid){
        if (trackFeedbackListener != null)
            return;

        trackFeedbackListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() !=  null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Feedback feedback = new Feedback(data);
                    feedbacks.add(feedback);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Feedback feedback = new Feedback(data);
                    for (Feedback feed: feedbacks) {
                        if (feedback.getIdx().equals(feed.getIdx())) {
                            feed.updateData(data);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() !=  null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Feedback feedback = new Feedback(data);
                    for (Feedback feed: feedbacks) {
                        if (feedback.getIdx().equals(feed.getIdx())) {
                            feedbacks.remove(feed);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
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

        References.getInstance().feedbackRef.addChildEventListener(trackFeedbackListener);
    }

    private void showFeedbackDialog(String title, String message, String positive, String negative){
        final MaterialDialog dialog =
                new MaterialDialog.Builder(this)
//                        .title(title)
                        .customView(R.layout.dialog_customview, true)
                        .positiveText(positive)
                        .negativeText(negative)
                        .build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitFeedback();
                dialog.dismiss();
            }
        });

        etFeedback = dialog.getCustomView().findViewById(R.id.etFeedback);
        etFeedback.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        positiveAction.setEnabled(s.toString().trim().length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        tvMessage = dialog.getCustomView().findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        dialog.show();
        positiveAction.setEnabled(false); // disabled by default
    }

    private void submitFeedback(){
        String subject = Feedback.getFeedbackTitle(this, issueType);
        String content = etFeedback.getText().toString();
        String html = content + "\n\n"
                + session.getFullName() + "\n"
                + "(" + session.getEmail() + ")" + "\n"
                + session.getPhone() + "\n"
                + carType + " " + washType +"\n"
                + TimeUtil.getFullTimeString(lastOrder.completedAt);
        AppManager.getInstance().sendEmailPushToADMIN(subject, subject, html, new ResultListener() {
            @Override
            public void onResponse(boolean success, String response) {
                String result = "Your feedback has been sent successfully.";
                if (!success) {
                    result = "Failed to send your feedback. Please try again...";
                }
                Toast.makeText(FeedbackActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /// ---->>> ADAPTER
    private class FeedbackAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater layoutInflater;
        private List<Feedback> items = new ArrayList<>();
        private List<Cell> cells = new ArrayList<>();

        public FeedbackAdapter(Context context, List<Feedback> items){
            this.context = context;
            this.items = items;
            this.layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Feedback getItem(int i) {
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
                cellView = this.layoutInflater.inflate(R.layout.row_feedback, null);
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

        private Feedback feedback;

        public Feedback getFeedback() {
            return feedback;
        }

        public Cell(View itemView) {
            mView = itemView;
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
        }

        public void setData(Feedback feedback){
            this.feedback = feedback;

            String issueTitle = Feedback.getFeedbackTitle(FeedbackActivity.this, issueType);
            tvTitle.setText(issueTitle);
            tvContent.setText(feedback.getContent());
            String date = TimeUtil.getDateString(feedback.getCreatedAt());
            String todayDate = TimeUtil.getDateString(System.currentTimeMillis());

            AppManager.getUser(feedback.getSenderID(), new UserValueListener() {
                @Override
                public void onLoadedUser(User user) {
                    String issueTitle = Feedback.getFeedbackTitle(FeedbackActivity.this, issueType) + " :\n " + user.getFullName() + "(" + user.getEmail() + ")";
                    tvTitle.setText(issueTitle);
                }
            });

            if (todayDate.compareTo(date) == 0) {
                tvDate.setText(TimeUtil.getUserTime(feedback.getCreatedAt()));
            } else {
                tvDate.setText(TimeUtil.getUserFriendlyDate(
                        mView.getContext(),
                        feedback.getCreatedAt()
                ));
            }

        }
    }
}
