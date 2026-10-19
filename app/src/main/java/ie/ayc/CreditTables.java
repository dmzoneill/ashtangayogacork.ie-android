package ie.ayc;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

/** Fills credit tables that share one row layout. */
public final class CreditTables {
  /** STRIPE. */
  private static final String STRIPE = "#efefef";

  /** NULL_TYPE. */
  private static final String NULL_TYPE = "null";

  /** STANDARD. */
  private static final String STANDARD = "standard";

  /** AMOUNT_INDEX. */
  private static final int AMOUNT_INDEX = 0;

  /** TRANSACTION_INDEX. */
  private static final int TRANSACTION_INDEX = 1;

  /** EXPIRY_INDEX. */
  private static final int EXPIRY_INDEX = 2;

  /** TYPE_INDEX. */
  private static final int TYPE_INDEX = 3;

  /** DETAILS_INDEX. */
  private static final int DETAILS_INDEX = 1;

  /** COUNT_INDEX. */
  private static final int COUNT_INDEX = 2;

  /** TYPE_FIELD. */
  private static final int TYPE_FIELD = 1;

  private CreditTables() {
    // Utility class.
  }

  /**
   * Replaces the rows in a credit table.
   *
   * @param host fragment that owns the table
   * @param tableId table to refill
   * @param records payload from the scraper, or null when the request failed
   * @param lengthLabel log prefix
   * @param emptyMessage message used when there is nothing to show
   * @param addEmptyAfterLoop append an empty row at the end
   */
  public static void fill(
      final Fragment host,
      final int tableId,
      final JSONArray records,
      final String lengthLabel,
      final String emptyMessage,
      final boolean addEmptyAfterLoop) {
    JSONObject simplified = null;
    final LayoutInflater inflater =
        (LayoutInflater) FormColumns.inflater(host.getActivity()); // xxx
    final TableLayout table = host.getView().findViewById(tableId);
    table.removeAllViews();
    try {
      simplified = records.getJSONObject(DETAILS_INDEX);
    } catch (Exception exception) {
      if (simplified == null) {
        addEmpty(inflater, table, emptyMessage);
        return;
      }
    }
    try {
      Log.v("ayc-profile", lengthLabel + records.length());
      int rowIndex = 0;
      Iterator<String> keys = simplified.keys();
      while (keys.hasNext()) {
        final String key = keys.next();
        final String[] parts = key.split("#");
        final JSONArray details = simplified.getJSONArray(key);
        int res22 = R.layout.profile_credit_table_row;
        final TableRow row = (TableRow) inflater.inflate(res22, null);
        if (rowIndex % 2 > 0) {
          row.setBackgroundColor(Color.parseColor(STRIPE));
        }
        final TextView amount = (TextView) row.getChildAt(AMOUNT_INDEX);
        final TextView transaction =
            (TextView) row.getChildAt(TRANSACTION_INDEX); // xxxxxxxxxxxxxxx
        final TextView expiry = (TextView) row.getChildAt(EXPIRY_INDEX);
        final TextView type = (TextView) row.getChildAt(TYPE_INDEX);
        amount.setText(details.getString(COUNT_INDEX) + "x");
        transaction.setText(parts[0]);
        expiry.setText(parts[DETAILS_INDEX]);
        type.setText(details.getString(TYPE_FIELD));
        if (details.getString(TYPE_FIELD).compareToIgnoreCase(NULL_TYPE) == 0) {
          type.setText(STANDARD);
        }
        table.addView(row);
        rowIndex++;
      }
      if (addEmptyAfterLoop && !simplified.keys().hasNext()) {
        addEmpty(inflater, table, emptyMessage);
      }
    } catch (Exception exception) {
      Log.v("ayc-classes", exception.getMessage());
    }
  }

  private static void addEmpty(
      final LayoutInflater inflater,
      final TableLayout table,
      final String message) { // xxxxxxxxxxxx
    int res23 = R.layout.empty_msg_table_row;
    final TableRow row = (TableRow) inflater.inflate(res23, null);
    final TextView empty = (TextView) row.getChildAt(0);
    empty.setText(message);
    table.addView(row);
  }
}
