package ie.ayc;

import android.webkit.JavascriptInterface;

/** Receives the page HTML that a WebView posts through the HTMLOUT bridge. */
public final class HtmlOutBridge {
  /** Consumer of the HTML snapshot. */
  public interface Sink {
    /**
     * Handles the HTML captured from the page.
     *
     * @param html document markup
     */
    void processFinish(String html);
  }

  /** sink. */
  private final Sink sink;

  /**
   * Binds the bridge to a page-finished handler.
   *
   * @param target activity that reads the HTML
   */
  public HtmlOutBridge(final Sink target) {
    this.sink = target;
  }

  /**
   * Called from the page once the document is available.
   *
   * @param html document markup
   */
  @JavascriptInterface
  public void processHTML(final String html) {
    this.sink.processFinish(html);
  }
}
