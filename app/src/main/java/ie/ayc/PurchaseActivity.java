package ie.ayc;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

/** Posts a class purchase and closes on the result. */
public final class PurchaseActivity extends AppCompatActivity {
  /** HOST. */
  private static final String HOST = "ashtangayoga.ie/";

  /** SUCCESS_PATH. */
  private static final String SUCCESS_PATH = HOST + "transaction-success";

  /** CANCELLED. */
  private static final String CANCELLED =
      "Sorry to see you cancelled your transaction, maybe next time";

  /** FAILED_A. */
  private static final String FAILED_A = "There was an error ";

  /** FAILED. */
  private static final String FAILED = FAILED_A + "with your transaction";

  /** THANKS_A. */
  private static final String THANKS_A = "Thank you for completing ";

  /** THANK_YOU. */
  private static final String THANK_YOU = THANKS_A + "your transaction";

  /** Required by older layout inflation paths. */
  public PurchaseActivity() {
    // No extra setup.
  }

  @SuppressLint("JavascriptInterface")
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_purchase);
    ActivityChrome.hideActionBar(this);
    ActivityChrome.attachBugReportButton(this);

    final String[] data = getIntent().getStringArrayExtra("endpoint");
    final String url = data[0];
    final String postData = data[1];
    final WebView webView = findViewById(R.id.webview);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(
        new AycWebViewClient() {
          @Override
          protected boolean handleUri(final WebView view, final Uri uri) {
            if (uri.toString().contains(SUCCESS_PATH)) {
              Log.v("ayc-purchase", "update_user_credits");
              view.loadUrl(uri.toString());
            }
            return false;
          }

          @Override
          public void onPageFinished(final WebView view, final String pageUrl) {
            Log.v("ayc-purchase-wv-finish", "onPageFinished");
            String script = "javascript:window.HTMLOUT.processHTML('<head>'";
            script += "+document.getElementsByTagName('html')[0]";
            script += ".innerHTML+'</head>');";
            view.loadUrl(script);
          }
        });
    WebViews.acceptCookies(webView, false);
    webView.addJavascriptInterface(
        new HtmlOutBridge(this::processFinish), "HTMLOUT"); // xxxxxxxxxxx
    Log.v("ayc-purchase", postData);
    Log.v("ayc-purchase", url);
    webView.postUrl(url, postData.getBytes());
  }

  /**
   * processFinish.
   *
   * @param output value
   */
  public void processFinish(final String output) {
    if (output.contains(CANCELLED)
        || output.contains(FAILED)
        || output.contains(THANK_YOU)) { // xxx
      finish();
    }
    Log.v("ayc-purchase-wv-finish", output);
  }
}
