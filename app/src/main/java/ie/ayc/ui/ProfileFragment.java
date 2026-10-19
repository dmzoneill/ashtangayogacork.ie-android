package ie.ayc.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ie.ayc.AycCookieManager;
import ie.ayc.AycNavigationActivity;
import ie.ayc.BuildConfig;
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
import org.json.JSONArray;
import org.json.JSONObject;

public final class ProfileFragment extends Fragment implements Observer {

  /** scale. */
  private Animation scale;

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
    this.root = inflater.inflate(R.layout.fragment_profile, container, false);
    int res100 = R.anim.buttonclick;
    this.scale = AnimationUtils.loadAnimation(this.getContext(), res100);

    String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

    try {
      ie.ayc.Headings.color(
          root,
          R.id.my_bookings,
          R.id.expiring_credit,
          R.id.used_credit,
          R.id.transaction_history); // x

      final ImageButton ime = this.root.findViewById(R.id.button_logout);
      ime.setClickable(true);
      ime.setOnClickListener(
          v -> {
            ime.startAnimation(ProfileFragment.this.scale);
            AycCookieManager.getInstance().clearCookies();
            getActivity().finish();
          });

      final ImageButton imp = this.root.findViewById(R.id.button_settings);
      imp.setClickable(true);
      imp.setOnClickListener(
          v -> {
            imp.startAnimation(ProfileFragment.this.scale);
            Intent myIntent = new Intent(getActivity(), SettingsActivity.class);
            // myIntent.putExtra("url", result); //Optional parameters
            ProfileFragment.this.startActivity(myIntent);
          });

      AycNavigationActivity.analytics().logEvent("profile", null);

      TextView versionText = root.findViewById(R.id.buildversion);
      versionText.setText("Build " + versionCode);
    } catch (Exception e) {
      Log.v("ayc-profile", e.getMessage());
    }

    ScraperManager sm = ScraperManager.getInstance();
    sm.attach(this);
    sm.notifyObservers(UpdateSource.profile);

    int res103 = R.id.refreshLayoutProfile;
    SwipeRefreshLayout swipeRefreshLayout = this.root.findViewById(res103);

