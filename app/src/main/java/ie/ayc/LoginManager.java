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
import java.util.Objects;

public class LoginManager extends AsyncTask<String, String, String> {

    public AsyncResponse delegate = null;

    public LoginManager(){
        //set context variables if required
    }

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

        Log.v("ayc-login-url", urlString);

        AycCookieManager ayccm = AycCookieManager.getInstance();

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(4000);
            urlConnection.setRequestMethod("POST");

            urlConnection.addRequestProperty("Cookie", ayccm.getCookieValue());

            urlConnection.setRequestProperty("Host", "ashtangayoga.ie");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.addRequestProperty("REFERER", "https://ashtangayoga.ie/wp-login.php");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length()));

            // Send post request
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            Log.v("ayc-login", " post data: " + data);

            ayccm.addCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

            // handle error response code it occurs
            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream;
            Log.v("ayc-login", " response code: " + responseCode);
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
            for(int i = 0; i <= resp.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i+1) * maxLogSize;
                end = end > resp.length() ? resp.length() : end;
                Log.v("ayc-resp", resp.substring(start,end));
            }

            if(resp.contains("<strong>ERROR</strong>: Incorrect username or password")){
                Log.v("ayc-login", " Failed to log in");
                return "{\"loggedin\":false}";
            }

            Log.v("ayc-login", " logged in");
            return "{\"loggedin\":true}";
        } catch (Exception e) {
            Log.v("ayc-async", e.getMessage());
        }

        return "{\"loggedin\":false}";
    }
}
