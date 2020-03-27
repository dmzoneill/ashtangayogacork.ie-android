package ie.ayc.ui;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import ie.ayc.DownloadImageTask;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateSource;

public class ProfileFragment extends Fragment implements Observer {

    private View root;

    public ProfileFragment(){
        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.profile);
    }

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

            LinearLayout ll = this.root.findViewById(R.id.profile_bookings_ll);
            ll.removeAllViews();

            Log.v("ayc-profile", "bookings:" + bookings.length());
            for (int t = 0; t < bookings.length(); t++) {
                JSONObject booking = bookings.getJSONObject(t);
                LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout booking_row = (LinearLayout) wvi.inflate(R.layout.booking_table_row, null);

                TextView tvdate = (TextView) booking_row.getChildAt(0);
                TextView tvtime = (TextView) booking_row.getChildAt(1);
                TextView tvname = (TextView) booking_row.getChildAt(2);
                TextView tvinstr = (TextView) booking_row.getChildAt(3);

                tvdate.setText(booking.getString("date"));
                tvtime.setText(booking.getString("start_time"));
                tvname.setText(booking.getString("class_name"));
                tvinstr.setText(booking.getString("instructor_name"));

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

            for (Iterator<String> it = simplified.keys(); it.hasNext(); ) {
                String key = it.next();
                String[] parts = key.split("#");
                JSONArray details = simplified.getJSONArray(key);

                LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TableRow expire_row = (TableRow) wvi.inflate(R.layout.profile_credit_table_row, null);

                TextView tvamount = (TextView) expire_row.getChildAt(0);
                TextView tvpd = (TextView) expire_row.getChildAt(1);
                TextView tvtrans = (TextView) expire_row.getChildAt(2);
                TextView tvexp = (TextView) expire_row.getChildAt(3);
                TextView tvtype = (TextView) expire_row.getChildAt(4);

                tvamount.setText(details.getString(2));
                tvpd.setText(details.getString(0));
                tvtrans.setText(parts[0]);
                tvexp.setText(parts[1]);
                tvtype.setText(details.getString(1));

                ll.addView(expire_row);
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

            for (Iterator<String> it = simplified.keys(); it.hasNext(); ) {
                String key = it.next();
                String[] parts = key.split("#");
                JSONArray details = simplified.getJSONArray(key);

                LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TableRow used_row = (TableRow) wvi.inflate(R.layout.profile_credit_table_row, null);

                TextView tvamount = (TextView) used_row.getChildAt(0);
                TextView tvpd = (TextView) used_row.getChildAt(1);
                TextView tvtrans = (TextView) used_row.getChildAt(2);
                TextView tvexp = (TextView) used_row.getChildAt(3);
                TextView tvtype = (TextView) used_row.getChildAt(4);

                tvamount.setText(details.getString(2));
                tvpd.setText(details.getString(0));
                tvtrans.setText(parts[0]);
                tvexp.setText(parts[1]);
                tvtype.setText(details.getString(1));

                ll.addView(used_row);
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

            for(int t=0; t< transactions.length(); t++) {
                JSONObject obj = transactions.getJSONObject(t);

                LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TableRow trans_row = (TableRow) wvi.inflate(R.layout.profile_trans_table_row, null);

                TextView tvdate = (TextView) trans_row.getChildAt(0);
                TextView tvtransid = (TextView) trans_row.getChildAt(1);
                TextView tvnameemail = (TextView) trans_row.getChildAt(2);
                TextView tvtype = (TextView) trans_row.getChildAt(3);
                TextView tvreceipt = (TextView) trans_row.getChildAt(4);
                TextView tvgift = (TextView) trans_row.getChildAt(5);

                tvdate.setText(obj.getString("purchase_date"));
                tvtransid.setText(obj.getString("txn_id"));
                tvnameemail.setText(obj.getString("payer_email"));
                tvtype.setText(obj.getString("class_type_restriction"));
                tvreceipt.setText("1");
                tvgift.setText("1");

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
}
