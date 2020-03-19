package ie.ayc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PurchaseActivity extends AppCompatActivity {

    String completed = "";

    public PurchaseActivity() { }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        String url = "https://www.paypal.com/cgi-bin/webscr";
        String paypalid = getIntent().getStringExtra("paypal_button_code");
        String postData = "cmd=_s-xclick&hosted_button_id=" + paypalid + "&submit.x=83&submit.y=7";

        final WebView wv = findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSaveFormData(true);
        wv.getSettings().setAppCacheEnabled(true);

        class MyJavaScriptInterface
        {
            private PurchaseActivity pa;

            public MyJavaScriptInterface(PurchaseActivity pa)
            {
                this.pa = pa;
            }

            @JavascriptInterface
            public void processHTML(String html) {
                this.pa.processFinish(html);
            }
        }

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
                final String host = uri.getHost();
                if (uri.toString().contains("ashtangayoga.ie/transaction-success")) {
                    Log.v("ayc-purchase", "update_user_credits");
                    this.update_user_credits(uri.toString());
                    view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v("ayc-purchase-wv-finish", "onPageFinished");
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }

            private void update_user_credits(String strurl){
                AycCookieManager ayccm = AycCookieManager.getInstance();

                try {
                    URL url = new URL(strurl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setUseCaches(false);
                    urlConnection.setDoOutput(true);
                    urlConnection.setInstanceFollowRedirects(false);
                    urlConnection.setConnectTimeout(4000);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.addRequestProperty("Cookie", ayccm.getCookieValue());
                    urlConnection.setRequestProperty("Host", "ashtangayoga.ie");
                    urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    urlConnection.setRequestProperty("Connection", "keep-alive");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.addRequestProperty("REFERER", "https://ashtangayoga.ie/wp-login.php");
                    urlConnection.setRequestProperty("charset", "utf-8");

                    // handle error response code it occurs
                    int responseCode = urlConnection.getResponseCode();
                    InputStream inputStream;
                    Log.v("ayc-purchase", " response code: " + responseCode);
                    if (200 <= responseCode && responseCode <= 299) {
                        inputStream = urlConnection.getInputStream();
                    } else {
                        inputStream = urlConnection.getErrorStream();
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String currentLine;

                    while ((currentLine = in.readLine()) != null) {
                        response.append(currentLine);
                    }
                    in.close();

                    String resp = response.toString();

                    int maxLogSize = 1000;
                    for (int i = 0; i <= resp.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i + 1) * maxLogSize;
                        end = end > resp.length() ? resp.length() : end;
                        Log.v("ayc-purchase", resp.substring(start, end));
                    }

                    PurchaseActivity.this.completed = resp;
                }
                catch(Exception e){
                    Log.v("ayc-purchase", e.getMessage());
                }
            }
        };

        wv.setWebViewClient(wvc);
        wv.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        wv.postUrl(url,postData.getBytes());
    }

    public void processFinish(String output) {
        if(this.completed.compareToIgnoreCase("") != 0) {
            output = this.completed;
        }

        String[] failed = new String[2];
        failed[0] = "Sorry to see you cancelled your transaction, maybe next time";
        failed[1] = "There was an error with your transaction";

        String[] success = new String[2];
        success[0] = "Thank you for completing your transaction";

        for (String str: failed) {
            if(output.contains(str)){
                this.finish();
            }
        }

        for (String str: success) {
            if(output.contains(str)){
                this.finish();
            }
        }

        Log.v("ayc-purchase-wv-finish", output);
    }
}
