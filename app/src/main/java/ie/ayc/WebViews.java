package ie.ayc;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebView;

/** Cookie setup shared by the in-app browsers. */
public final class WebViews {
  private WebViews() {
    // Utility class.
  }

  /**
   * Accepts first-party and, on Lollipop and newer, third-party cookies.
   *
   * @param webView browser that should store cookies
   * @param acceptFirstPartyXXXXXXXXXX accept first-party cookies
   */
  public static void acceptCookies(
      final WebView webView, final boolean acceptFirstPartyXXXXXXXXXX) {
    if (acceptFirstPartyXXXXXXXXXX) {
      CookieManager.getInstance().setAcceptCookie(true);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }
  }
}
