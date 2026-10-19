package ie.ayc;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Environment;

/** Saves a transaction receipt PDF into the public downloads directory. */
public final class ReceiptActivity extends DocumentDownloadActivity {
  /** transactionId. */
  private String transactionId = "";

  @Override
  protected String logTag() {
    return "ayc-receipt";
  }

  @Override
  protected String loadLogTag() {
    return logTag();
  }

  @Override
  protected String permissionLogTag() {
    return logTag();
  }

  @Override
  protected void prepareWebView(final android.webkit.WebView webView) {
    // Receipts use the default user agent.
  }

  @Override
  protected String screenTitle() {
    return "Downloading Receipt " + this.transactionId;
  }

  @Override
  protected void readIntent(final Intent intent) {
    this.transactionId = intent.getStringExtra("transid");
    setDocumentUrl("https://ashtangayoga.ie/receipt/?id=" + this.transactionId);
  }

  @Override
  protected void configureDownload(final DownloadManager.Request request) {
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "Ayc-Receipt-" + this.transactionId + ".pdf"); // xxxxxxxxxx
  }

  @Override
  protected void onDownloadQueued() {
    Common.alert(getApplicationContext(), "Downloading Receipt");
    finish();
  }
}
