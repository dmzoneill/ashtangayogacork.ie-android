package ie.ayc.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ie.ayc.AycCookieManager;
import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateSource;

public class ClassesFragment extends Fragment implements Observer {

    private Map<String,Button> book_buttons = new HashMap<String,Button>();
    private View root;

    public ClassesFragment(){
        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.classes);
    }

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_classes, container, false);

        int[] headers = new int[4];
        headers[0] = R.id.available_class_credits;
        headers[1] = R.id.bookings;
        headers[2] = R.id.workshops_events;
        headers[3] = R.id.schedule;

        for (int y = 0; y < headers.length; y++) {
            TextView tv = this.root.findViewById(headers[y]);
            String old_text = tv.getText().toString();
            tv.setText(getColoredString(old_text));
        }

        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.classes);

        try {
            ImageView iv = this.root.findViewById(R.id.person);

            iv.setClickable(true);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AycCookieManager.getInstance().clearCookies();
                    Common.alert(getContext(), "Logging Out");
                }
            });
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }

        this.setupRedeemButton();

        return root;
    }

    private void setupRedeemButton(){
        final Button redeem_button = this.root.findViewById(R.id.button_redeem);

        redeem_button.setOnClickListener(new View.OnClickListener() {
            //@SuppressWarnings("deprecation")
            public void onClick(final View view) {

                // Creating alert Dialog with one Button
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ClassesFragment.this.root.getContext());

                // Setting Dialog Title
                alertDialog.setTitle("Redeem Code");

                FrameLayout container = new FrameLayout(getActivity().getApplicationContext());
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                // Setting Dialog Message
                alertDialog.setMessage("Enter code");
                final EditText input = new EditText(ClassesFragment.this.root.getContext());
                input.setLayoutParams(params);
                input.setBackgroundResource(R.drawable.login_input);
                input.setHint("CODE");
                input.setGravity(Gravity.CENTER);
                container.addView(input);
                alertDialog.setView(container);

                // Setting Icon to Dialog
                alertDialog.setIcon(R.drawable.sticker_ganesh);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("Redeem",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                // Write your code here to execute after dialog

                            }
                        });
                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                dialog.cancel();
                            }
                        });

                // Showing Alert Message
                alertDialog.show();
            }
        });
    }

    private void updateClasses() {
        JSONArray stickies = ScraperManager.getClasses(1);
        Log.v("ayc-classes", " classes: " + stickies.length());

        JSONArray classes = ScraperManager.getClasses(0);
        Log.v("ayc-classes", " classes: " + classes.length());

        TableLayout lm = this.root.findViewById(R.id.events_table);
        lm.removeAllViews();
        this.addrows(lm,stickies);

        TableLayout ls = this.root.findViewById(R.id.schedule_table);
        ls.removeAllViews();
        this.addrows(ls,classes);
    }

    private void addrows(TableLayout ll, JSONArray classes){
        try {
            String lastweek = "";
            String lastdate = "";
            Log.v("ayc-classes-table", " lenght: " + classes.length());

            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (int t = 0; t < classes.length(); t++) {
                Log.v("ayc-classes", " row: " + t);

                TableRow classcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_time, null);
                JSONObject classs = classes.getJSONObject(t);

                String date = classs.get("date").toString();
                Log.v("ayc-class-date", date);

                String week = classs.get("week").toString();
                Log.v("ayc-class-week", week);

                if(lastweek.compareTo(week)!=0) {
                    TableRow weekcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_week, null);
                    TextView weektv = (TextView) weekcontainer.getChildAt(0);
                    weektv.setText("Week " + week);
                    ll.addView(weekcontainer);

                    TableRow hrcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_week_red_hr, null);
                    ll.addView(hrcontainer);
                }

                if(lastdate.compareTo(date)!=0) {
                    TableRow datecontainer = (TableRow) vi.inflate(R.layout.classes_table_row_day, null);
                    TextView datetv = (TextView) datecontainer.getChildAt(0);
                    datetv.setText(date);
                    ll.addView(datecontainer);
                }

                TextView tvi = classcontainer.findViewById(R.id.class_instructor);
                tvi.setText(classs.get("instructor").toString());

                TextView tvct = classcontainer.findViewById(R.id.class_time);
                tvct.setText(classs.get("start_time").toString() + " - " + classs.get("end_time").toString());

                TextView tvcn = classcontainer.findViewById(R.id.class_name);
                tvcn.setText(classs.get("name").toString());

                final Button btbk = classcontainer.findViewById(R.id.class_button);
                btbk.setText(classs.get("button_text").toString());
                btbk.setTag(classs.get("class_id").toString());
                this.book_buttons.put(classs.get("class_id").toString(), btbk);

                btbk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClassesFragment.this.book_button_click(btbk);
                    }
                });

                if(classs.get("button_text").toString().compareTo("Cancelled") != 0 && Integer.parseInt(classs.get("max_attendees").toString()) == 0){
                    btbk.setVisibility(View.GONE);
                }

                ll.addView(classcontainer);

                lastweek = week;
                lastdate = date;

                Log.v("ayc-classes", " added row");
            }
        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void book_button_click(Button btn){
        try {
            String class_id = btn.getTag().toString();
            Log.v("ayc-class-book", class_id);
            JSONObject aycclass = ScraperManager.getClassById(class_id);
            TextView tvc = this.root.findViewById(R.id.textview_available_count);
            int quotaavail = Integer.parseInt(tvc.getText().toString());

            if (aycclass.get("disabled").toString().compareTo("true") == 0) {
                return;
            }

            if (quotaavail <= 0 && aycclass.get("free").toString().compareTo("false") == 0) {
                Common.alert(this.getContext(),"You are out of credits, please purchase");
                return;
            }

            if(aycclass.get("cancellation_warning").toString().compareTo("true") == 0){
                //warn can't be cancelled
            }


        }
        catch(Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void book_class(String class_id) {

    }

    private void updateCredits() {
        try {
            JSONArray profile = ScraperManager.getProfile();
            JSONObject obj = profile.getJSONObject(2);
            String credits_available = obj.getString("credits_available");
            int available_monthlys = Integer.parseInt(obj.getString("available_monthlys"));
            JSONObject types = obj.getJSONObject("credit_type");

            LinearLayout ll = this.root.findViewById(R.id.credit_types);
            ll.removeAllViews();

            if(available_monthlys==0) {
                Button button = this.root.findViewById(R.id.button_start_monthly);
                button.setVisibility(View.GONE);
            }

            for (Iterator<String> it = types.keys(); it.hasNext(); ) {
                String str = it.next();
                try {
                    LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout credit_type_row = (LinearLayout) wvi.inflate(R.layout.credits_table_row, null);
                    TextView credit_amount = (TextView) credit_type_row.getChildAt(0);
                    TextView credit_type = (TextView) credit_type_row.getChildAt(1);
                    credit_amount.setText(types.getString(str));
                    credit_type.setText(str);
                    ll.addView(credit_type_row);
                }
                catch(Exception e){

                }
            }

            TextView tv = this.root.findViewById(R.id.textview_available_count);
            tv.setText(credits_available);
        }
        catch(Exception e) {
            Log.v("ayc-classes",e.getMessage());
        }
    }

    private void updateBookings() {
        JSONArray bookings = ScraperManager.getBookings();

        LinearLayout ll = this.root.findViewById(R.id.bookings_table);
        ll.removeAllViews();

        Log.v("ayc-classes", "bookings:" + bookings.length());
        for (int t = 0; t < bookings.length(); t++) {
            try {
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
            catch(Exception e){
                Log.v("ayc-classes", e.getMessage());
            }
        }
    }

    @Override
    public void update(UpdateSource updatesource) {
        Log.v("ayc-classes", "update");

        if(updatesource != UpdateSource.classes) return;

        try {
            Log.v("ayc-classes", "update accepted");
            this.updateClasses();
            this.updateCredits();
            this.updateBookings();
        }
        catch (Exception e){
            Log.v("ayc-classes", e.getMessage());
        }
    }
}
