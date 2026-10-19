package ie.ayc;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

/** Downloads a document through a WebView. */
public abstract class DocumentDownloadActivity extends AppCompatActivity {
  /** Address loaded into the hidden browser. */
  private String documentUrl = "";

  /**
   * Address loaded into the hidden browser.
   *
   * @return document URL
   */
  protected final String documentUrl() {
    return this.documentUrl;
  }

  /**
   * Sets the address loaded into the hidden browser.
   *
   * @param url document URL
   */
  protected final void setDocumentUrl(final String url) {
    this.documentUrl = url;
  }

  /**
   * Log tag for navigation messages.
   *
   * @return log tag
   */
  protected abstract String logTag();

  /**
   * Log tag used for the line written just before {@code loadUrl}.
   *
   * @return log tag
   */
  protected abstract String loadLogTag();

  /**
   * Log tag used while storage permission is resolved.
   *
   * @return log tag
   */
  protected abstract String permissionLogTag();

  /**
   * Title shown while the file is fetched.
   *
   * @return screen title
   */
  protected abstract String screenTitle();

  /**
   * Reads the download address from the launching intent.
   *
   * @param intent launching intent
   */
  protected abstract void readIntent(Intent intent);

  /**
   * Adjusts the web view before the document URL is loaded.
   *
   * @param webView document web view
   */
  protected abstract void prepareWebView(WebView webView);

  /**
   * Chooses the download destination and any extra request options.
   *
   * @param request download request already carrying the session cookie
   */
  protected abstract void configureDownload(DownloadManager.Request request);

  /** Runs after the download has been queued. */
  protected abstract void onDownloadQueued();

  @Override
  protected final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_receipt);
    ActivityChrome.hideActionBar(this);
    ActivityChrome.attachBugReportButton(this);
    readIntent(getIntent());
    setTitle(screenTitle());
    Log.v(logTag(), this.documentUrl);
    if (PermissionCheck.readAndWriteExternalStorage(this)) {
      openDocument();
    }
  }

  private void openDocument() {
    final WebView webView = findViewById(R.id.receiptwebview);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(
        new AycWebViewClient() {
          @Override
          protected boolean handleUri(final WebView view, final Uri uri) {
            Log.v(logTag(), uri.toString());
            if (uri.toString().contains("profile")) {
              view.loadUrl(documentUrl);
            } else {
              view.loadUrl(uri.toString());
            }
            return false;
          }
        });
    WebViews.acceptCookies(webView, true);
    webView.setDownloadListener(
        new DownloadListener() {
          @Override
          public void onDownloadStart(
              final String url,
              final String userAgent,
              final String contentDisposition,
              final String mimeType,
              final long contentLength) {
            final DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url)); // x
            request.addRequestHeader(
                "cookie", AycCookieManager.getInstance().getCookieValue()); // x
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            configureDownload(request);
            final DownloadManager manager =
                (DownloadManager) getSystemService(DOWNLOAD_SERVICE); // x
            manager.enqueue(request);
            onDownloadQueued();
          }
        });
    Log.v(loadLogTag(), this.documentUrl);
    prepareWebView(webView);
    webView.loadUrl(this.documentUrl);
  }

  @Override
  public final void onRequestPermissionsResult(
      final int requestCode,
      final String[] permissions,
      final int[] grantResults) { // xxxxxxxxxxxxx
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    Log.v(permissionLogTag(), "onRequestPermissionsResult");
    if (grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // xxxxxxx
      Log.v(permissionLogTag(), "granted");
      openDocument();
    } else {
      Common.alert(this, "Permission to save not granted");
      finish();
    }
  }
}
