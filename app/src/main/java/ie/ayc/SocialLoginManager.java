package ie.ayc;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialLoginManager extends AppCompatActivity {

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_login_manager);

        Intent intent = getIntent();

        String url;

        if( intent.getStringExtra("type").compareTo("fb") == 0) {
            url = "https://ashtangayoga.ie/wp-login.php?loginSocial=facebook&redirect=https%3A%2F%2Fashtangayoga.ie%2F";
        } else {
            url = "https://ashtangayoga.ie/wp-login.php?loginSocial=google&redirect=https%3A%2F%2Fashtangayoga.ie%2F";
        }

        class MyJavaScriptInterface
        {
            private SocialLoginManager pa;

            public MyJavaScriptInterface(SocialLoginManager pa)
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
                Log.v("ayc-social", uri.toString());
                if (uri.toString().contains("ashtangayoga.ie/transaction-success")) {
                    Log.v("ayc-purchase", "update_user_credits");

                    String cookies = AycCookieManager.getInstance().getCookieValue();
                    String[] cookiesList = cookies.split(";");
                    for(String cookie : cookiesList){
                        CookieManager.getInstance().setCookie("https://ashtangayoga.ie", cookie);
                    }
                    CookieSyncManager.getInstance().sync();
                    view.loadUrl(uri.toString());
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                List<String> cookie_list = new ArrayList<>();
                String[] key_vals = CookieManager.getInstance().getCookie(url).split(";");
                for(String val: key_vals){
                    cookie_list.add(val + ";");
                    Log.v("ayc-social-login", "cookie: " + val);
                }
                AycCookieManager.getInstance().addCookies(cookie_list);
                if(url.contains("https://ashtangayoga.ie/") && url.contains("wp-login.php") == false) {
                    Log.v("ayc-social-login", "cookie: " + cookie_list.toArray().toString());
                    SocialLoginManager.this.finish();
                }
            }
        };

        AycCookieManager.getInstance().clearCookies();
        WebView wv = this.findViewById(R.id.social_webview);
        wv.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        wv.setWebViewClient(wvc);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSaveFormData(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        wv.loadUrl(url);
    }

    public void processFinish(String output) {
        Log.v("ayc-purchase-wv-finish", output);
    }
}
