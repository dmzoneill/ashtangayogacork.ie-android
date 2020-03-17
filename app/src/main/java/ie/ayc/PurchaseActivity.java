package ie.ayc;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class PurchaseActivity extends AppCompatActivity {

    public PurchaseActivity() {

    }

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

        /*
        class MyJavaScriptInterface
        {
            private PurchaseActivity pa;

            public MyJavaScriptInterface(PurchaseActivity pa)
            {
                this.pa = pa;
            }

            @SuppressWarnings("unused")

            public void processContent(String aContent)
            {
                Log.v("ayc-purchase-wv-finish", "processContent");
                this.pa.processFinish(aContent);
            }
        }
        */

        class MyJavaScriptInterface
        {
            private PurchaseActivity pa;

            public MyJavaScriptInterface(PurchaseActivity pa)
            {
                this.pa = pa;
            }

            @JavascriptInterface
            public void processHTML(String html)
            {
                //Html extract here
                Log.v("ayc-purchase-wv-finish", "my : " + html);
                this.pa.processFinish(html);
            }
        }

        WebViewClient wvc = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v("ayc-purchase-wv-finish", "onPageFinished");
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        };

        wv.setWebViewClient(wvc);
        wv.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        wv.postUrl(url,postData.getBytes());
    }

    public void processFinish(String output) {
        Log.v("ayc-purchase-wv-finish", output);
    }
}
