package ie.ayc.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import ie.ayc.AycCookieManager;
import ie.ayc.Common;
import ie.ayc.DownloadImageTask;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ReceiptActivity;
import ie.ayc.ScraperManager;
import ie.ayc.SettingsActivity;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;
import ie.ayc.VoucherActivity;

public class ProfileFragment extends Fragment implements Observer {

    private Animation scale;
    private View root;

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_profile, container, false);
        this.scale = AnimationUtils.loadAnimation(this.getContext(), R.anim.buttonclick);

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

            final ImageButton ime = this.root.findViewById(R.id.button_logout);
            ime.setClickable(true);
            ime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ime.startAnimation(ProfileFragment.this.scale);
                    AycCookieManager.getInstance().clearCookies();
                    getActivity().finish();
                }
            });

            final ImageButton imp = this.root.findViewById(R.id.button_settings);
            imp.setClickable(true);
            imp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imp.startAnimation(ProfileFragment.this.scale);
                    Intent myIntent = new Intent(ProfileFragment.this.getActivity(), SettingsActivity.class);
                    //myIntent.putExtra("url", result); //Optional parameters
                    ProfileFragment.this.startActivity(myIntent);
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

    private void updateImageView() {
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
            //TableRow booking_row_header = (TableRow) wvi.inflate(R.layout.profile_booking_table_row_header, null);
            //ll.addView(booking_row_header);

            Log.v("ayc-profile", "bookings:" + bookings.length());
            for (int t = 0; t < bookings.length(); t++) {
                JSONObject booking = bookings.getJSONObject(t);
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.profile_booking_table_row, null);

                TextView tvdate = (TextView) booking_row.getChildAt(0);
                TextView tvtime = (TextView) booking_row.getChildAt(1);
                TextView tvname = (TextView) booking_row.getChildAt(2);
                final ImageButton meeting_button = (ImageButton) booking_row.getChildAt(3);

                tvdate.setText(booking.getString("date"));
                tvtime.setText(booking.getString("start_time"));
                tvname.setText(booking.getString("class_name"));

                final String murl = booking.getString("murl");

                if(murl.compareTo("") == 0) {
                    meeting_button.setVisibility(View.INVISIBLE);
                }

                meeting_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        meeting_button.startAnimation(ProfileFragment.this.scale);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(murl));
                        startActivity(i);
                    }
                });

                ll.addView(booking_row);
            }

            if( bookings.length() == 0) {
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("You have 0 bookings");
                ll.addView(booking_row);
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void updateExpiringCredit() {
        JSONObject simplified = null;
        JSONArray expired = null;
        TableLayout ll = null;
        LayoutInflater wvi = null;

        try {
            wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ll = this.root.findViewById(R.id.profile_expiring_credit_ll);
            ll.removeAllViews();
            expired = ScraperManager.getExpiringCredit();
            simplified = expired.getJSONObject(1);
        } catch (Exception e){
            if(simplified == null) {
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("You have no expiring credit");
                ll.addView(booking_row);
                return;
            }
        }


        try {
            Log.v("ayc-profile","expired-length: " + expired.length());

            //TableRow used_row_header = (TableRow) wvi.inflate(R.layout.profile_credit_table_row_header, null);
            //ll.addView(used_row_header);

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

            if(simplified.keys().hasNext() == false) {
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("You have no expiring credit");
                ll.addView(booking_row);
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void showVoucherDialog(final String transid) {
        // Creating alert Dialog with one Button
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileFragment.this.root.getContext());

        // Setting Dialog Title
        alertDialog.setTitle("Create gift voucher");

        FrameLayout container = new FrameLayout(getActivity().getApplicationContext());
        LinearLayout ll = new LinearLayout(getActivity().getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        // Setting Dialog Message
        alertDialog.setMessage("Enter recipient name:");
        final EditText input = new EditText(ProfileFragment.this.root.getContext());
        input.setLayoutParams(params);
        input.setBackgroundResource(R.drawable.login_input);
        input.setHint("John Doe");
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(input);

        container.addView(ll);
        alertDialog.setView(container);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.sticker_ganesh);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Gift",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ScraperManager sm = ScraperManager.getInstance();
                        sm.get_voucher_url(input.getText().toString(), transid);
                        dialog.dismiss();
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    private void updateUsedCredit() {
        JSONObject simplified = null;
        JSONArray used = null;
        TableLayout ll = null;
        LayoutInflater wvi = null;

        try {
            wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ll = this.root.findViewById(R.id.profile_used_credit_ll);
            ll.removeAllViews();
            used = ScraperManager.getUsedCredit();
            simplified = used.getJSONObject(1);
        } catch (Exception e){
            if(simplified == null) {
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("You have no used credit");
                ll.addView(booking_row);
                return;
            }
        }

        try {
            Log.v("ayc-profile","used-length: " + used.length());
            //TableRow used_row_header = (TableRow) wvi.inflate(R.layout.profile_credit_table_row_header, null);
            //ll.addView(used_row_header);

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
            //TableRow trans_row_header = (TableRow) wvi.inflate(R.layout.profile_trans_table_row_header, null);
            //ll.addView(trans_row_header);

            for(int t=0; t< transactions.length(); t++) {
                JSONObject obj = transactions.getJSONObject(t);
                TableRow trans_row = (TableRow) wvi.inflate(R.layout.profile_trans_table_row, null);
                if (t % 2 > 0) {
                    trans_row.setBackgroundColor(Color.parseColor("#efefef"));
                }

                TextView tvdate = (TextView) trans_row.getChildAt(0);
                TextView tvtransid = (TextView) trans_row.getChildAt(1);
                TextView tvtype = (TextView) trans_row.getChildAt(2);
                final ImageView ivreceipt = (ImageView) trans_row.getChildAt(3);
                final ImageView ivgift = (ImageView) trans_row.getChildAt(4);

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
                    ivgift.setVisibility(View.INVISIBLE);
                }
                else {
                    ivgift.setClickable(true);
                    ivgift.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ivgift.startAnimation(ProfileFragment.this.scale);
                            ProfileFragment.this.showVoucherDialog(tid);
                        }
                    });
                }
                if(obj.getString("class_type_restriction").compareToIgnoreCase("null")==0) {
                    tvtype.setText(purchase_amount + "\nstandard");
                }

                ivreceipt.setClickable(true);
                ivreceipt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        ivreceipt.startAnimation(ProfileFragment.this.scale);
                        Common.alert(getContext(), "Loading receipt...");
                        v.startAnimation(AnimationUtils.loadAnimation(ProfileFragment.this.getContext(), R.anim.image_click));
                        Intent myIntent = new Intent(ProfileFragment.this.getActivity(), ReceiptActivity.class);
                        myIntent.putExtra("transid", tid); //Optional parameters
                        ProfileFragment.this.startActivity(myIntent);
                    }
                });

                ll.addView(trans_row);
            }

            if(transactions.length() == 0) {
                TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
                TextView empty = (TextView) booking_row.getChildAt(0);
                empty.setText("You have no transactions");
                ll.addView(booking_row);
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
            this.updateImageView();
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
        Log.v("ayc-profile", "update response");

        try {
            if (updatesource != UpdateSource.profile) return;

            String action = ur.response.getString("action");
            String error = ur.response.getString("error");
            String result = ur.response.getString("result");

            Log.v("ayc-profile", "action: " + action);
            Log.v("ayc-profile", "error: " + error);
            Log.v("ayc-profile", "result: " + result);

            ScraperManager sm = ScraperManager.getInstance();

            switch (action) {
                case "get_voucher":
                    if (error.compareTo("") != 0) {
                        Common.alert(getContext(), error);
                    } else {
                        Intent myIntent = new Intent(ProfileFragment.this.getActivity(), VoucherActivity.class);
                        myIntent.putExtra("url", result); //Optional parameters
                        ProfileFragment.this.startActivity(myIntent);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }
}
