package ie.ayc;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/** Binds a booking JSON object onto a booking-row layout. */
public final class BookingRows {
  /** SECONDS_PER_MINUTE. */
  private static final int SECONDS_PER_MINUTE = 60;

  /** MILLIS_PER_SECOND. */
  private static final long MILLIS_PER_SECOND = 1000L;

  /** WHEN_PATTERN. */
  private static final String WHEN_PATTERN = "yyyy-MM-dd HH:mm";

  /** MEETING_INDEX. */
  private static final int MEETING_INDEX = 0;

  /** DOOR_INDEX. */
  private static final int DOOR_INDEX = 1;

  /** WHEN_INDEX. */
  private static final int WHEN_INDEX = 2;

  /** NAME_INDEX. */
  private static final int NAME_INDEX = 3;

  /** DATE_INDEX. */
  private static final int DATE_INDEX = 0;

  /** TIME_INDEX. */
  private static final int TIME_INDEX = 1;

  private BookingRows() {
    // Utility class.
  }

  /**
   * Fills the row and wires the meeting and door buttons.
   *
   * @param host fragment that owns the animation and navigation
   * @param bookingRow inflated booking row
   * @param booking booking record
   * @param scale click animation
   * @param logTag log tag for the door window
   * @throws JSONException when a required field is missing
   * @throws ParseException when the class time cannot be parsed
   */
  public static void populate(
      final Fragment host,
      final View bookingRow,
      final JSONObject booking,
      final Animation scale,
      final String logTag)
      throws JSONException, ParseException {
    final View when = (View) bookingRow.getChildAt(WHEN_INDEX);
    final TextView dateView =
        (TextView) ((android.view.ViewGroup) when).getChildAt(DATE_INDEX); // x
    final TextView timeView =
        (TextView) ((android.view.ViewGroup) when).getChildAt(TIME_INDEX); // x
    final TextView nameView = (TextView) bookingRow.getChildAt(NAME_INDEX);
    final ImageButton meetingButton =
        (ImageButton) bookingRow.getChildAt(MEETING_INDEX); // xxxxxxxx
    final ImageButton doorButton =
        (ImageButton) bookingRow.getChildAt(DOOR_INDEX); // xxxxxxxxxxxxxx

    dateView.setText(booking.getString("date"));
    timeView.setText(booking.getString("start_time"));
    nameView.setText(booking.getString("class_name"));

    final String meetingUrl = booking.getString("murl");
    if (meetingUrl.compareTo("") == 0) {
      meetingButton.setVisibility(View.INVISIBLE);
    }
    showDoorWhenArmed(doorButton, booking, logTag);

    meetingButton.setOnClickListener(
        view -> {
          meetingButton.startAnimation(scale);
          final Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(meetingUrl));
          host.startActivity(intent);
        });
    doorButton.setOnClickListener(
        view -> {
          doorButton.startAnimation(scale);
          try {
            ScraperManager.getInstance()
                .openDoor(booking.getString("class_id")); // xxxxxxxxxxxxxxxx
          } catch (JSONException ignored) {
            Log.v(logTag, "door click failed");
          }
        });
  }

  private static void showDoorWhenArmed(
      final ImageButton doorButton,
      final JSONObject booking,
      final String logTag) // xxxxxxxxxxxxxxx
      throws JSONException, ParseException {
    final String classDate = booking.getString("date");
    final String classTime = booking.getString("start_time");
    String whenText = classDate + " " + classTime;
    final Date starts = new SimpleDateFormat(WHEN_PATTERN).parse(whenText);
    final Date now = new Date();
    final int armedBefore =
        Integer.parseInt(booking.getString("doorArmedBeforeMins"))
            * SECONDS_PER_MINUTE; // xxxxxxxxx
    final int disarmedAfter =
        Integer.parseInt(booking.getString("doorDisarmedAfterMins"))
            * SECONDS_PER_MINUTE; // xxxxxxx
    int started = (int) (starts.getTime() / MILLIS_PER_SECOND);
    final int start = started - armedBefore;
    final int stop = started + disarmedAfter;
    final int current = (int) (now.getTime() / MILLIS_PER_SECOND);
    Log.v(logTag, " -- ");
    Log.v(logTag, "class date:" + starts.toString());
    Log.v(logTag, "now date:" + now.toString());
    Log.v(logTag, "cdate: " + classDate);
    Log.v(logTag, "ctime: " + classTime);
    Log.v(logTag, "start: " + start);
    Log.v(logTag, "stop: " + stop);
    Log.v(logTag, "now: " + current);
    Log.v(logTag, "doorArmedBeforeSeconds: " + armedBefore);
    Log.v(logTag, "doorDisarmedAfterSeconds: " + disarmedAfter);
    if (current > start && current < stop) {
      doorButton.setVisibility(View.VISIBLE);
    } else {
      doorButton.setVisibility(View.INVISIBLE);
    }
  }
}
