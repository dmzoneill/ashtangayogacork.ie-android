package ie.ayc;

import android.util.Log;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SessionManager extends AycAsyncTask<String, String, String> {

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

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call

        AycCookieManager ayccm = AycCookieManager.getInstance();

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.addRequestProperty("Cookie", ayccm.getCookieValue());

            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(4000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("charset", "utf-8");

            ayccm.addCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

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
