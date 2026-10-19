package ie.ayc.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ClassesFragment extends Fragment implements Observer {
  /** Pixel padding inside prompt dialogs. */
  private static final int DIALOG_PAD = 10;

  /** Lines in the monthly warning. */
  private static final int WARNING_LINES = 3;

  /** scale. */
  private Animation scale;

  /** bookButtons. */
  private Map<String, Button> bookButtons = new HashMap<String, Button>();

  /** root. */
  private View root;

  /**
   * onCreateView.
   *
   * @param inflater value
   * @param container value
   * @param savedInstanceState value
   * @return result
   */
  public View onCreateView(
      @NonNull final LayoutInflater inflater,
      final ViewGroup container,
      final Bundle savedInstanceState) {
    this.root = inflater.inflate(R.layout.fragment_classes, container, false);
    int res69 = R.anim.buttonclick;
    this.scale = AnimationUtils.loadAnimation(this.getContext(), res69);

    ie.ayc.Headings.color(
        this.root,
        R.id.available_class_credits,
        R.id.bookings,
        R.id.workshops_events,
        R.id.schedule);

    final Button redeemCodeButton = this.root.findViewById(R.id.button_redeem);

    // @SuppressWarnings("deprecation")
    redeemCodeButton.setOnClickListener(
        view -> {
          redeemCodeButton.startAnimation(ClassesFragment.this.scale);
          ClassesFragment.this.showRedeemDialog("");
        });

    int res70 = R.id.button_start_monthly;
    final Button startMonthlyButton = this.root.findViewById(res70);

    // @SuppressWarnings("deprecation")
    startMonthlyButton.setOnClickListener(
        view -> {
          startMonthlyButton.startAnimation(ClassesFragment.this.scale);
          ClassesFragment.this.showStartMonthlyDialog();

          Bundle params = new Bundle();
          params.putString("start", "now");
          AycNavigationActivity.analytics().logEvent("monthly", params);
        });

    ScraperManager sm = ScraperManager.getInstance();
    sm.attach(this);
    sm.notifyObservers(UpdateSource.classes);

    AycNavigationActivity.analytics().logEvent("classes", null);

    int res71 = R.id.refreshLayoutClasses;
    SwipeRefreshLayout swipeRefreshLayout = this.root.findViewById(res71);

    swipeRefreshLayout.setOnRefreshListener(
        () -> {
          sm.fetchAll();
          swipeRefreshLayout.setRefreshing(false);
          sm.fetchAll();
        });

    return root;
  }

  private void showRedeemDialog(final String error) {
    // Creating alert Dialog with one Button
    final AlertDialog.Builder alertDialog =
        new AlertDialog.Builder(ClassesFragment.this.root.getContext());

    // Setting Dialog Title
    alertDialog.setTitle("Redeem Code");

    final ie.ayc.FormColumns.Column column =
        ie.ayc.FormColumns.centered(
            getActivity().getApplicationContext(),
            getResources().getDimensionPixelSize(R.dimen.dialog_margin));
    final FrameLayout container = column.container();
    final LinearLayout ll = column.layout();
    final FrameLayout.LayoutParams params = column.fieldParams();

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
      LinearLayout.LayoutParams pms =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT); // xxx
      pms.setMargins(DIALOG_PAD, DIALOG_PAD, DIALOG_PAD, DIALOG_PAD);
      tv.setLayoutParams(pms);
      ll.addView(tv);
    }

    container.addView(ll);
    alertDialog.setView(container);

    // Setting Icon to Dialog
    alertDialog.setIcon(R.drawable.sticker_ganesh);

    // Setting Positive "Yes" Button
    alertDialog.setPositiveButton(
        "Redeem",
        (dialog, which) -> {
          ScraperManager sm = ScraperManager.getInstance();
          sm.applyRedeemCode(input.getText().toString());
          dialog.dismiss();
        });
    // Setting Negative "NO" Button
    alertDialog.setNegativeButton(
        "Cancel", (dialog, which) -> dialog.dismiss()); // xxxxxxxxxxxxxxxx

    // Showing Alert Message
    alertDialog.show();
  }

  private void showStartMonthlyDialog() {
    // Creating alert Dialog with one Button
    final AlertDialog.Builder alertDialog =
        new AlertDialog.Builder(ClassesFragment.this.root.getContext());

    // Setting Dialog Title
    alertDialog.setTitle("Start Monthly Warning");

    final ie.ayc.FormColumns.Column column =
        ie.ayc.FormColumns.centered(
            getActivity().getApplicationContext(),
            getResources().getDimensionPixelSize(R.dimen.dialog_margin));
    final FrameLayout container = column.container();
    final LinearLayout ll = column.layout();

    String warning = "Please check your expiring credit prior to starting";
    warning += " your monthly in case you have available credits";
    warning += " expiring within the month.";
    String[] msgs = new String[WARNING_LINES];
    msgs[0] = "You are about to start your monthly";
    msgs[1] = warning;
    msgs[2] = "Are you sure you want to start your monthly?";

    for (String msg : msgs) {
      TextView tv = new TextView(ClassesFragment.this.root.getContext());
      tv.setText(msg);
      // tv.setTextColor(Color.RED);
      // tv.setTypeface(null, Typeface.BOLD);
      tv.setGravity(Gravity.CENTER_HORIZONTAL);
      LinearLayout.LayoutParams pms =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT); // xxx
      pms.setMargins(DIALOG_PAD, DIALOG_PAD, DIALOG_PAD, DIALOG_PAD);
      tv.setLayoutParams(pms);
      ll.addView(tv);
    }

    container.addView(ll);
    alertDialog.setView(container);

    // Setting Icon to Dialog
    alertDialog.setIcon(R.drawable.sticker_ganesh);

    // Setting Positive "Yes" Button
    alertDialog.setPositiveButton(
        "Start",
        (dialog, which) -> {
          ScraperManager sm = ScraperManager.getInstance();
          sm.beginMonthly();
          dialog.dismiss();
        });
    // Setting Negative "NO" Button
    alertDialog.setNegativeButton(
        "Cancel", (dialog, which) -> dialog.dismiss()); // xxxxxxxxxxxxxxxx

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

  private void addrows(final TableLayout ll, final JSONArray classes) {
    try {
      String lastweek = "";
      String lastdate = "";
      Log.v("ayc-classes-table", " length: " + classes.length());

      LayoutInflater vi = FormColumns.inflater(getActivity());

      for (int t = 0; t < classes.length(); t++) {
        Log.v("ayc-classes", " row: " + t);

        int res76 = R.layout.classes_table_row_time;
        TableRow classcontainer = (TableRow) vi.inflate(res76, null);
        final JSONObject classRow = classes.getJSONObject(t);

        String date = classRow.get("date").toString();
        Log.v("ayc-class-date", date);

        String week = classRow.get("week").toString();
        Log.v("ayc-class-week", week);

        if (lastweek.compareTo(week) != 0) {
          int res77 = R.layout.classes_table_row_week;
          TableRow weekcontainer = (TableRow) vi.inflate(res77, null);
          TextView weektv = (TextView) weekcontainer.getChildAt(0);
          weektv.setText("Week " + week);
          ll.addView(weekcontainer);

          int res78 = R.layout.classes_table_row_week_red_hr;
          TableRow hrcontainer = (TableRow) vi.inflate(res78, null);
          ll.addView(hrcontainer);
        }

        if (lastdate.compareTo(date) != 0) {
          int res79 = R.layout.classes_table_row_day;
          TableRow datecontainer = (TableRow) vi.inflate(res79, null);
          TextView datetv = (TextView) datecontainer.getChildAt(0);
          datetv.setText(date);
          ll.addView(datecontainer);
        }

        TextView tvi = classcontainer.findViewById(R.id.class_instructor);
        tvi.setText(classRow.get("instructor").toString());

        TextView tvct = classcontainer.findViewById(R.id.class_time);
        tvct.setGravity(Gravity.CENTER);
        tvct.setText(
            classRow.get("start_time").toString()
                + "\n"
                + classRow.get("end_time").toString()); // xxxxx

        TextView tvcn = classcontainer.findViewById(R.id.class_name);
        tvcn.setText(classRow.get("name").toString());

        final Button btbk = classcontainer.findViewById(R.id.class_button);
        btbk.setText(classRow.get("button_text").toString());
        btbk.setTag(classRow.get("class_id").toString());
        this.bookButtons.put(classRow.get("class_id").toString(), btbk);

        String action = classRow.get("book_button_action").toString();
        if (action.compareTo("ButtonCancel") != 0) { // xxxxxxxx
          btbk.setOnClickListener(
              v -> {
                btbk.startAnimation(ClassesFragment.this.scale);
                Log.v("ayc-class-book", "clicked");
                ClassesFragment.this.bookButtonClick(btbk);
              });
        } else {
          btbk.setOnClickListener(
              v -> {
                btbk.startAnimation(ClassesFragment.this.scale);
                Log.v("ayc-class-book", "clicked");
                ClassesFragment.this.cancelClass(btbk.getTag().toString());
              });
        }

        String buttonText = classRow.get("button_text").toString();
        String maxAttendees = classRow.get("max_attendees").toString();
        boolean notCancelled = buttonText.compareTo("Cancelled") != 0;
        boolean emptyClass = Integer.parseInt(maxAttendees) == 0;
        if (notCancelled && emptyClass) {
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

  private void bookButtonClick(final Button btn) {
    try {
      String classId = btn.getTag().toString();
      Log.v("ayc-class-book", classId);
      JSONObject aycclass = ScraperManager.getClassById(classId);
      TextView tvc = this.root.findViewById(R.id.textview_available_count);
      int quotaavail = Integer.parseInt(tvc.getText().toString());

      if (aycclass.get("disabled").toString().compareTo("true") == 0) {
        return;
      }

      String free = aycclass.get("free").toString();
      if (quotaavail <= 0 && free.compareTo("false") == 0) {
        String part13 = "You are out of credits, please purchase";
        Common.alert(this.getContext(), part13);
        return;
      }

      if (aycclass.get("cancellation_warning").toString().compareTo("true")
          == 0) { // xxxxxxxxxxxxxx
        String part14 = "\nThis booking cannot be cancelled";
        this.bookButtonConfirm("Confirm booking", part14, classId);
        return;
      }

      this.bookClass(classId);
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }

  private void bookButtonConfirm(
      final String title, final String message, final String classIdXXX) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

    builder.setTitle(title);
    builder.setMessage(message);
    builder.setIcon(R.drawable.sticker_ganesh);

    builder.setPositiveButton(
        "Book",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog, final int which) {
            ClassesFragment.this.bookClass(classIdXXX);
            dialog.dismiss();
          }
        });

    builder.setNegativeButton(
        "Cancel",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog, final int which) {
            dialog.dismiss();
          }
        });

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void bookClass(final String classId) {
    Log.v("ayc-classes", classId);

    ScraperManager sm = ScraperManager.getInstance();
    sm.bookClassAdd(classId);

    Bundle params = new Bundle();
    params.putString("booking_confirmed", classId);
    AycNavigationActivity.analytics().logEvent("class", params);
  }

  private void cancelClass(final String classId) {
    Log.v("ayc-classes", classId);

    ScraperManager sm = ScraperManager.getInstance();
    sm.bookClassRemove(classId);

    Bundle params = new Bundle();
    params.putString("booking_cancel", classId);
    AycNavigationActivity.analytics().logEvent("class", params);
  }

  private void updateCredits() {
    try {
      JSONArray profile = ScraperManager.getProfile();
      JSONObject obj = profile.getJSONObject(2);
      String creditsAvailable = obj.getString("credits_available");
      String part15 = "available_monthlys";
      int availableMonthlys = Integer.parseInt(obj.getString(part15));
      JSONObject types = obj.getJSONObject("credit_type");

      LinearLayout ll = this.root.findViewById(R.id.credit_types);
      ll.removeAllViews();

      if (availableMonthlys == 0) {
        Button button = this.root.findViewById(R.id.button_start_monthly);
        button.setVisibility(View.GONE);
      }

      Iterator<String> it = types.keys();
      while (it.hasNext()) {
        String str = it.next();
        try {
          LayoutInflater wvi = FormColumns.inflater(getActivity());
          int res87 = R.layout.credits_table_row;
          LinearLayout creditTypeRow = (LinearLayout) wvi.inflate(res87, null);
          TextView creditAmount = (TextView) creditTypeRow.getChildAt(0);
          TextView creditType = (TextView) creditTypeRow.getChildAt(1);
          creditAmount.setText(types.getString(str));
          switch (str) {
            case "availableStandardCredits":
              creditType.setText("standard credits");
              break;
            case "availableMonthliesCredits":
              creditType.setText("monthlies");
              break;
            default:
              creditType.setText(str);
              break;
          }
          ll.addView(creditTypeRow);
        } catch (Exception e) {

        }
      }

      TextView tv = this.root.findViewById(R.id.textview_available_count);
      tv.setText(creditsAvailable);
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
        LayoutInflater wvi = FormColumns.inflater(getActivity());
        int res88 = R.layout.booking_table_row;
        LinearLayout bookingRow = (LinearLayout) wvi.inflate(res88, null);
        ie.ayc.BookingRows.populate(
            this, bookingRow, booking, this.scale, "ayc-profile"); // xxxxxxx

        ll.addView(bookingRow);
      } catch (Exception e) {
        Log.v("ayc-classes", e.getMessage());
      }
    }

    if (bookings.length() == 0) {
      LayoutInflater wvi = FormColumns.inflater(getActivity());
      int res89 = R.layout.empty_msg_table_row;
      TableRow bookingRow = (TableRow) wvi.inflate(res89, null);
      TextView empty = (TextView) bookingRow.getChildAt(0);
      empty.setText("You have 0 bookings");
      ll.addView(bookingRow);
    }
  }

  @Override
  public void update(final UpdateSource updatesource) {
    Log.v("ayc-classes", "update");

    try {
      if (updatesource != UpdateSource.classes) {
        return;
      }

      Log.v("ayc-classes", "update accepted");
      this.updateClasses();
      this.updateCredits();
      this.updateBookings();
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }

  @Override
  public void update(final UpdateSource updatesource, final UpdateResponse ur) {
    Log.v("ayc-classes", "update response");

    try {
      if (updatesource != UpdateSource.classes) {
        return;
      }

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
            sm.fetchAll();
          }
          break;
        case "begin_monthly":
        default:
          if (error.compareTo("") != 0) {
            Context context = getActivity().getApplicationContext();
            Common.alert(context, ur.response.getString("error"));
          } else {
            sm.fetchAll();
          }
          break;
      }
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }
}
