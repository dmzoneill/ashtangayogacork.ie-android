package ie.ayc;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/** Centered column used by the redeem, monthly, and voucher prompts. */
public final class FormColumns {
  /** A dialog body and the margins applied to its fields. */
  public static final class Column {
    /** container. */
    private final FrameLayout container;

    /** layout. */
    private final LinearLayout layout;

    /** fieldParams. */
    private final FrameLayout.LayoutParams fieldParams;

    private Column(
        final FrameLayout frame,
        final LinearLayout body,
        final FrameLayout.LayoutParams params) { // x
      this.container = frame;
      this.layout = body;
      this.fieldParams = params;
    }

    /**
     * Frame attached as the dialog view.
     *
     * @return outer frame
     */
    public FrameLayout container() {
      return this.container;
    }

    /**
     * Vertical column inside the frame.
     *
     * @return column
     */
    public LinearLayout layout() {
      return this.layout;
    }

    /**
     * Margins for a full-width field.
     *
     * @return layout params
     */
    public FrameLayout.LayoutParams fieldParams() {
      return this.fieldParams;
    }
  }

  private FormColumns() {
    // Utility class.
  }

  /**
   * Layout inflater for a context.
   *
   * @param context host context
   * @return inflater
   */
  public static LayoutInflater inflater(final Context context) {
    String service = Context.LAYOUT_INFLATER_SERVICE;
    return (LayoutInflater) context.getSystemService(service);
  }

  /**
   * Builds a horizontally centered column with the dialog side margins.
   *
   * @param context dialog context
   * @param sideMarginPx left and right margin in pixels
   * @return column holder
   */
  public static Column centered(final Context context, final int sideMarginPx) {
    final FrameLayout container = new FrameLayout(context);
    final LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.CENTER_HORIZONTAL);
    final FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT); // xxxxxxxxxxx
    params.leftMargin = sideMarginPx;
    params.rightMargin = sideMarginPx;
    return new Column(container, layout, params);
  }
}
