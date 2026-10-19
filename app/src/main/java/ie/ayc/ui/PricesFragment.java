package ie.ayc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ie.ayc.AycNavigationActivity;
import ie.ayc.Common;
import ie.ayc.Observer;
import ie.ayc.PurchaseActivity;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateResponse;
import ie.ayc.UpdateSource;
import org.json.JSONArray;
import org.json.JSONObject;

public final class PricesFragment extends Fragment implements Observer {

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
    this.root = inflater.inflate(R.layout.fragment_prices, container, false);
    int res90 = R.anim.buttonclick;
    this.scale = AnimationUtils.loadAnimation(this.getContext(), res90);

    ie.ayc.Headings.color(
        this.root,
        R.id.yoga_for_everyone,
        R.id.gifts,
        R.id.monthlies,
        R.id.specials,
        R.id.standard);

    ScraperManager sm = ScraperManager.getInstance();
    sm.attach(this);
    sm.notifyObservers(UpdateSource.prices);

    AycNavigationActivity.analytics().logEvent("prices", null);

    return root;
  }

  private void updateTables() {
    ScraperManager sm = ScraperManager.getInstance();

    JSONArray pricesMonthly = ScraperManager.getPrices(0);
    Log.v("ayc-prices-fragment", " monthlies: " + pricesMonthly.length());

    JSONArray pricesSpecial = ScraperManager.getPrices(1);
    Log.v("ayc-prices-fragment", " specials: " + pricesSpecial.length());

    JSONArray pricesStandard = ScraperManager.getPrices(2);
    Log.v("ayc-prices-fragment", " standard: " + pricesStandard.length());

    LinearLayout lm = this.root.findViewById(R.id.monthlies_table);
    lm.removeAllViews();
    this.addrows(lm, pricesMonthly);

    LinearLayout ls = this.root.findViewById(R.id.specials_table);
    ls.removeAllViews();
    this.addrows(ls, pricesSpecial);

    LinearLayout lc = this.root.findViewById(R.id.standard_table);
    lc.removeAllViews();
    this.addrows(lc, pricesStandard);
  }

  private void addrows(final LinearLayout ll, final JSONArray prices) {
    try {
      Log.v("ayc-prices-table", " length: " + prices.length());
      for (int t = 0; t < prices.length(); t++) {
        Log.v("ayc-prices", " row: " + t);
        LayoutInflater vi = ie.ayc.FormColumns.inflater(getActivity());
        int res91 = R.layout.prices_table_row;
        LinearLayout rowcontainer = (LinearLayout) vi.inflate(res91, null);
        final JSONObject price = prices.getJSONObject(t);
        final String name = price.get("name").toString();
        Log.v("ayc-prices", " name: " + name);
        String euros = price.get("price").toString();
        Log.v("ayc-prices", " euros: " + euros);
        LinearLayout row = (LinearLayout) rowcontainer.getChildAt(0);
        TextView tvname = (TextView) row.getChildAt(0);
        tvname.setText(name);

        String credits = price.get("credits").toString();
        Log.v("ayc-prices", " credits: " + credits);
        String days = price.get("credit_expiry").toString().substring(1).trim();
        Log.v("ayc-prices", " days: " + days);

        TextView tvvalidity = (TextView) row.getChildAt(2);
        tvvalidity.setText(
            String.format(
                "You will receive %s credit(s) and it's valid for %s days.",
                credits, days)); // xxxx

        TextView tvprice = (TextView) rowcontainer.getChildAt(1);
        tvprice.setText("€" + euros);

        final String endpoint = price.get("endpoint").toString();
        final String params = price.get("params").toString();

        final ImageButton buybutton = (ImageButton) rowcontainer.getChildAt(2);
        buybutton.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(final View v) {
                buybutton.startAnimation(PricesFragment.this.scale);
                Context host = getActivity();
                Intent myIntent = new Intent(host, PurchaseActivity.class);
                String[] endpointData = new String[] {endpoint, params};
                myIntent.putExtra("endpoint", endpointData);
                PricesFragment.this.startActivity(myIntent);

                Bundle params = new Bundle();
                params.putString("class_type", name);
                AycNavigationActivity.analytics().logEvent("buy", params);
              }
            });

        ll.addView(rowcontainer);
        Log.v("ayc-prices", " added row");
      }
      if (prices.length() == 0) {
        LayoutInflater vi = ie.ayc.FormColumns.inflater(getActivity());
        int res95 = R.layout.empty_msg_table_row;
        TableRow bookingRow = (TableRow) vi.inflate(res95, null);
        TextView empty = (TextView) bookingRow.getChildAt(0);
        empty.setText(
            "Sorry, there are none available at this time, contact us for your"
                + " information"); // xxxx
        ll.addView(bookingRow);
      }
    } catch (Exception e) {
      Log.v("ayc-tablerow", e.getMessage());
    }
  }

  @Override
  public void update(final UpdateSource updatesource) {
    try {
      if (updatesource != UpdateSource.prices) {
        return;
      }
      this.updateTables();
    } catch (Exception e) {
      Log.v("ayc-prices-update", e.getMessage());
    }
  }

  @Override
  public void update(final UpdateSource updatesource, final UpdateResponse ur) {
    Log.v("ayc-classes", "update response");

    try {
      if (updatesource != UpdateSource.classes) {
        return;
      }

      if (ur.response.has("success")) {
        Common.alert(this.getContext(), "class booked");
      }
    } catch (Exception e) {
      Log.v("ayc-classes", e.getMessage());
    }
  }
}
