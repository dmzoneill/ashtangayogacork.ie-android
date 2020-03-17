package ie.ayc;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class PurchaseActivity extends AppCompatActivity implements AsyncResponse {
    private final AsyncResponse this_async;

    public PurchaseActivity() {
        this.this_async = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        String url = "https://www.paypal.com/cgi-bin/webscr";
        String paypalid = getIntent().getStringExtra("paypal_button_code");
        String postData = "cmd=_s-xclick&hosted_button_id=" + paypalid + "&submit.x=83&submit.y=7";
        WebView wv = findViewById(R.id.webview);
        wv.postUrl(url,postData.getBytes());

        /*
        PurchaseManager task = new PurchaseManager();
        task.delegate = this.this_async;
        String paypalid = getIntent().getStringExtra("paypal_button_code");
        String postfields = "cmd=_s-xclick&hosted_button_id=" + paypalid + "&submit.x=83&submit.y=7";

        task.execute("https://www.paypal.com/cgi-bin/webscr", postfields);
        */
    }


    @Override
    public void processFinish(String output) {
        Log.v("ayc-purchase", output);
        WebView wv = findViewById(R.id.webview);
        wv.loadDataWithBaseURL("https://www.paypal.com/cgi-bin/webscr", output, "text/html", "UTF-8", null);
    }
}
