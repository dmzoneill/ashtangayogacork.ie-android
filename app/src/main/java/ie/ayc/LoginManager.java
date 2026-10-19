package ie.ayc;

import android.util.Log;
import java.net.HttpURLConnection;

/** Posts the login form and reports the session. */
public final class LoginManager extends CookieTask {
  /** USER_AGENT. */
  private static final String USER_AGENT = "Mozilla/5.0";

  /** BAD_PASSWORD. */
  private static final String BAD_PASSWORD =
      "<strong>ERROR</strong>: Incorrect username or password";

  /** LOGGED_OUT. */
  private static final String LOGGED_OUT = "{\"loggedin\":false}";

  /** LOGGED_IN. */
  private static final String LOGGED_IN = "{\"loggedin\":true}";

  /** Creates a login request. */
  public LoginManager() {
    // Delegate is attached by the caller.
  }

  @Override
  protected String doInBackground(final String... params) {
    final String urlString = params[0];
    final String data = params[1];
    Log.v("ayc-login-url", urlString);
    final AycCookieManager cookies = AycCookieManager.getInstance();
    try {
      final HttpURLConnection connection =
          SiteHttp.open(urlString, "POST", false, false); // xxxxxxx
      connection.addRequestProperty("Cookie", cookies.getCookieValue());
      SiteHttp.applyPageHeaders(connection, USER_AGENT);
      SiteHttp.writeForm(connection, data);
      Log.v("ayc-login", " post data: " + data);
      cookies.addCookies(connection.getHeaderFields().get("Set-Cookie"));
      final String body = SiteHttp.readBody(connection, "ayc-login");
      SiteHttp.logChunks("ayc-resp", body);
      if (body.contains(BAD_PASSWORD)) {
        Log.v("ayc-login", " Failed to log in");
        return LOGGED_OUT;
      }
      Log.v("ayc-login", " logged in");
      return LOGGED_IN;
    } catch (Exception exception) {
      Log.v("ayc-async", exception.getMessage());
    }
    return LOGGED_OUT;
  }
}
