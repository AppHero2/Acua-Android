package com.acua.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.acua.app.classes.AppManager;
import com.acua.app.models.PayCard;
import com.acua.app.models.User;
import com.acua.app.utils.Const;
import com.acua.app.utils.References;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acua.app.utils.Const.KEY_STRIPE_KEY;

public class PaymentActivity extends AppCompatActivity {

    private KProgressHUD hud;
    private CardInputWidget cardInputWidget;

    private SimpleAdapter mAdatper;
    private List<Map<String, String>> mCardTokens = new ArrayList<Map<String, String>>();

    private WebView webView;
    private Boolean loadingFinished = false, redirect = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this,R.color.colorTransparency))
                .setDimAmount(0.5f);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.side_menu_payment));
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentActivity.this.finish();
            }
        });

        User user = AppManager.getSession();
        if (user != null) {

            webView = (WebView) findViewById(R.id.webView);
            final String urlString = Const.URL_HEROKU_PAYMENT_VERIFY + "?userId=" + user.getIdx();

            final RelativeLayout layout_status = findViewById(R.id.layout_status);
            Button btnVerify = findViewById(R.id.btn_verify);
            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layout_status.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    loadPaymentWebview(urlString);
                }
            });

            switch (user.getCardStatus()) {
                case 0: // not verified
                {
                    layout_status.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    loadPaymentWebview(urlString);
                }
                    break;
                case 1: // verified
                {
                    layout_status.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
                    break;
                case 2: // expired
                {
                    layout_status.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
                    break;
            }

        }


        /*cardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        ListView listView =  (ListView) findViewById(R.id.listview);

        User session = AppManager.getSession();
        mAdatper = new SimpleAdapter(
                this,
                mCardTokens,
                R.layout.list_item_layout,
                new String[]{"last4", "tokenId"},
                new int[]{R.id.last4, R.id.tokenId});
        listView.setAdapter(mAdatper);
        addToList(session.getPayCard());

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Card cardToSave = cardInputWidget.getCard();
                if (cardToSave == null) {
                    Toast.makeText(PaymentActivity.this, "Invalid Card Data", Toast.LENGTH_SHORT).show();
                    return;
                }

                hud.show();
                new Stripe(PaymentActivity.this).createToken(
                        cardToSave,
                        KEY_STRIPE_KEY,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                Map<String, Object> cardData = new HashMap<>();
                                cardData.put("token", token.getId());
                                cardData.put("last4", cardToSave.getLast4());
                                cardData.put("number", cardToSave.getNumber());
                                cardData.put("month", cardToSave.getExpMonth().toString());
                                cardData.put("year", cardToSave.getExpYear().toString());
                                cardData.put("cvc", cardToSave.getCVC());
                                cardData.put("bankName", token.getBankAccount()!=null?token.getBankAccount().getBankName():"?");
                                final PayCard card = new PayCard(cardData);
                                User session = AppManager.getSession();
                                References.getInstance().usersRef.child(session.getIdx()).child("payCard").setValue(cardData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        addToList(card);
                                        Toast.makeText(PaymentActivity.this, "Card has been saved successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                hud.dismiss();
                            }
                            public void onError(Exception error) {
                                Toast.makeText(PaymentActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                hud.dismiss();
                            }
                        });
            }
        });*/
    }

    void loadPaymentWebview(String url){

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url.replace(" ", "%20"));
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

    void addToList(PayCard card) {
        addToList(card.getLast4(), card.getToken());
    }

    public void addToList(@NonNull String last4, @NonNull String tokenId) {
        mCardTokens.clear();
        String endingIn = getString(R.string.endingIn);
        Map<String, String> map = new HashMap<>();
        map.put("last4", endingIn + " " + last4);
        map.put("tokenId", tokenId);
        mCardTokens.add(map);
        mAdatper.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
