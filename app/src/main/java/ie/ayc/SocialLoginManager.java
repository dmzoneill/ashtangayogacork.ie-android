package ie.ayc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

/** Completes a social login and stores cookies. */
public final class SocialLoginManager extends AppCompatActivity {
  /** HOST. */
  private static final String HOST = "ashtangayoga.ie/";

  /** SUCCESS_PATH. */
  private static final String SUCCESS_PATH = HOST + "transaction-success";

  /** HOME. */
  private static final String HOME = "https://ashtangayoga.ie/";

  /** LOGIN_PAGE. */
  private static final String LOGIN_PAGE = "wp-login.php";

  /** AGENT_A. */
  private static final String AGENT_A = "Mozilla/5.0 (Linux; Android 7.0; ";

  /** AGENT_B. */
  private static final String AGENT_B = "SM-G930V Build/NRD90M) ";

  /** AGENT_E. */
  private static final String AGENT_E = "AppleWebKit/537.36 ";

  /** AGENT_C. */
  private static final String AGENT_C = "(KHTML, like Gecko) ";

  /** AGENT_F. */
  private static final String AGENT_F = "Chrome/59.0.3071.125 ";

  /** AGENT_D. */
  private static final String AGENT_D = "Mobile Safari/537.36";

  /** MOBILE_AGENT. */
  private static final String AGENT_LEFT = AGENT_A + AGENT_B + AGENT_E;

  /** AGENT_RIGHT. */
  private static final String AGENT_RIGHT = AGENT_C + AGENT_F + AGENT_D;

  /** MOBILE_AGENT. */
  private static final String MOBILE_AGENT = AGENT_LEFT + AGENT_RIGHT;

  @SuppressLint("JavascriptInterface")
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_social_login_manager);
    final Intent intent = getIntent();
    final String url = socialUrl(intent.getStringExtra("type"));
    final WebView webView = findViewById(R.id.social_webview);
    webView.getSettings().setUserAgentString(MOBILE_AGENT);
    webView.setWebViewClient(
        new AycWebViewClient() {
          @Override
          protected boolean handleUri(final WebView view, final Uri uri) {
            Log.v("ayc-social", uri.toString());
            if (uri.toString().contains(SUCCESS_PATH)) {
              Log.v("ayc-purchase", "update_user_credits");
              view.loadUrl(uri.toString());
            }
            return false;
          }

          @Override
          public void onPageFinished(final WebView view, final String pageUrl) {
            storeCookies(pageUrl);
            if (pageUrl.contains(HOME) && !pageUrl.contains(LOGIN_PAGE)) {
              finish();
            }
          }
        });
    AycCookieManager.getInstance().clearCookies();
    webView.getSettings().setJavaScriptEnabled(true);
    webView.addJavascriptInterface(
        new HtmlOutBridge(this::processFinish), "HTMLOUT"); // xxxxxxxxxxx
    webView.loadUrl(url);
  }

  private static String socialUrl(final String type) {
    if ("fb".compareTo(type) == 0) {
      return "https://ashtangayoga.ie/wp-login.php?loginSocial=facebook"
          + "&redirect=https%3A%2F%2Fashtangayoga.ie%2F";
    }
    return "https://ashtangayoga.ie/wp-login.php?loginSocial=google"
        + "&redirect=https%3A%2F%2Fashtangayoga.ie%2F";
  }

  private static void storeCookies(final String pageUrl) {
    final List<String> cookies = new ArrayList<String>();
    final String header = CookieManager.getInstance().getCookie(pageUrl);
    final String[] pairs = header.split(";");
    for (final String value : pairs) {
      cookies.add(value + ";");
      Log.v("ayc-social-login", "cookie: " + value);
    }
    AycCookieManager.getInstance().addCookies(cookies);
    if (pageUrl.contains(HOME) && !pageUrl.contains(LOGIN_PAGE)) {
      Log.v("ayc-social-login", "cookie: " + cookies.toArray().toString());
    }
  }

  /**
   * processFinish.
   *
   * @param output value
   */
  public void processFinish(final String output) {
    Log.v("ayc-purchase-wv-finish", output);
  }
}
