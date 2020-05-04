package ie.ayc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.jediburrell.customfab.FloatingActionButton;

public class PurchaseActivity extends AppCompatActivity {

    public PurchaseActivity() { }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

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
                PurchaseActivity.this.startActivity(myIntent);
            }
        });

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
                Log.v("ayc-purchase-wv-finish", "onPageFinished");
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        };

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        }

        wv.setWebViewClient(wvc);
        wv.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        wv.postUrl(url,postData.getBytes());
    }

    public void processFinish(String output) {
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