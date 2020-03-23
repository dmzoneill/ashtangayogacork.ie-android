package ie.ayc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

public class AycCookieManager {
    private static final String COOKIES_FILE = "cookies";
    private static final String COOKIES_DELIMETER = "\n";
    private static AycCookieManager instance;
    private CookieManager mCookieManager = null;

    private AycCookieManager() {
        this.mCookieManager = new CookieManager();
        this.mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);
        this.loadCookies();
    }

    private CookieManager getCookieManager() {
        return this.mCookieManager;
    }

    public static AycCookieManager getInstance() {
        if (instance == null) {
            instance = new AycCookieManager();
        }
        return instance;
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static String implode(List<HttpCookie> list) {
        Log.v("ayc-cookie-save", "imploding: " + list.size() + " cookies");

        if (list.size() == 0) return "";
        if (list.size() == 1) return list.get(0).getName() + "=" + list.get(0).getValue();

        String imploded = "";
        int y = 0;

        while (y < list.size() - 1) {
            imploded += list.get(y).getName() + "=" + list.get(y).getValue() + COOKIES_DELIMETER;
            y++;
        }
        imploded += list.get(y).getName() + "=" + list.get(y).getValue();

        return imploded;
    }

    public List<HttpCookie> getCookies() {
        if (this.mCookieManager == null) {
            return null;
        } else {
            return this.mCookieManager.getCookieStore().getCookies();
        }
    }

    public void addCookies(List<String> list) {
        try {
            for (String cookie : list) {
                String[] allparts = cookie.split(";");
                String[] cookie_parts = allparts[0].split("=");
                HttpCookie thecookie = new HttpCookie(cookie_parts[0], cookie_parts[1]);
                this.mCookieManager.getCookieStore().add(new URI("https://ashtangayoga.ie"), thecookie);
                Log.v("ayc-cookie-update", "updated cookie store with: " + cookie);
                this.saveCookies();
            }
        } catch (Exception e) {
            Log.v("ayc-cookie-load", "failed to update cookie store");
        }
    }

    public void clearCookies() {
        if (this.mCookieManager != null) {
            this.mCookieManager.getCookieStore().removeAll();
            this.saveCookies();
        }
    }

    public void saveCookies() {
        try {
            FileOutputStream outputStream = Login.mContext.openFileOutput(COOKIES_FILE, Context.MODE_PRIVATE);
            String imploded = implode(getCookies());
            Log.v("ayc-cookie-save", "imploded: " + imploded);
            outputStream.write(imploded.getBytes());
            outputStream.close();
            Log.v("ayc-cookie-save", "saved cookies to file");
        } catch (Exception e) {
            Log.v("ayc-cookie-save", "failed ot save cookies to file");
        }
    }

    public void loadCookies() {
        try {
            InputStream inputStream = Login.mContext.openFileInput(COOKIES_FILE);
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));
            String line = buf.readLine();

            while (line != null) {
                //sb.append(line);
                String[] cookie_parts = line.split("=");
                this.mCookieManager.getCookieStore().add(new URI("https://ashtangayoga.ie"), new HttpCookie(cookie_parts[0], cookie_parts[1]));
                Log.v("ayc-cookie-load", "load cookie: " + line);
                line = buf.readLine();
            }

            Log.v("ayc-cookie-load", "Done loading cookies");
        } catch (Exception e) {
            Log.v("ayc-cookie-load", "failed to load cookies from file");
        }
    }

    public boolean isCookieManagerEmpty() {
        if (this.mCookieManager == null) {
            return true;
        } else {
            return this.mCookieManager.getCookieStore().getCookies().isEmpty();
        }
    }

    public String getCookieValue() {
        String cookieValue = "";

        if (!isCookieManagerEmpty()) {
            for (HttpCookie eachCookie : getCookies()) {
                Log.v("ayc-coookie-manager",String.format("%s=%s; ", eachCookie.getName(), eachCookie.getValue()));
                cookieValue = cookieValue + String.format("%s=%s; ", eachCookie.getName(), eachCookie.getValue());
            }
        }
        return cookieValue;
    }
}