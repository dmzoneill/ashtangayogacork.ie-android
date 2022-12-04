package ie.ayc.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;

public class ClassesFragment extends Fragment implements Observer {

    private Animation scale;
    private Map<String, Button> book_buttons = new HashMap<String, Button>();
    private View root;

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_classes, container, false);
        this.scale = AnimationUtils.loadAnimation(this.getContext(), R.anim.buttonclick);

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

        final Button redeem_code_button = this.root.findViewById(R.id.button_redeem);

        //@SuppressWarnings("deprecation")
        redeem_code_button.setOnClickListener(view -> {
            redeem_code_button.startAnimation(ClassesFragment.this.scale);
            ClassesFragment.this.showRedeemDialog("");
        });

        final Button start_monthly_button = this.root.findViewById(R.id.button_start_monthly);

        //@SuppressWarnings("deprecation")
        start_monthly_button.setOnClickListener(view -> {
            start_monthly_button.startAnimation(ClassesFragment.this.scale);
            ClassesFragment.this.showStartMonthlyDialog();

            Bundle params = new Bundle();
            params.putString("start", "now");
            AycNavigationActivity.mFirebaseAnalytics.logEvent("monthly", params);
        });

        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.classes);

        AycNavigationActivity.mFirebaseAnalytics.logEvent("classes", null);

        SwipeRefreshLayout swipeRefreshLayout = this.root.findViewById(R.id.refreshLayoutClasses);

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    sm.fetch_all();
                    swipeRefreshLayout.setRefreshing(false);
                    sm.fetch_all();
                }
        );

        return root;
    }

    private void showRedeemDialog(String error) {
        // Creating alert Dialog with one Button
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ClassesFragment.this.root.getContext());

        // Setting Dialog Title
        alertDialog.setTitle("Redeem Code");

        FrameLayout container = new FrameLayout(getActivity().getApplicationContext());
        LinearLayout ll = new LinearLayout(getActivity().getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        // Setting Dialog Message
        alertDialog.setMessage("Enter code");
        final EditText input = new EditText(ClassesFragment.this.root.getContext());
        input.setLayoutParams(params);
        input.setBackgroundResource(R.drawable.login_input);
        input.setHint("CODE");
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(input);

        if (error.compareTo("") != 0) {
            TextView tv = new TextView(ClassesFragment.this.root.getContext());
            tv.setText(error);
            tv.setTextColor(Color.RED);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams pms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pms.setMargins(10, 10, 10, 10);
            tv.setLayoutParams(pms);
            ll.addView(tv);
        }

        container.addView(ll);
        alertDialog.setView(container);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.sticker_ganesh);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Redeem",
                (dialog, which) -> {
                    ScraperManager sm = ScraperManager.getInstance();
                    sm.apply_redeem_code(input.getText().toString());
                    dialog.dismiss();
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                (dialog, which) -> dialog.dismiss());

        // Showing Alert Message
        alertDialog.show();
    }

    private void showStartMonthlyDialog() {
        // Creating alert Dialog with one Button
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ClassesFragment.this.root.getContext());

        // Setting Dialog Title
        alertDialog.setTitle("Start Monthly Warning");

        FrameLayout container = new FrameLayout(getActivity().getApplicationContext());
        LinearLayout ll = new LinearLayout(getActivity().getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        String[] msgs = {
                "You are about to start your monthly",
                "Please check your expiring credit prior to starting your monthly in case you have available credits expiring within the month.",
                "Are you sure you want to start your monthly?"
        };

        for (String msg : msgs) {
            TextView tv = new TextView(ClassesFragment.this.root.getContext());
            tv.setText(msg);
            //tv.setTextColor(Color.RED);
            //tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams pms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pms.setMargins(10, 10, 10, 10);
            tv.setLayoutParams(pms);
            ll.addView(tv);
        }

        container.addView(ll);
        alertDialog.setView(container);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.sticker_ganesh);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Start",
                (dialog, which) -> {
                    ScraperManager sm = ScraperManager.getInstance();
                    sm.begin_monthly();
                    dialog.dismiss();
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                (dialog, which) -> dialog.dismiss());

        // Showing Alert Message
        alertDialog.show();
    }

    private void updateClasses() {
        JSONArray stickies = ScraperManager.getClasses(1);
        Log.v("ayc-classes", " classes: " + stickies.length());

        JSONArray classes = ScraperManager.getClasses(0);
        Log.v("ayc-classes", " classes: " + classes.length());

        TableLayout lm = this.root.findViewById(R.id.events_table);
        lm.removeAllViews();
        this.addrows(lm, stickies);

        TableLayout ls = this.root.findViewById(R.id.schedule_table);
        ls.removeAllViews();
        this.addrows(ls, classes);
    }

    private void addrows(TableLayout ll, JSONArray classes) {
        try {
            String lastweek = "";
            String lastdate = "";
            Log.v("ayc-classes-table", " lenght: " + classes.length());

            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (int t = 0; t < classes.length(); t++) {
                Log.v("ayc-classes", " row: " + t);

                TableRow classcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_time, null);
                final JSONObject classs = classes.getJSONObject(t);

                String date = classs.get("date").toString();
                Log.v("ayc-class-date", date);

                String week = classs.get("week").toString();
                Log.v("ayc-class-week", week);

                if (lastweek.compareTo(week) != 0) {
                    TableRow weekcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_week, null);
                    TextView weektv = (TextView) weekcontainer.getChildAt(0);
                    weektv.setText("Week " + week);
                    ll.addView(weekcontainer);

                    TableRow hrcontainer = (TableRow) vi.inflate(R.layout.classes_table_row_week_red_hr, null);
                    ll.addView(hrcontainer);
                }

                if (lastdate.compareTo(date) != 0) {
                    TableRow datecontainer = (TableRow) vi.inflate(R.layout.classes_table_row_day, null);
                    TextView datetv = (TextView) datecontainer.getChildAt(0);
                    datetv.setText(date);
                    ll.addView(datecontainer);
                }

                TextView tvi = classcontainer.findViewById(R.id.class_instructor);
                tvi.setText(classs.get("instructor").toString());

                TextView tvct = classcontainer.findViewById(R.id.class_time);
                tvct.setGravity(Gravity.CENTER);
                tvct.setText(classs.get("start_time").toString() + "\n" + classs.get("end_time").toString());

                TextView tvcn = classcontainer.findViewById(R.id.class_name);
                tvcn.setText(classs.get("name").toString());

                final Button btbk = classcontainer.findViewById(R.id.class_button);
                btbk.setText(classs.get("button_text").toString());
                btbk.setTag(classs.get("class_id").toString());
                this.book_buttons.put(classs.get("class_id").toString(), btbk);

                if (classs.get("book_button_action").toString().compareTo("ButtonCancel") != 0) {
                    btbk.setOnClickListener(v -> {
                        btbk.startAnimation(ClassesFragment.this.scale);
                        Log.v("ayc-class-book", "clicked");
                        ClassesFragment.this.book_button_click(btbk);
                    });
                } else {
                    btbk.setOnClickListener(v -> {
                        btbk.startAnimation(ClassesFragment.this.scale);
                        Log.v("ayc-class-book", "clicked");
                        ClassesFragment.this.cancel_class(btbk.getTag().toString());
                    });
                }

                if (classs.get("button_text").toString().compareTo("Cancelled") != 0 && Integer.parseInt(classs.get("max_attendees").toString()) == 0) {
                    btbk.setVisibility(View.INVISIBLE);
                }

                ll.addView(classcontainer);

                lastweek = week;
                lastdate = date;

                Log.v("ayc-classes", " added row");
            }
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void book_button_click(Button btn) {
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
                Common.alert(this.getContext(), "You are out of credits, please purchase");
                return;
            }

            if (aycclass.get("cancellation_warning").toString().compareTo("true") == 0) {
                this.book_button_confirm("Confirm booking", "\nThis booking cannot be cancelled", class_id);
                return;
            }

            this.book_class(class_id);
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }

    private void book_button_confirm(String title, String message, final String class_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.sticker_ganesh);

        builder.setPositiveButton("Book", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClassesFragment.this.book_class(class_id);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void book_class(String class_id) {
        Log.v("ayc-classes", class_id);

        ScraperManager sm = ScraperManager.getInstance();
        sm.book_class_add(class_id);

        Bundle params = new Bundle();
        params.putString("booking_confirmed", class_id);
        AycNavigationActivity.mFirebaseAnalytics.logEvent("class", params);
    }

    private void cancel_class(String class_id) {
        Log.v("ayc-classes", class_id);

        ScraperManager sm = ScraperManager.getInstance();
        sm.book_class_remove(class_id);

        Bundle params = new Bundle();
        params.putString("booking_cancel", class_id);
        AycNavigationActivity.mFirebaseAnalytics.logEvent("class", params);
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

            if (available_monthlys == 0) {
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
                    switch(str) {
                        case "availableStandardCredits":
                            credit_type.setText("standard credits");
                            break;
                        case "availableMonthliesCredits":
                            credit_type.setText("monthlies");
                            break;
                        default:
                            credit_type.setText(str);
                            break;
                    }
                    ll.addView(credit_type_row);
                } catch (Exception e) {

                }
            }

            TextView tv = this.root.findViewById(R.id.textview_available_count);
            tv.setText(credits_available);
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
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

                LinearLayout datetime = (LinearLayout) booking_row.getChildAt(2);
                TextView tvdate = (TextView) datetime.getChildAt(0);
                TextView tvtime = (TextView) datetime.getChildAt(1);
                TextView tvname = (TextView) booking_row.getChildAt(3);

                final ImageButton meeting_button = (ImageButton) booking_row.getChildAt(0);
                final ImageButton door_button = (ImageButton) booking_row.getChildAt(1);

                tvdate.setText(booking.getString("date"));
                tvtime.setText(booking.getString("start_time"));
                tvname.setText(booking.getString("class_name"));

                final String murl = booking.getString("murl");

                if(murl.compareTo("") == 0) {
                    meeting_button.setVisibility(View.INVISIBLE);
                }

                String cdate = booking.getString("date");
                String ctime = booking.getString("start_time");
                Date classDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(cdate + " " + ctime);
                Date nowDate = new Date();

                int doorArmedBeforeSeconds = Integer.parseInt(booking.getString("doorArmedBeforeMins")) * 60;
                int doorDisarmedAfterSeconds = Integer.parseInt(booking.getString("doorDisarmedAfterMins")) * 60;
                int start = (int) (classDate.getTime() / 1000) - doorArmedBeforeSeconds;
                int stop = (int) (classDate.getTime() / 1000) + doorDisarmedAfterSeconds;
                int now = (int) (nowDate.getTime() / 1000);

                Log.v("ayc-profile"," -- ");
                Log.v("ayc-profile","class date:" + classDate.toString());
                Log.v("ayc-profile","now date:" + nowDate.toString());
                Log.v("ayc-profile","cdate: " + cdate);
                Log.v("ayc-profile","ctime: " + ctime);
                Log.v("ayc-profile","start: " + start);
                Log.v("ayc-profile","stop: " + stop);
                Log.v("ayc-profile","now: " + now);
                Log.v("ayc-profile","doorArmedBeforeSeconds: " + doorArmedBeforeSeconds);
                Log.v("ayc-profile","doorDisarmedAfterSeconds: " + doorDisarmedAfterSeconds);

                if(now > start && now < stop) {
                    door_button.setVisibility(View.VISIBLE);
                } else {
                    door_button.setVisibility(View.INVISIBLE);
                }

                meeting_button.setOnClickListener(v -> {
                    meeting_button.startAnimation(ClassesFragment.this.scale);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(murl));
                    startActivity(i);
                });

                door_button.setOnClickListener(v -> {
                    door_button.startAnimation(ClassesFragment.this.scale);
                    try {
                        ScraperManager.getInstance().open_door(booking.getString("class_id"));
                    } catch(Exception e)
                    {

                    }
                });

                ll.addView(booking_row);
            } catch (Exception e) {
                Log.v("ayc-classes", e.getMessage());
            }
        }

        if( bookings.length() == 0) {
            LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow booking_row = (TableRow) wvi.inflate(R.layout.empty_msg_table_row, null);
            TextView empty = (TextView) booking_row.getChildAt(0);
            empty.setText("You have 0 bookings");
            ll.addView(booking_row);
        }
    }

    @Override
    public void update(UpdateSource updatesource) {
        Log.v("ayc-classes", "update");

        try {
            if (updatesource != UpdateSource.classes) return;

            Log.v("ayc-classes", "update accepted");
            this.updateClasses();
            this.updateCredits();
            this.updateBookings();
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }

    @Override
    public void update(UpdateSource updatesource, UpdateResponse ur) {
        Log.v("ayc-classes", "update response");

        try {
            if (updatesource != UpdateSource.classes) return;

            String action = ur.response.getString("action");
            String error = ur.response.getString("error");
            String result = ur.response.getString("result");

            Log.v("ayc-classes", "action: " + action);
            Log.v("ayc-classes", "error: " + error);
            Log.v("ayc-classes", "result: " + result);

            ScraperManager sm = ScraperManager.getInstance();

            switch (action) {
                case "redeem_code":
                    if (error.compareTo("") != 0) {
                        this.showRedeemDialog(error);
                    } else {
                        sm.fetch_all();
                    }
                    break;
                case "begin_monthly":
                default:
                    if (error.compareTo("") != 0) {
                        Common.alert(getActivity().getApplicationContext(), ur.response.getString("error"));
                    } else {
                        sm.fetch_all();
                    }
                    break;
            }
        } catch (Exception e) {
            Log.v("ayc-classes", e.getMessage());
        }
    }
}
