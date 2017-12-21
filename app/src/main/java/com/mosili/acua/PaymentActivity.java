package com.mosili.acua;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mosili.acua.classes.AppManager;
import com.mosili.acua.models.PayCard;
import com.mosili.acua.models.User;
import com.mosili.acua.utils.References;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mosili.acua.utils.Const.KEY_STRIPE_KEY;

public class PaymentActivity extends AppCompatActivity {

    private KProgressHUD hud;
    private CardInputWidget cardInputWidget;

    private SimpleAdapter mAdatper;
    private List<Map<String, String>> mCardTokens = new ArrayList<Map<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this,R.color.colorTransparency))
                .setDimAmount(0.5f);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Add a payment");
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentActivity.this.finish();
            }
        });

        cardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
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
                                cardData.put("endAt", cardToSave.getExpMonth() + "/"+ cardToSave.getExpYear());
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
