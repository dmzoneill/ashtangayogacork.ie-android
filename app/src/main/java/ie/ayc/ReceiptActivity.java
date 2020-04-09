package ie.ayc;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ReceiptActivity extends AppCompatActivity {

    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Intent intent = getIntent();
        this.id = intent.getStringExtra("transid");
        String url = "https://ashtangayoga.ie/receipt/?id=" + this.id;

        setTitle("Downloading Receipt " + this.id);

        Log.v("ayc-receipt", url);

        final WebView wv = findViewById(R.id.receiptwebview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSaveFormData(true);
        wv.getSettings().setAppCacheEnabled(true);

        WebViewClient wvc = new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final Uri uri = Uri.parse(url);
                return handleUri(view, uri);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                return handleUri(view, uri);
            }

            private boolean handleUri(WebView view, final Uri uri) {
                Log.v("ayc-receipt", uri.toString());
                String cookies = AycCookieManager.getInstance().getCookieValue();
                String[] cookiesList = cookies.split(";");
                for(String cookie : cookiesList){
                    CookieManager.getInstance().setCookie("https://ashtangayoga.ie", cookie);
                }
                Log.v("ayc-receipt", "set cookies");
                CookieSyncManager.getInstance().sync();
                view.loadUrl(uri.toString());
                return false;
            }
        };

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        }

        wv.setWebViewClient(wvc);

        wv.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.addRequestHeader("cookie", AycCookieManager.getInstance().getCookieValue());
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Ayc-Receipt-" + ReceiptActivity.this.id +".pdf");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Common.alert(getApplicationContext(), "Downloading Receipt");
                ReceiptActivity.this.finish();
            }
        });

        Log.v("ayc-receipt", url);
        wv.loadUrl(url);
    }
}
