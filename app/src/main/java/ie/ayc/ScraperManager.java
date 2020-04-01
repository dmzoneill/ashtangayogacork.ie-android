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
import java.util.ArrayList;

public class ScraperManager extends AsyncTask<String, String, String> implements AsyncResponse, Observable {

    private static ArrayList<Observer> observers;
    private static ScraperManager instance;
    private final AsyncResponse this_async;
    public AsyncResponse delegate = null;
    private static String page_welcome;
    private static JSONArray profile;
    private static JSONArray classes;
    private static JSONArray prices;
    private static JSONArray bookings;
    private static JSONArray transactions;
    private static JSONArray used_credit;
    private static JSONArray expiring_credit;
    private static JSONObject add_booking;

    private ScraperManager() {
        //set context variables if required
        observers = new ArrayList<>();

        this.this_async = this;
        this.delegate = this;
    }

    public static ScraperManager getInstance(){
        if(instance == null){
            instance = new ScraperManager();
        }
        return instance;
    }

    public static JSONArray getProfile(){
        return profile;
    }

    public static JSONArray getBookings(){
        return bookings;
    }

    public static JSONArray getExpiringCredit(){
        return expiring_credit;
    }

    public static JSONArray getUsedCredit(){
        return used_credit;
    }

    public static JSONArray getTransactions(){
        return transactions;
    }

    public static JSONArray getPrices(int filter) {
        JSONArray filtered = new JSONArray();

        try {
            Log.v("ayc-scraper", " total prices: " + prices.length());

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


    public static JSONArray getClasses(int filter_sticky){
        JSONArray filtered = new JSONArray();

        try {
            for (int y = 0; y < classes.length(); y++) {
                JSONObject jo = classes.getJSONObject(y);

                if(jo.get("sticky").toString().compareToIgnoreCase(String.valueOf(filter_sticky))==0){
                    filtered.put(jo);
                }
            }
        }
        catch (Exception e){
            Log.v("ayc-scraper", " classes filter error: " + e.getStackTrace());
        }

        return filtered;
    }

    public static JSONObject getClassById(String id){
        try {
            for (int y = 0; y < classes.length(); y++) {
                JSONObject jo = classes.getJSONObject(y);

                if(jo.get("class_id").toString().compareToIgnoreCase(id)==0){
                    return jo;
                }
            }
        }
        catch (Exception e){
            Log.v("ayc-scraper-class", " get class by id : " + e.getStackTrace());
            return null;
        }

        return null;
    }

    public void fetch_all() {
        int i = 0;
        String json_endpoint = "https://ashtangayoga.ie/json/?a=";

        String[] urls = new String[7];
        urls[i++] = "get_profile";
        urls[i++] = "get_used_credit";
        urls[i++] = "get_expiring_credit";
        urls[i++] = "get_transactions";
        urls[i++] = "get_bookings";
        urls[i++] = "get_classes";
        urls[i++] = "get_prices";

        for(String action: urls) {
            ScraperManager task = new ScraperManager();
            task.delegate = this.this_async;
            task.execute(json_endpoint+action);
        }
    }

    public void book_class_add(String class_id){
        ScraperManager book_class = new ScraperManager();
        book_class.delegate = this.this_async;
        book_class.execute("https://ashtangayoga.ie/json/?a=add_booking&class_id="+class_id);
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
    public void processFinish(String output) {
        Log.v("ayc-scraper", output);
        try {
            JSONObject reader = new JSONObject(output);

            Object obj = reader.get("result");
            if(obj instanceof Boolean) {
                this.object_notify(UpdateSource.logout);
                return;
            }

            switch (reader.getString("action")) {
                case "get_bookings":
                    bookings = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.classes);
                    break;
                case "get_prices":
                    prices = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.prices);
                    break;
                case "get_classes":
                    classes = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.classes);
                    break;
                case "get_profile":
                    profile = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.profile);
                    break;
                case "get_transactions":
                    transactions = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.profile);
                    break;
                case "get_used_credit":
                    used_credit = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.profile);
                    break;
                case "add_booking":
                    add_booking = reader.getJSONObject("result");
                    this.object_notify(UpdateSource.classes);
                    break;
                case "get_expiring_credit":
                    expiring_credit = reader.getJSONArray("result");
                    this.object_notify(UpdateSource.profile);
                    break;
                case "get_page":
                    JSONArray js = reader.getJSONArray("result");
                    String pname = js.get(0).toString();
                    switch(pname){
                        case "Welcome":
                            page_welcome = java.net.URLDecoder.decode(js.get(1).toString(),"UTF-8");
                            break;
                    }

                    break;
            }
            this.notify_all();
        } catch (Exception e) {
            Log.v("ayc-error", e.getStackTrace().toString());
        }
    }

    @Override
    public void attach(Observer obj) {
        if(observers.contains(obj)==false){
            observers.add(obj);
        }
    }

    @Override
    public void detach(Observer obj) {
        if(observers.contains(obj)){
            observers.remove(obj);
        }
    }

    @Override
    public void object_notify(UpdateSource updatesource) {
        for (Observer obj: observers) {
            obj.update(updatesource);
        }
    }

    @Override
    public void notify_all() {
        for (UpdateSource source : UpdateSource.values()) {
            if(source == UpdateSource.logout) continue;
            this.object_notify(source);
        }
    }
}
