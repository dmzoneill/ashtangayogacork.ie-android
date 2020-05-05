package ie.ayc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ie.ayc.ui.ClassesFragment;

public class BugReportActivity extends AppCompatActivity {
    private Animation scale;

    public static Spannable getColoredString(String mString) {
        Spannable spannable = new SpannableString(mString);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1, 0); // set size
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report);
        this.scale = AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.buttonclick);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        TextView tv = findViewById(R.id.bug_report);
        String old_text = tv.getText().toString();
        tv.setText(getColoredString(old_text));

        EditText det = findViewById(R.id.issue_description);
        String bugreportTemplate = "";
        bugreportTemplate += "Model: " + this.getDeviceName() + "\n";
        bugreportTemplate += "System Version: " + this.getAndroidVersion() + "\n\n";
        bugreportTemplate += "Steps to reproduce:\nI clicked the buy button and nothing happened happened\n\n";
        bugreportTemplate += "Expected Result:\nI expected the buy view to be presented\n\n";
        bugreportTemplate += "Actual Result:\nThe view did show\n\n";
        bugreportTemplate += "Severity/Priority:\nHigh severity.  I am unable to purchase and book classes\n\n";
        det.setText(bugreportTemplate);

        final Button cib = findViewById(R.id.issue_button);
        cib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cib.startAnimation(BugReportActivity.this.scale);
                Log.v("ayc", "clicked");
                EditText det = BugReportActivity.this.findViewById(R.id.issue_description);
                EditText tet = BugReportActivity.this.findViewById(R.id.issue_title);
                if( tet.getText().toString().length() < 3) {
                    Common.alert(BugReportActivity.this,"Please enter a title");
                    Log.v("ayc", "too short");
                    return;
                }
                ScraperManager.getInstance().create_issue(tet.getText().toString(), det.getText().toString());

                AlertDialog.Builder builder = new AlertDialog.Builder(BugReportActivity.this);
                builder.setMessage("Thank you, we have received your report")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                Log.v("ayc", "issue created");
            }
        });
    }
}
