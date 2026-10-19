package ie.ayc;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.jediburrell.customfab.FloatingActionButton;

/** Hides the action bar and wires the shared bug-report button. */
public final class ActivityChrome {
  private ActivityChrome() {
    // Utility class.
  }

  /**
   * Hides the support action bar when the theme provides one.
   *
   * @param activity host activity
   */
  public static void hideActionBar(final AppCompatActivity activity) {
    try {
      final ActionBar bar = activity.getSupportActionBar();
      if (bar != null) {
        bar.hide();
      }
    } catch (NullPointerException ignored) {
      Log.v("ayc", "no action bar");
    }
  }

  /**
   * Opens the bug report screen from the mini floating button.
   *
   * @param activity host activity
   */
  public static void attachBugReportButton(final AppCompatActivity activity) {
    int res1 = R.id.floating_action_button;
    final FloatingActionButton fab = activity.findViewById(res1);
    fab.setFabSize(FloatingActionButton.FAB_SIZE_MINI);
    fab.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View view) {
            Log.v("ayc", "ayc fab");
            Context context = activity.getApplicationContext();
            final Intent intent = new Intent(context, BugReportActivity.class);
            activity.startActivity(intent);
          }
        });
  }
}
