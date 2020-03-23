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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.ayc.Observer;
import ie.ayc.PurchaseActivity;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateSource;

public class ClassesFragment extends Fragment implements Observer {

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

        return root;
    }

    private void update_tables(){

        JSONArray stickies = ScraperManager.getClasses(1);
        Log.v("ayc-classes", " classes: " + stickies.length());

        JSONArray classes = ScraperManager.getClasses(0);
        Log.v("ayc-classes", " classes: " + classes.length());

        LinearLayout lm = this.root.findViewById(R.id.events_table);
        lm.removeAllViews();
        this.addrows(lm,stickies);

        LinearLayout ls = this.root.findViewById(R.id.schedule_table);
        ls.removeAllViews();
        this.addrows(ls,classes);
    }

    private void addrows(LinearLayout ll, JSONArray classes){
        try {
            String lastweek = "";
            String lastdate = "";
            Log.v("ayc-classes-table", " lenght: " + classes.length());
            for (int t = 0; t < classes.length(); t++) {
                Log.v("ayc-classes", " row: " + t);
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout classcontainer = (LinearLayout) vi.inflate(R.layout.classes_table_row_time, null);
                JSONObject classs = classes.getJSONObject(t);

                String date = classs.get("date").toString();
                Log.v("ayc-class-date", date);

                String week = classs.get("week").toString();
                Log.v("ayc-class-week", week);

                if(lastweek.compareTo(week)!=0) {
                    LayoutInflater wvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout weekcontainer = (LinearLayout) wvi.inflate(R.layout.classes_table_row_week, null);
                    TextView weektv = (TextView) weekcontainer.getChildAt(0);
                    weektv.setText("Week " + week);
                    ll.addView(weekcontainer);
                }

                if(lastdate.compareTo(date)!=0) {
                    LayoutInflater dvi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout datecontainer = (LinearLayout) dvi.inflate(R.layout.classes_table_row_day, null);
                    TextView datetv = (TextView) datecontainer.getChildAt(0);
                    datetv.setText(date);
                    ll.addView(datecontainer);
                }

                ll.addView(classcontainer);

                lastweek = week;
                lastdate = date;

                Log.v("ayc-classes", " added row");
            }
        }
        catch(Exception e){
            Log.v("ayc-tablerow", e.getMessage());
        }
    }


    @Override
    public void update(UpdateSource updatesource) {
        if(updatesource != UpdateSource.classes) return;
        try {
            this.update_tables();
        }
        catch (Exception e){
            Log.v("ayc-classes-update", e.getMessage());
        }
    }
}
