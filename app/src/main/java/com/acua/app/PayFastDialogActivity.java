package com.acua.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.acua.app.classes.AppManager;
import com.acua.app.models.Order;
import com.acua.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;

public class PayFastDialogActivity extends AppCompatActivity {

    private WebView webView;

    private KProgressHUD hud;
    private Boolean loadingFinished = false, redirect = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_fast_dialog);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.side_menu_payment));
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayFastDialogActivity.this.finish();
            }
        });

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this,R.color.colorTransparency))
                .setDimAmount(0.5f);

        Order order = AppManager.getInstance().focusedOrder;
        String orderName = AppManager.getTypesString(order);
        Double price = order.menu.getPrice();
        String urlString = Const.URL_HEROKU_PAYMENT + "?orderId=" + order.idx + "&orderName=" + orderName + "&price=" + price;

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(urlString.replace(" ", "%20"));
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                webView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                hud.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished && !redirect) {
                    //HIDE LOADING IT HAS FINISHED
                } else {
                    redirect = false;
                }

                hud.dismiss();
            }
        });
    }
}
