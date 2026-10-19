package ie.ayc;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Web client that routes both URL-loading callbacks through one handler. */
public abstract class AycWebViewClient extends WebViewClient {
  /**
   * Decides whether the app handles this address itself.
   *
   * @param view web view that is loading
   * @param uri destination
   * @return true when the host consumes the navigation
   */
  protected abstract boolean handleUri(WebView view, Uri uri);

  @SuppressWarnings("deprecation")
  @Override
  public final boolean shouldOverrideUrlLoading(
      final WebView view, final String urlXXXXXXXXXXXXXX) {
    return handleUri(view, Uri.parse(urlXXXXXXXXXXXXXX));
  }

  @TargetApi(Build.VERSION_CODES.N)
  @Override
  public final boolean shouldOverrideUrlLoading(
      final WebView view, final WebResourceRequest request) {
    return handleUri(view, request.getUrl());
  }
}
