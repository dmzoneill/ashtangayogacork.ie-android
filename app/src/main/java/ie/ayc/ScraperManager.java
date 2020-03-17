package ie.ayc;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScraperManager extends AsyncTask<String, String, String> implements AsyncResponse {

    private static ScraperManager instance;
    private final AsyncResponse this_async;
    public AsyncResponse delegate = null;
    private static JSONObject classes;
    private static JSONArray prices;
    private static JSONObject bookings;
    private static JSONObject transactions;
    private static JSONObject used_credit;
    private static JSONObject expiring_credit;

    private ScraperManager() {
        //set context variables if required
        this.this_async = this;
        this.delegate = this;
    }

    public static JSONObject getClasses(){
        return classes;
    }

    public static JSONArray getPrices(int filter) {
        JSONArray filtered = new JSONArray();

        Log.v("ayc-scraper", " total prices: " + prices.length());

        try {
            for (int y = 0; y < prices.length(); y++) {
                JSONObject jo = prices.getJSONObject(y);

                Log.v("ayc-scraper", " prices filter " + String.valueOf(y) + " / " + prices.length());
                switch (filter) {
                    case 0:
                        Log.v("ayc-scraper", " prices filter monthly: " + jo.get("monthly").toString());
                        if(jo.get("monthly").toString().compareToIgnoreCase("1") == 0){
                            filtered.put(jo);
                        }
                        break;
                    case 1:
                        Log.v("ayc-scraper", " prices filter special: " + jo.get("class_type_restriction").toString());
                        if(jo.get("class_type_restriction").toString().compareToIgnoreCase("null") != 0){
                            filtered.put(jo);
                        }
                        break;
                    case 2:
                    default:
                        Log.v("ayc-scraper", " prices filter standard: " + jo.get("monthly").toString());
                        if(jo.get("monthly").toString().compareToIgnoreCase("0") == 0){
                            filtered.put(jo);
                        }
                        break;
                }
            }
        }
        catch (Exception e){
            Log.v("ayc-scraper", " prices filter error: " + e.getMessage());
        }

        return filtered;
    }

    public static ScraperManager getInstance(){
        if(instance == null){
            instance = new ScraperManager();
        }
        return instance;
    }

    public void fetch_all() {
        ScraperManager task1 = new ScraperManager();
        task1.delegate = this.this_async;
        task1.execute("https://ashtangayoga.ie/json/?a=get_bookings");

        ScraperManager task2 = new ScraperManager();
        task2.delegate = this.this_async;
        task2.execute("https://ashtangayoga.ie/json/?a=get_classes");

        ScraperManager task3 = new ScraperManager();
        task3.delegate = this.this_async;
        task3.execute("https://ashtangayoga.ie/json/?a=get_prices");
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

        AycCookieManager ayccm = AycCookieManager.getInstance();

        try {
            URL url = new URL(urlString);
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
            urlConnection.addRequestProperty("REFERER", "https://ashtangayoga.ie/wp-login.php");
            urlConnection.setRequestProperty("charset", "utf-8");

            ayccm.addCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

            // handle error response code it occurs
            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream;
            Log.v("ayc-scraper", " response code: " + responseCode);
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
                Log.v("ayc-scraper-resp", resp.substring(start, end));
            }

            return resp;
        } catch (Exception e) {
            Log.v("ayc-scraper", e.getMessage());
        }

        return "";
    }

    @Override
    public void processFinish(String output) throws JSONException {
        Log.v("ayc-scraper", output);
        try {
            JSONObject reader = new JSONObject(output);
            switch (reader.getString("action")) {
                case "get_bookings":
                    bookings = reader;
                    break;
                case "get_prices":
                    prices = reader.getJSONArray("result");
                    break;
                case "get_classes":
                    classes = reader;
                    break;
            }
        } catch (Exception e) {
            Log.v("ayc-error", e.getStackTrace().toString());
        }
    }
}
