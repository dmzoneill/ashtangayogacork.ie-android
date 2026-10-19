package ie.ayc;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

/** Section headings whose first character is drawn larger and red. */
public final class Headings {
  /** LEAD_SCALE. */
  private static final float LEAD_SCALE = 1.5f;

  /** LEAD_END. */
  private static final int LEAD_END = 1;

  private Headings() {
    // Utility class.
  }

  /**
   * Returns a copy of the text with an enlarged red first character.
   *
   * @param value heading text
   * @return styled heading
   */
  public static Spannable lead(final String value) {
    final Spannable spannable = new SpannableString(value);
    spannable.setSpan(new RelativeSizeSpan(LEAD_SCALE), 0, LEAD_END, 0);
    spannable.setSpan(
        new ForegroundColorSpan(Color.RED),
        0,
        LEAD_END,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // xxxx
    return spannable;
  }

  /**
   * Applies {@link #lead} to each heading in the view.
   *
   * @param root inflated screen
   * @param headingIds heading view ids
   */
  public static void color(final View root, final int... headingIds) {
    for (final int headingId : headingIds) {
      final TextView heading = root.findViewById(headingId);
      heading.setText(lead(heading.getText().toString()));
    }
  }
}
