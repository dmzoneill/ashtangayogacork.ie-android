package ie.ayc;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PurchaseManager extends AsyncTask<String, String, String> {

    public AsyncResponse delegate = null;

    @Override
    protected void onPostExecute(String result) {
        try {
            delegate.processFinish(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call
        String data = params[1]; //data to post

        AycCookieManager ayccm = AycCookieManager.getInstance();

        try {
            Log.v("ayc-async-url",urlString);
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.addRequestProperty("Cookie", ayccm.getCookieValue());

            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(4000);
            urlConnection.setRequestMethod("POST");

            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Host", "ashtangayoga.ie");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.addRequestProperty("REFERER", "https://ashtangayoga.ie/prices");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length()));

            // Send post request
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            Log.v("ayc-purchase", " post data: " + data);

            ayccm.addCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

            int responseCode = urlConnection.getResponseCode();

            InputStream inputStream;
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

            Log.v("ayc-async-response",response.toString());

            return response.toString();
        } catch (Exception e) {
            Log.v("ayc-async",e.getMessage());
        }

        return "false";
    }
}
