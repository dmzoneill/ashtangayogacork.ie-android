package ie.ayc;

import android.util.Log;
import java.net.HttpURLConnection;

/** Loads a session URL and returns the body. */
public final class SessionManager extends CookieTask {
  /** LOGGED_OUT. */
  private static final String LOGGED_OUT = "{\"loggedin\":false}";

  /** Creates a session request. */
  public SessionManager() {
    // Delegate is attached by the caller.
  }

  @Override
  protected String doInBackground(final String... params) {
    final String urlString = params[0];
    final AycCookieManager cookies = AycCookieManager.getInstance();
    try {
      final HttpURLConnection connection =
          SiteHttp.open(urlString, "GET", true, true); // xxxxxxxxxx
      connection.addRequestProperty("Cookie", cookies.getCookieValue());
      connection.setRequestProperty("charset", "utf-8");
      cookies.addCookies(connection.getHeaderFields().get("Set-Cookie"));
      return SiteHttp.readBody(connection, "ayc-login");
    } catch (Exception exception) {
      Log.v("ayc-async", exception.getMessage());
    }
    return LOGGED_OUT;
  }
}
