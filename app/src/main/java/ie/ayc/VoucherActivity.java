package ie.ayc;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.jediburrell.customfab.FloatingActionButton;

public class VoucherActivity extends AppCompatActivity {

    String url = "";

    private void downloadVoucher() {
        final WebView wv = this.findViewById(R.id.receiptwebview);
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
                Log.v("ayc-voucher", uri.toString());
                String cookies = AycCookieManager.getInstance().getCookieValue();
                String[] cookiesList = cookies.split(";");
                for (String cookie : cookiesList) {
                    CookieManager.getInstance().setCookie("https://ashtangayoga.ie", cookie);
                }
                Log.v("ayc-voucher", "set cookies");
                CookieSyncManager.getInstance().sync();
                if (uri.toString().contains("profile")) {
                    view.loadUrl(VoucherActivity.this.url);
                } else {
                    view.loadUrl(uri.toString());
                }
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

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.addRequestHeader("cookie", AycCookieManager.getInstance().getCookieValue());
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ayc-voucher.png");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                VoucherActivity.this.finish();
            }
        });

        Log.v("ayc-receipt", this.url);
        wv.getSettings().setUserAgentString("ayc-android/1.0");
        wv.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setFabSize(FloatingActionButton.FAB_SIZE_MINI);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("ayc", "ayc fab");
                Intent myIntent = new Intent(getApplicationContext(), BugReportActivity.class);
                VoucherActivity.this.startActivity(myIntent);
            }
        });

        Intent intent = getIntent();
        this.url = intent.getStringExtra("url");
        setTitle("Downloading Voucher");

        Log.v("ayc-voucher", this.url);

        if (PermissionCheck.readAndWriteExternalStorage(this) == false) {
            return;
        } else {
            this.downloadVoucher();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("ayc-receipt", "onRequestPermissionsResult");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("ayc-receipt", "granted");
            this.downloadVoucher();
        } else {
            Common.alert(this, "Permission to save not granted");
            this.finish();
        }
    }
}