    swipeRefreshLayout.setOnRefreshListener(
        () -> {
          sm.fetchAll();
          swipeRefreshLayout.setRefreshing(false);
          sm.fetchAll();
        });

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
      Log.v("ayc-profile", img);
      dit.execute(img);
    } catch (Exception e) {
      Log.v("ayc-profile", e.getMessage());
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

      AycNavigationActivity.analytics().setUserId(ud.getString("ID"));
    } catch (Exception e) {
      Log.v("ayc-profile", e.getMessage());
    }
  }

  private void updateBookings() {
    try {
      JSONArray bookings = ScraperManager.getBookings();
      Log.v("ayc-profile", "bookings-length: " + bookings.length());

      TableLayout ll = this.root.findViewById(R.id.profile_bookings_ll);
      ll.removeAllViews();

      LayoutInflater wvi = ie.ayc.FormColumns.inflater(getActivity());
      // TableRow booking_row_header = (TableRow)
      // wvi.inflate(R.layout.profile_booking_table_row_header, null);
      // ll.addView(booking_row_header);

      Log.v("ayc-profile", "bookings:" + bookings.length());
      for (int t = 0; t < bookings.length(); t++) {
        JSONObject booking = bookings.getJSONObject(t);
        int res104 = R.layout.profile_booking_table_row;
        TableRow bookingRow = (TableRow) wvi.inflate(res104, null);
        ie.ayc.BookingRows.populate(
            this, bookingRow, booking, this.scale, "ayc-profile"); // xxxxxxx

        ll.addView(bookingRow);
      }

      if (bookings.length() == 0) {
        int res105 = R.layout.empty_msg_table_row;
        TableRow bookingRow = (TableRow) wvi.inflate(res105, null);
        TextView empty = (TextView) bookingRow.getChildAt(0);
        empty.setText("You have 0 bookings");
        ll.addView(bookingRow);
      }
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }

  private void updateExpiringCredit() {
    ie.ayc.CreditTables.fill(
        this,
        R.id.profile_expiring_credit_ll,
        ScraperManager.getExpiringCredit(),
        "expired-length: ",
        "You have no expiring credit",
        true);
  }

  private void showVoucherDialog(final String transid) {
    // Creating alert Dialog with one Button
    final AlertDialog.Builder alertDialog =
        new AlertDialog.Builder(ProfileFragment.this.root.getContext());

    // Setting Dialog Title
    alertDialog.setTitle("Create gift voucher");

    final ie.ayc.FormColumns.Column column =
        ie.ayc.FormColumns.centered(
            getActivity().getApplicationContext(),
            getResources().getDimensionPixelSize(R.dimen.dialog_margin));
    final FrameLayout container = column.container();
    final LinearLayout ll = column.layout();
    final FrameLayout.LayoutParams params = column.fieldParams();

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
    alertDialog.setPositiveButton(
        "Gift",
        (dialog, which) -> {
          ScraperManager sm = ScraperManager.getInstance();
          sm.getVoucherUrl(input.getText().toString(), transid);
          dialog.dismiss();
        });
    // Setting Negative "NO" Button
    alertDialog.setNegativeButton(
        "Cancel", (dialog, which) -> dialog.dismiss()); // xxxxxxxxxxxxxxxx

    // Showing Alert Message
    alertDialog.show();
  }

  private void updateUsedCredit() {
    ie.ayc.CreditTables.fill(
        this,
        R.id.profile_used_credit_ll,
        ScraperManager.getUsedCredit(),
        "used-length: ",
        "You have no used credit",
        false);
  }

  private void updateTransactions() {
    try {
      JSONArray transactions = ScraperManager.getTransactions();
      Log.v("ayc-profile", "transactions-length: " + transactions.length());

      TableLayout ll = this.root.findViewById(R.id.profile_transactions_ll);
      ll.removeAllViews();

      LayoutInflater wvi = ie.ayc.FormColumns.inflater(getActivity());
      // Header row intentionally omitted.
      // null);
      // ll.addView(trans_row_header);

      for (int t = 0; t < transactions.length(); t++) {
        JSONObject obj = transactions.getJSONObject(t);
        int res106 = R.layout.profile_trans_table_row;
        TableRow transRow = (TableRow) wvi.inflate(res106, null);
        if (t % 2 > 0) {
          transRow.setBackgroundColor(Color.parseColor("#efefef"));
        }

        TextView tvdate = (TextView) transRow.getChildAt(0);
        TextView tvtransid = (TextView) transRow.getChildAt(1);
        TextView tvtype = (TextView) transRow.getChildAt(2);
        final ImageView ivreceipt = (ImageView) transRow.getChildAt(3);
        final ImageView ivgift = (ImageView) transRow.getChildAt(4);

        String rawId = obj.getString("id");
        final String tid = java.net.URLDecoder.decode(rawId, "UTF-8");
        String str6 = "payer_email";
        String name = java.net.URLDecoder.decode(obj.getString(str6), "UTF-8");
        String rawFirst = obj.getString("first_name");
        String firstName = java.net.URLDecoder.decode(rawFirst, "UTF-8");
        String rawLast = obj.getString("last_name");
        String lastName = java.net.URLDecoder.decode(rawLast, "UTF-8");
        String purchaseAmount = obj.getString("purchase_amount");
        String fullName = firstName + " " + lastName;
        String payee = "";

        if (firstName.compareToIgnoreCase("cash") == 0) {
          payee = "Cash";
        } else {
          payee = fullName + "\n" + name;
        }

        tvdate.setText(obj.getString("purchase_date"));
        tvtransid.setText(obj.getString("txn_id") + "\n" + payee);
        tvtype.setText(obj.getString("class_type_restriction"));

        if (Integer.parseInt(obj.getString("used_tokens")) != 0) {
          ivgift.setVisibility(View.INVISIBLE);
        } else {
          ivgift.setClickable(true);
          ivgift.setOnClickListener(
              new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                  ivgift.startAnimation(ProfileFragment.this.scale);
                  ProfileFragment.this.showVoucherDialog(tid);
                }
              });
        }
        if (obj.getString("class_type_restriction").compareToIgnoreCase("null")
            == 0) { // xxxxxxxxxx
          tvtype.setText(purchaseAmount + "\nstandard");
        }

        ivreceipt.setClickable(true);
        ivreceipt.setOnClickListener(
            v -> {
              ivreceipt.startAnimation(ProfileFragment.this.scale);
              Common.alert(getContext(), "Loading receipt...");
              v.startAnimation(
                  AnimationUtils.loadAnimation(
                      ProfileFragment.this.getContext(), R.anim.image_click));
              Context host = getActivity();
              Intent myIntent = new Intent(host, ReceiptActivity.class);
              myIntent.putExtra("transid", tid); // Optional parameters
              ProfileFragment.this.startActivity(myIntent);
            });

        ll.addView(transRow);
      }

      if (transactions.length() == 0) {
        int res108 = R.layout.empty_msg_table_row;
        TableRow bookingRow = (TableRow) wvi.inflate(res108, null);
        TextView empty = (TextView) bookingRow.getChildAt(0);
        empty.setText("You have no transactions");
        ll.addView(bookingRow);
      }
    } catch (Exception e) {
      Log.v("ayc-profile", e.getMessage());
    }
  }

  @Override
  public void update(final UpdateSource updatesource) {
    if (updatesource != UpdateSource.profile) {
      return;
    }
    try {
      this.updateImageView();
      this.updateTransactions();
      this.updateExpiringCredit();
      this.updateUsedCredit();
      this.updateBookings();
      this.updateUsername();
    } catch (Exception e) {
      Log.v("ayc-classes-update", e.getMessage());
    }
  }

  @Override
  public void update(final UpdateSource updatesource, final UpdateResponse ur) {
    Log.v("ayc-profile", "update response");

    try {
      if (updatesource != UpdateSource.profile) {
        return;
      }

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
            Intent myIntent = new Intent(getActivity(), VoucherActivity.class);
            myIntent.putExtra("url", result); // Optional parameters
            ProfileFragment.this.startActivity(myIntent);
          }
          break;
        case "open_door":
          if (error.compareTo("") != 0) {
            Common.alert(getContext(), error);
          }
          break;
        default:
          break;
      }
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }
}
