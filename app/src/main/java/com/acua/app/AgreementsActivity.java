package com.acua.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class AgreementsActivity extends AppCompatActivity implements View.OnClickListener{

    private ScrollView scrlTerms;
    private TextView txtTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreements);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Agreements");
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreementsActivity.this.finish();
            }
        });

        Button btnAccept = (Button) findViewById(R.id.btnAccept); btnAccept.setOnClickListener(this);
        Button btnDecline = (Button) findViewById(R.id.btnDecline); btnDecline.setOnClickListener(this);

        scrlTerms = (ScrollView) findViewById(R.id.scrlTerms);
        txtTerms = (TextView) findViewById(R.id.txtTerms);
        txtTerms.setText(Html.fromHtml(getString(R.string.terms_content)));

    }

    @Override
    public void onClick(View view) {
        int idx = view.getId();
        switch (idx) {
            case R.id.btnAccept: {
                finish();
            }
            break;
            case R.id.btnDecline: {
                finishAffinity();
            }
            break;
        }
    }
}
