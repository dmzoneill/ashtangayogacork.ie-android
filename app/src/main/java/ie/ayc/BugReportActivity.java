package ie.ayc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.UnsupportedEncodingException;

public final class BugReportActivity extends AppCompatActivity {
  /** Minimum characters required in a bug title. */
  private static final int MIN_TITLE_LENGTH = 3;

  /** scale. */
  private Animation scale;

  /**
   * getDeviceName.
   *
   * @return result
   */
  public String getDeviceName() {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.startsWith(manufacturer)) {
      return capitalize(model);
    } else {
      return capitalize(manufacturer) + " " + model;
    }
  }

  private String capitalize(final String s) {
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

  /**
   * getAndroidVersion.
   *
   * @return result
   */
  public String getAndroidVersion() {
    String release = Build.VERSION.RELEASE;
    int sdkVersion = Build.VERSION.SDK_INT;
    return "Android SDK: " + sdkVersion + " (" + release + ")";
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bug_report);
    int res8 = R.anim.buttonclick;
    Context context = this.getApplicationContext();
    this.scale = AnimationUtils.loadAnimation(context, res8);

    ActivityChrome.hideActionBar(this);

    TextView tv = findViewById(R.id.bug_report);
    tv.setText(Headings.lead(tv.getText().toString()));

    EditText det = findViewById(R.id.issue_description);
    String bugreportTemplate = "";
    bugreportTemplate += "Model: " + this.getDeviceName() + "\n";
    bugreportTemplate += "System Version: " + this.getAndroidVersion() + "\n\n";
    String steps = "I clicked the buy button and nothing happened happened\n\n";
    bugreportTemplate += "Steps to reproduce:\n" + steps;
    String expected = "I expected the buy view to be presented\n\n";
    bugreportTemplate += "Expected Result:\n" + expected;
    bugreportTemplate += "Actual Result:\nThe view did show\n\n";
    String severity = "I am unable to purchase and book classes\n\n";
    bugreportTemplate += "Severity/Priority:\nHigh severity.  " + severity;
    det.setText(bugreportTemplate);

    final Button cib = findViewById(R.id.issue_button);
    cib.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            cib.startAnimation(BugReportActivity.this.scale);
            Log.v("ayc", "clicked");
            int res20 = R.id.issue_description;
            EditText det = BugReportActivity.this.findViewById(res20);
            int res21 = R.id.issue_title;
            EditText tet = BugReportActivity.this.findViewById(res21);
            if (tet.getText().toString().length() < MIN_TITLE_LENGTH) {
              Common.alert(BugReportActivity.this, "Please enter a title");
              Log.v("ayc", "too short");
              return;
            }
            try {
              String title = tet.getText().toString();
              String detail = det.getText().toString();
              ScraperManager.getInstance().createIssue(title, detail);
            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
            }

            AlertDialog.Builder builder =
                new AlertDialog.Builder(BugReportActivity.this); // xxxxxxx
            builder
                .setMessage("Thank you, we have received your report")
                .setCancelable(false)
                .setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                      public void onClick(
                          final DialogInterface dialog,
                          final int idXXXXXXXXXXXXXX) { // xxxxxxxxxxxx
                        // do things
                      }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            Log.v("ayc", "issue created");
          }
        });
  }
}
