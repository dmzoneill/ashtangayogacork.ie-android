package ie.ayc;

import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import ie.ayc.ui.Session;

public class SessionManager extends AsyncTask<String, String, String> {

    public AsyncResponse delegate = null;

    @Override
    protected void onPostExecute(String result) {
        try {
            delegate.processFinish(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SessionManager(){
        //set context variables if required
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            for (String cookie : Session.getCookies()) {
                urlConnection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }

            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(4000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("charset", "utf-8");

            Session.setCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

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

            return response.toString();
        } catch (Exception e) {
            Log.v("ayc-async",e.getMessage());
        }

        return "{\"loggedin\":false}";
    }
}
