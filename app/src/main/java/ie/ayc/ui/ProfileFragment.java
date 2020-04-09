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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import ie.ayc.AycCookieManager;
import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.DownloadImageTask;
import ie.ayc.Login;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ReceiptActivity;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;

public class ProfileFragment extends Fragment implements Observer {

    private View root;

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            int[] headers = new int[4];
            headers[0] = R.id.my_bookings;
            headers[1] = R.id.expiring_credit;
            headers[2] = R.id.used_credit;
            headers[3] = R.id.transaction_history;

            for (int y = 0; y < headers.length; y++) {
                TextView tv = root.findViewById(headers[y]);
                String old_text = tv.getText().toString();
                tv.setText(getColoredString(old_text));
            }

            ImageButton ime = this.root.findViewById(R.id.button_logout);
            ime.setClickable(true);
            ime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AycCookieManager.getInstance().clearCookies();
                    getActivity().finish();
                }
            });

            ImageButton imp = this.root.findViewById(R.id.button_settings);
            imp.setClickable(true);
            imp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Common.alert(getContext(), "Not available");
                }
            });
        }
        catch(Exception e) {
            Log.v("ayc-profile",e.getMessage());
        }

        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.profile);

        return root;
    }

    private void upateImageView() {
        ImageView profileview = root.findViewById(R.id.person);
        DownloadImageTask dit = new DownloadImageTask(profileview);
        try {
            JSONArray profile = ScraperManager.getProfile();
            JSONObject obj = profile.getJSONObject(0);
            JSONObject ud = obj.getJSONObject("data");
            String img = ud.getString("user_avatar_url");
            Log.v("ayc-profile",img);
            dit.execute(img);
        }
        catch(Exception e) {
            Log.v("ayc-profile",e.getMessage());
        }
    }

    private void updateUsername() {
        try {
            JSONArray profile = ScraperManager.getProfile();
            JSONObject obj = profile.getJSONObject(0);
            JSONObject ud = obj.getJSONObject("data");
            String un = ud.getString("user_login");
            TextView tv = this.root.findViewById(R.id.username);
            tv.setText(un);
        }
        catch(Exception e) {
            Log.v("ayc-profile",e.getMessage());
        }
    }

    private void updateBookings() {
        try {
            JSONArray bookings = ScraperManager.getBookings();
            Log.v("ayc-profile","bookings-length: " + bookings.length());

            TableLayout ll = this.root.findViewById(R.id.profile_bookings_ll);
            ll.removeAllViews();

            LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow booking_row_header = (TableRow) wvi.inflate(R.layout.profile_booking_table_row_header, null);
            ll.addView(booking_row_header);

            Log.v("ayc-profile", "bookings:" + bookings.length());
            for (int t = 0; t < bookings.length(); t++) {
                JSONObject booking = bookings.getJSONObject(t);
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.profile_booking_table_row, null);

                TextView tvdate = (TextView) booking_row.getChildAt(0);
                TextView tvtime = (TextView) booking_row.getChildAt(1);
                TextView tvname = (TextView) booking_row.getChildAt(2);
                TextView tvinstr = (TextView) booking_row.getChildAt(3);

                tvdate.setText(booking.getString("date"));
                tvtime.setText(booking.getString("start_time"));
                tvname.setText(booking.getString("class_name"));
                tvinstr.setText(booking.getString("instructor_name"));

                if (t % 2 > 0) {
                    booking_row.setBackgroundColor(Color.parseColor("#efefef"));
                }

                ll.addView(booking_row);
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void updateExpiringCredit() {
        try {
            JSONArray expired = ScraperManager.getExpiringCredit();
            JSONObject simplified = expired.getJSONObject(1);
            Log.v("ayc-profile","expired-length: " + expired.length());

            TableLayout ll = this.root.findViewById(R.id.profile_expiring_credit_ll);
            ll.removeAllViews();

            LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow used_row_header = (TableRow) wvi.inflate(R.layout.profile_credit_table_row_header, null);
            ll.addView(used_row_header);

            int t = 0;
            for (Iterator<String> it = simplified.keys(); it.hasNext(); ) {
                String key = it.next();
                String[] parts = key.split("#");
                JSONArray details = simplified.getJSONArray(key);

                TableRow expire_row = (TableRow) wvi.inflate(R.layout.profile_credit_table_row, null);

                if (t % 2 > 0) {
                    expire_row.setBackgroundColor(Color.parseColor("#efefef"));
                }

                TextView tvamount = (TextView) expire_row.getChildAt(0);
                TextView tvtrans = (TextView) expire_row.getChildAt(1);
                TextView tvexp = (TextView) expire_row.getChildAt(2);
                TextView tvtype = (TextView) expire_row.getChildAt(3);

                tvamount.setText(details.getString(2) + "x");
                tvtrans.setText(parts[0]);
                tvexp.setText(parts[1]);
                tvtype.setText(details.getString(1));

                if(details.getString(1).compareToIgnoreCase("null")==0) {
                    tvtype.setText("standard");
                }

                ll.addView(expire_row);
                t++;
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void updateUsedCredit() {
        try {
            JSONArray used = ScraperManager.getUsedCredit();
            JSONObject simplified = used.getJSONObject(1);
            Log.v("ayc-profile","expired-length: " + used.length());

            TableLayout ll = this.root.findViewById(R.id.profile_used_credit_ll);
            ll.removeAllViews();

            LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow used_row_header = (TableRow) wvi.inflate(R.layout.profile_credit_table_row_header, null);
            ll.addView(used_row_header);

            int t = 0;
            for (Iterator<String> it = simplified.keys(); it.hasNext(); ) {
                String key = it.next();
                String[] parts = key.split("#");
                JSONArray details = simplified.getJSONArray(key);
                TableRow used_row = (TableRow) wvi.inflate(R.layout.profile_credit_table_row, null);

                if (t % 2 > 0) {
                    used_row.setBackgroundColor(Color.parseColor("#efefef"));
                }

                TextView tvamount = (TextView) used_row.getChildAt(0);
                TextView tvtrans = (TextView) used_row.getChildAt(1);
                TextView tvexp = (TextView) used_row.getChildAt(2);
                TextView tvtype = (TextView) used_row.getChildAt(3);

                tvamount.setText(details.getString(2) + "x");
                tvtrans.setText(parts[0]);
                tvexp.setText(parts[1]);
                tvtype.setText(details.getString(1));

                if(details.getString(1).compareToIgnoreCase("null")==0) {
                    tvtype.setText("standard");
                }

                ll.addView(used_row);
                t++;
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void updateTransactions() {
        try {
            JSONArray transactions = ScraperManager.getTransactions();
            Log.v("ayc-profile","transactions-length: " + transactions.length());

            TableLayout ll = this.root.findViewById(R.id.profile_transactions_ll);
            ll.removeAllViews();

            LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow trans_row_header = (TableRow) wvi.inflate(R.layout.profile_trans_table_row_header, null);
            ll.addView(trans_row_header);

            for(int t=0; t< transactions.length(); t++) {
                JSONObject obj = transactions.getJSONObject(t);
                TableRow trans_row = (TableRow) wvi.inflate(R.layout.profile_trans_table_row, null);
                if (t % 2 > 0) {
                    trans_row.setBackgroundColor(Color.parseColor("#efefef"));
                }

                TextView tvdate = (TextView) trans_row.getChildAt(0);
                TextView tvtransid = (TextView) trans_row.getChildAt(1);
                TextView tvtype = (TextView) trans_row.getChildAt(2);
                ImageView ivreceipt = (ImageView) trans_row.getChildAt(3);
                ImageView ivgift = (ImageView) trans_row.getChildAt(4);

                final String tid = java.net.URLDecoder.decode(obj.getString("id"), "UTF-8");
                String name = java.net.URLDecoder.decode(obj.getString("payer_email"), "UTF-8");
                String first_name = java.net.URLDecoder.decode(obj.getString("first_name"), "UTF-8");
                String last_name = java.net.URLDecoder.decode(obj.getString("last_name"), "UTF-8");
                String purchase_amount = obj.getString("purchase_amount");
                String full_name = first_name + " " + last_name;
                String payee = "";

                if(first_name.compareToIgnoreCase("cash")==0){
                    payee = "Cash";
                }
                else {
                    payee = full_name + "\n" + name;
                }

                tvdate.setText(obj.getString("purchase_date"));
                tvtransid.setText(obj.getString("txn_id") + "\n" + payee);
                tvtype.setText(obj.getString("class_type_restriction"));

                if(Integer.parseInt(obj.getString("used_tokens")) != 0) {
                    ivgift.setVisibility(View.GONE);
                }

                if(obj.getString("class_type_restriction").compareToIgnoreCase("null")==0) {
                    tvtype.setText(purchase_amount + "\nstandard");
                }

                ivreceipt.setClickable(true);
                ivreceipt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        Intent myIntent = new Intent(ProfileFragment.this.getActivity(), ReceiptActivity.class);
                        myIntent.putExtra("transid", tid); //Optional parameters
                        ProfileFragment.this.startActivity(myIntent);
                    }
                });

                ll.addView(trans_row);
            }
        }
        catch(Exception e){
            Log.v("ayc-profile", e.getMessage());
        }
    }

    @Override
    public void update(UpdateSource updatesource) {
        if(updatesource != UpdateSource.profile) return;
        try {
            //this.upateImageView();
            this.updateTransactions();
            this.updateExpiringCredit();
            this.updateUsedCredit();
            this.updateBookings();
            this.updateUsername();
        }
        catch (Exception e){
            Log.v("ayc-classes-update", e.getMessage());
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
