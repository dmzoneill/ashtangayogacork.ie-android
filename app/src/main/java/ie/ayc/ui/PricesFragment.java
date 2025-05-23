package ie.ayc.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.Observer;
import ie.ayc.PurchaseActivity;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;

public class PricesFragment extends Fragment implements Observer {

    private Animation scale;
    private View root;

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_prices, container, false);
        this.scale = AnimationUtils.loadAnimation(this.getContext(), R.anim.buttonclick);

        int[] headers = new int[5];
        headers[0] = R.id.yoga_for_everyone;
        headers[1] = R.id.gifts;
        headers[2] = R.id.monthlies;
        headers[3] = R.id.specials;
        headers[4] = R.id.standard;

        for (int y = 0; y < headers.length; y++) {
            TextView tv = root.findViewById(headers[y]);
            String old_text = tv.getText().toString();
            tv.setText(getColoredString(old_text));
        }

        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.prices);

        AycNavigationActivity.mFirebaseAnalytics.logEvent("prices", null);

        return root;
    }


    private void update_tables(){
        ScraperManager sm = ScraperManager.getInstance();

        JSONArray prices_monthly = ScraperManager.getPrices(0);
        Log.v("ayc-prices-fragment", " monthlies: " + prices_monthly.length());

        JSONArray prices_special = ScraperManager.getPrices(1);
        Log.v("ayc-prices-fragment", " specials: " + prices_special.length());

        JSONArray prices_standard = ScraperManager.getPrices(2);
        Log.v("ayc-prices-fragment", " standard: " + prices_standard.length());

        LinearLayout lm = this.root.findViewById(R.id.monthlies_table);
        lm.removeAllViews();
        this.addrows(lm,prices_monthly);

        LinearLayout ls = this.root.findViewById(R.id.specials_table);
        ls.removeAllViews();
        this.addrows(ls,prices_special);

        LinearLayout lc = this.root.findViewById(R.id.standard_table);
        lc.removeAllViews();
        this.addrows(lc,prices_standard);

    }


    private void addrows(LinearLayout ll, JSONArray prices){
        try {
            Log.v("ayc-prices-table", " lenght: " + prices.length());
            for (int t = 0; t < prices.length(); t++) {
                Log.v("ayc-prices", " row: " + t);
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout rowcontainer = (LinearLayout) vi.inflate(R.layout.prices_table_row, null);
                final JSONObject price = prices.getJSONObject(t);
                final String name = price.get("name").toString();
                Log.v("ayc-prices", " name: " + name);
                String euros = price.get("price").toString();
                Log.v("ayc-prices", " euros: " + euros);
                LinearLayout row = (LinearLayout) rowcontainer.getChildAt(0);
                TextView tvname = (TextView) row.getChildAt(0);
                tvname.setText(name);

                String credits = price.get("credits").toString();
                Log.v("ayc-prices", " credits: " + credits);
                String days = price.get("credit_expiry").toString().substring(1).trim();
                Log.v("ayc-prices", " days: " + days);

                TextView tvvalidity = (TextView) row.getChildAt(2);
                tvvalidity.setText(String.format("You will receive %s credit(s) and it's valid for %s days.", credits, days));

                TextView tvprice = (TextView) rowcontainer.getChildAt(1);
                tvprice.setText("€" + euros);

                final String endpoint = price.get("endpoint").toString();
                final String params = price.get("params").toString();

                final ImageButton buybutton = (ImageButton) rowcontainer.getChildAt(2);
                buybutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buybutton.startAnimation(PricesFragment.this.scale);
                        Intent myIntent = new Intent(getActivity(), PurchaseActivity.class);
                        myIntent.putExtra("endpoint", new String[] { endpoint, params }); //Optional parameters
                        PricesFragment.this.startActivity(myIntent);

                        Bundle params = new Bundle();
                        params.putString("class_type", name);
                        AycNavigationActivity.mFirebaseAnalytics.logEvent("buy", params);
                    }
                });

                ll.addView(rowcontainer);
                Log.v("ayc-prices", " added row");
            }
            if(prices.length() == 0) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TableRow booking_row = (TableRow) vi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("Sorry, there are none available at this time, contact us for your information");
                ll.addView(booking_row);
            }
        }
        catch(Exception e){
            Log.v("ayc-tablerow", e.getMessage());
        }
    }


    @Override
    public void update(UpdateSource updatesource) {
        try {
            if(updatesource != UpdateSource.prices) return;
            this.update_tables();
        }
        catch (Exception e){
            Log.v("ayc-prices-update", e.getMessage());
        }
    }

    @Override
    public void update(UpdateSource updatesource, UpdateResponse ur) {
        Log.v("ayc-classes", "update response");

        try {
            if (updatesource != UpdateSource.classes) return;

            if(ur.response.has("success")) {
                Common.alert(this.getContext(), "class booked");
            }
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }
}
