package com.acua.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class RegisterTermsActivity extends AppCompatActivity implements View.OnClickListener{


    private LinearLayout layoutGetStarted;
    private ScrollView scrlTerms;
    private TextView txtTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_terms);

        Button btnStart = (Button) findViewById(R.id.btnStart); btnStart.setOnClickListener(this);
        Button btnAccept = (Button) findViewById(R.id.btnAccept); btnAccept.setOnClickListener(this);
        Button btnDecline = (Button) findViewById(R.id.btnDecline); btnDecline.setOnClickListener(this);

        layoutGetStarted = (LinearLayout) findViewById(R.id.layout_get_started);
        scrlTerms = (ScrollView) findViewById(R.id.scrlTerms);
        txtTerms = (TextView) findViewById(R.id.txtTerms);
        txtTerms.setText(Html.fromHtml(getString(R.string.terms_content)));

        layoutGetStarted.setVisibility(View.VISIBLE); scrlTerms.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int idx = view.getId();
        switch (idx) {
            case R.id.btnStart: {
                layoutGetStarted.setVisibility(View.GONE); scrlTerms.setVisibility(View.VISIBLE);
            }
                break;
            case R.id.btnAccept: {
                startActivity(new Intent(this, RegisterPhoneActivity.class));
            }
                break;
            case R.id.btnDecline: {
                finish();
            }
                break;
        }
    }
}
