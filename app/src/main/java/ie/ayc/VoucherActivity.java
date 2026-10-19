package ie.ayc;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Environment;
import android.webkit.WebView;

/** Saves a gift-voucher image into the public downloads directory. */
public final class VoucherActivity extends DocumentDownloadActivity {
  @Override
  protected String logTag() {
    return "ayc-voucher";
  }

  @Override
  protected String loadLogTag() {
    return "ayc-receipt";
  }

  @Override
  protected String permissionLogTag() {
    return "ayc-receipt";
  }

  @Override
  protected String screenTitle() {
    return "Downloading Voucher";
  }

  @Override
  protected void readIntent(final Intent intent) {
    setDocumentUrl(intent.getStringExtra("url"));
  }

  @Override
  protected void prepareWebView(final WebView webView) {
    webView.getSettings().setUserAgentString("ayc-android/1.0");
  }

  @Override
  protected void configureDownload(final DownloadManager.Request request) {
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS, "ayc-voucher.png"); // x
  }

  @Override
  protected void onDownloadQueued() {
    finish();
  }
}
